/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.utils.constNull
import org.jetbrains.kotlin.bir.backend.utils.constTrue
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.builders.build
import org.jetbrains.kotlin.bir.builders.defaultValueForType
import org.jetbrains.kotlin.bir.builders.setTemporary
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.expressions.impl.*
import org.jetbrains.kotlin.bir.replaceWith
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.bir.types.isClassWithFqName
import org.jetbrains.kotlin.bir.types.isUnit
import org.jetbrains.kotlin.bir.types.utils.classOrNull
import org.jetbrains.kotlin.bir.utils.deepCopy
import org.jetbrains.kotlin.bir.utils.explicitParameters
import org.jetbrains.kotlin.bir.utils.getArgumentsWithBir
import org.jetbrains.kotlin.bir.utils.usesDefaultArguments
import org.jetbrains.kotlin.builtins.StandardNames

context (WasmBirContext)
open class TailrecLowering : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        getElementsOfClass<BirSimpleFunction>().forEach { function ->
            if (function.isTailrec) {
                lowerTailRecursionCalls(function)
            }
        }
    }

    open val useProperComputationOrderOfTailrecDefaultParameters: Boolean
        get() = true

    open fun followFunctionReference(reference: BirFunctionReference): Boolean = false

    open fun nullConst(sourceSpan: SourceSpan, type: BirType): BirExpression =
        BirConst.defaultValueForType(sourceSpan, type)

    private fun lowerTailRecursionCalls(function: BirSimpleFunction) {
        val oldBody = function.body as? BirBlockBody ?: return

        val (tailRecursionCalls, someCallsAreFromOtherFunctions) = collectTailRecursionCalls(function, ::followFunctionReference)
        if (tailRecursionCalls.isEmpty()) {
            return
        }

        val oldBodyStatements = oldBody.statements.toList()
        oldBody.statements.clear()

        // `return recursiveCall(...)` is rewritten into assignments to parameters followed by a jump to the start.
        // While we may be able to write to the parameters directly, the recursive call may be inside an inline lambda,
        // so the parameters are captured and assigning to them requires temporarily rewriting their types (see
        // `SharedVariablesLowering`), and that we can't do. So we have to create new `var`s for this purpose.
        // TODO: an optimization pass will rewrite the types of vars back since the lambdas are guaranteed to be inlined
        //  in place (otherwise they can't jump to the start of the function at all), so this is all a waste of CPU time.
        val parameterToVariable = function.explicitParameters.associateWith {
            if (someCallsAreFromOtherFunctions || !it.isAssignable)
                BirVariable.build {
                    setTemporary(nameHint = it.suggestVariableName())
                    isVar = true
                    initializer = BirGetValueImpl(oldBody.sourceSpan, it.type, it, null)
                }
            else
                it
        }

        val loopBody = BirBlockImpl(oldBody.sourceSpan, birBuiltIns.unitType, null)
        val loopCondition = BirBlockImpl(oldBody.sourceSpan, birBuiltIns.booleanType, null)
        val loop = BirDoWhileLoopImpl(oldBody.sourceSpan, birBuiltIns.unitType, null, loopBody, loopCondition, null)
        oldBody.statements += loop

        oldBodyStatements.forEach {
            loopBody.statements += it
        }
        loopBody.statements += BirBreakImpl(oldBody.sourceSpan, birBuiltIns.unitType, loop, null)

        for ((parameter, variable) in parameterToVariable.entries) {
            if (parameter.isAssignable && parameter !== variable) {
                loopCondition.statements += BirSetValueImpl(
                    oldBody.sourceSpan, parameter.type, parameter, null,
                    BirGetValueImpl(oldBody.sourceSpan, variable.type, variable, null)
                )
            }
        }
        loopCondition.statements += BirConst.constTrue()

        for (call in tailRecursionCalls) {
            genTailCall(call, function.explicitParameters, parameterToVariable, loop)
        }
    }

    private fun genTailCall(
        call: BirCall,
        parameters: List<BirValueParameter>,
        parameterToVariable: Map<BirValueParameter, BirValueDeclaration>,
        loop: BirDoWhileLoop,
    ) {
        val block = BirBlockImpl(call.sourceSpan, call.type, null)
        call.replaceWith(block)

        // Get all specified arguments:
        val parameterToArgument = call.getArgumentsWithBir().associateTo(mutableMapOf()) { (parameter, argument) ->
            // Note that we create `val`s for those parameters so that if some default value contains an object
            // that captures another parameter, it won't capture it as a mutable ref.
            parameter to BirVariable.build {
                sourceSpan = call.sourceSpan
                setTemporary()
                type = argument.type
                initializer = argument
            }
        }

        // Create new null-initialized variables for all other values in case of forward references:
        //   fun f(x: () -> T = { y }, y: T = ...) // in `f()`, `x()` returns `null`
        val defaultValuedParameters = parameters.filter { it !in parameterToArgument }
        defaultValuedParameters.associateWithTo(parameterToArgument) {
            // Note that we intentionally keep the original type of the parameter for the variable even though that violates type safety
            // if it's non-null. This ensures that capture parameters have the same types for all copies of `x`.
            BirVariable.build {
                sourceSpan = call.sourceSpan
                setTemporary()
                type = it.type
                initializer = BirConst.constNull()
            }
        }

        // Now replace those variables with ones containing actual default values. Unused null-valued temporaries will hopefully
        // be optimized out later.
        for ((parameter, argument) in parameterToArgument) {
            parameter.referencedBy.forEach { ref ->
                if (ref is BirValueAccessExpression && ref.target == parameter) {
                    ref.target = argument
                }
            }
        }

        defaultValuedParameters.let { if (useProperComputationOrderOfTailrecDefaultParameters) it else it.asReversed() }
            .associateWithTo(parameterToArgument) { parameter ->
                val originalDefaultValue = parameter.defaultValue?.expression ?: throw Error("no argument specified for $parameter")
                BirVariable.build {
                    sourceSpan = call.sourceSpan
                    setTemporary()
                    initializer = originalDefaultValue.deepCopy()
                }
            }

        for ((parameter, argument) in parameterToArgument) {
            val variable = parameterToVariable[parameter]!!
            block.statements += BirSetValueImpl(
                call.sourceSpan, variable.type, variable, null,
                BirGetValueImpl(call.sourceSpan, argument.type, argument, null)
            )
        }

        block.statements += BirContinueImpl(call.sourceSpan, call.type, loop, null)
    }

    private fun BirValueParameter.suggestVariableName(): String =
        if (name.isSpecial) {
            val oldNameStr = name.asString()
            "$" + oldNameStr.substring(1, oldNameStr.length - 1)
        } else {
            name.identifier
        }


    private data class TailCalls(val calls: Set<BirCall>, val fromManyFunctions: Boolean)

    /**
     * Collects calls to be treated as tail recursion.
     * The checks are partially based on the frontend implementation
     * in `ControlFlowInformationProvider.markAndCheckRecursiveTailCalls()`.
     *
     * This analysis is not very precise and can miss some calls.
     * It is also not guaranteed that each returned call is detected as tail recursion by the frontend.
     * However any returned call can be correctly optimized as tail recursion.
     */
    private fun collectTailRecursionCalls(
        function: BirSimpleFunction,
        followFunctionReference: (BirFunctionReference) -> Boolean,
    ): TailCalls {
        val isUnitReturn = function.returnType.isUnit()
        val result = mutableSetOf<BirCall>()
        var someCallsAreInOtherFunctions = false

        class VisitorState(val isTailExpression: Boolean, val inOtherFunction: Boolean)
        function.body?.traverseStackBased(VisitorState(isTailExpression = true, inOtherFunction = false)) { element, state ->
            when (element) {
                is BirFunction -> {
                    // Ignore local functions.
                }
                is BirClass -> {
                    // Ignore local classes
                }
                is BirTry -> {
                    // We do not support tail calls in try-catch-finally, for simplicity of the mental model
                    // very few cases there would be real tail-calls, and it's often not so easy for the user to see why
                }
                is BirReturn -> {
                    element.value.walkInto(VisitorState(element.returnTarget == function, state.inOtherFunction))
                }
                is BirBlockBody, is BirContainerExpression -> {
                    element as BirStatementContainer
                    element.statements.forEachIndexed { index, statement ->
                        val isTailStatement = if (index == element.statements.size - 1) {
                            // The last statement defines the result of the container expression, so it has the same kind.
                            state.isTailExpression
                        } else if (isUnitReturn) {
                            // In a Unit-returning function, any statement directly followed by a `return` is a tail statement.
                            element.statements.elementAt(index + 1).let {
                                it is BirReturn && it.returnTarget == function && it.value.isUnitRead()
                            }
                        } else false

                        statement.walkInto(VisitorState(isTailStatement, state.inOtherFunction))
                    }
                }
                is BirWhen -> {
                    element.branches.forEach {
                        it.condition.walkInto(VisitorState(isTailExpression = false, state.inOtherFunction))
                        it.result.walkInto(state)
                    }
                }
                is BirCall -> {
                    element.walkInto(VisitorState(isTailExpression = false, state.inOtherFunction))

                    // TODO: the frontend generates diagnostics on calls that are not optimized. This may or may not
                    //   match what the backend does here. It'd be great to validate that the two are in agreement.
                    if (!state.isTailExpression || element != function) {
                        return@traverseStackBased
                    }
                    // TODO: check type arguments

                    if (function.overriddenSymbols.isNotEmpty() && element.usesDefaultArguments()) {
                        // Overridden functions using default arguments at tail call are not included: KT-4285
                        return@traverseStackBased
                    }

                    val hasSameDispatchReceiver =
                        function.dispatchReceiverParameter?.type?.classOrNull?.asElement?.kind?.isSingleton == true ||
                                element.dispatchReceiver?.let { it is BirGetValue && it.target == function.dispatchReceiverParameter } != false
                    if (!hasSameDispatchReceiver) {
                        // A tail call is not allowed to change dispatch receiver
                        //   class C {
                        //       fun foo(other: C) {
                        //           other.foo(this) // not a tail call
                        //       }
                        //   }
                        // TODO: KT-15341 - if the tailrec function is neither `override` nor `open`, this is fine actually?
                        //   Probably requires editing the frontend too.
                        return@traverseStackBased
                    }

                    if (state.inOtherFunction) {
                        someCallsAreInOtherFunctions = true
                    }
                    result.add(element)
                }
                is BirFunctionReference -> {
                    element.walkIntoChildren(VisitorState(isTailExpression = false, state.inOtherFunction))
                    // This should match inline lambdas:
                    //   tailrec fun foo() {
                    //     run { return foo() } // non-local return from `foo`, so this *is* a tail call
                    //   }
                    // Whether crossinline lambdas are matched is unimportant, as they can't contain any returns
                    // from `foo` anyway.
                    if (followFunctionReference(element)) {
                        // If control reaches end of lambda, it will *not* end the current function by default,
                        // so the lambda's body itself is not a tail statement.
                        (element.target as BirFunction).body?.walkIntoChildren(
                            VisitorState(
                                isTailExpression = false,
                                inOtherFunction = true
                            )
                        )
                    }
                }
                else -> element.walkIntoChildren(state)
            }
        }

        return TailCalls(result, someCallsAreInOtherFunctions)
    }

    private fun BirExpression.isUnitRead(): Boolean =
        this is BirGetObjectValue && target.isClassWithFqName(StandardNames.FqNames.unit)
}
