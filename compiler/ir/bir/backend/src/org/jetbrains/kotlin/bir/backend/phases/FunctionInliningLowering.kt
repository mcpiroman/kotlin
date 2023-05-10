/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.backend.common.lower.inline.*
import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.backend.BirBackendContext
import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.InnerClassesSupport
import org.jetbrains.kotlin.bir.backend.utils.implicitCastIfNeededTo
import org.jetbrains.kotlin.bir.backend.utils.isPure
import org.jetbrains.kotlin.bir.backend.utils.isTypeOfIntrinsic
import org.jetbrains.kotlin.bir.builders.BirNoExpression
import org.jetbrains.kotlin.bir.builders.build
import org.jetbrains.kotlin.bir.builders.setTemporary
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.expressions.impl.*
import org.jetbrains.kotlin.bir.symbols.*
import org.jetbrains.kotlin.bir.traversal.BirTreeStackBasedTraverseScope
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.bir.types.*
import org.jetbrains.kotlin.bir.types.utils.*
import org.jetbrains.kotlin.bir.utils.*
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.types.SimpleTypeNullability
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import org.jetbrains.kotlin.utils.mapOrTakeThisIfIdentity
import org.jetbrains.kotlin.utils.memoryOptimizedMap

interface InlineFunctionResolver {
    fun getFunctionDeclaration(symbol: BirFunctionSymbol): BirFunction
    fun getFunctionSymbol(function: BirFunction): BirFunctionSymbol
}

context(BirBackendContext)
open class DefaultInlineFunctionResolver() : InlineFunctionResolver {
    override fun getFunctionDeclaration(symbol: BirFunctionSymbol): BirFunction {
        val function = symbol as BirFunction
        // TODO: Remove these hacks when coroutine intrinsics are fixed.
        return when {
            function.isBuiltInSuspendCoroutineUninterceptedOrReturn() ->
                builtInSymbols.suspendCoroutineUninterceptedOrReturn

            symbol == builtInSymbols.coroutineContextGetter ->
                builtInSymbols.coroutineGetContext

            else -> (symbol as? BirSimpleFunction)?.resolveFakeOverride() ?: symbol
        }
    }

    override fun getFunctionSymbol(function: BirFunction): BirFunctionSymbol {
        return function
    }

    private fun BirFunction.isBuiltInSuspendCoroutineUninterceptedOrReturn(): Boolean =
        isTopLevelInPackage(
            "suspendCoroutineUninterceptedOrReturn",
            StandardNames.COROUTINES_INTRINSICS_PACKAGE_FQ_NAME.asString()
        )

    private fun BirFunction.isTopLevelInPackage(name: String, packageName: String): Boolean {
        if (name != this.name.asString()) return false

        val containingDeclaration = ancestors().firstIsInstanceOrNull<BirDeclarationHost>() as? BirPackageFragment ?: return false
        val packageFqName = containingDeclaration.fqName.asString()
        return packageName == packageFqName
    }
}

context(BirBackendContext)
open class FunctionInliningLowering(
    private val inlineFunctionResolver: InlineFunctionResolver,
    private val innerClassesSupport: InnerClassesSupport? = null,
    private val insertAdditionalImplicitCasts: Boolean = false,
    private val alwaysCreateTemporaryVariablesForArguments: Boolean = false,
    private val inlinePureArguments: Boolean = true,
    private val regenerateInlinedAnonymousObjects: Boolean = false,
    private val inlineArgumentsWithTheirOriginalTypeAndOffset: Boolean = false,
    private val allowExternalInlining: Boolean = false,
) : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        class CallToInline(val callSite: BirFunctionAccessExpression, val callee: BirFunction) {
            var alreadyInlined = false
        }

        val allCallsToInline = mutableListOf<CallToInline>()
        val functionsWithCallsToInline = mutableMapOf<BirFunction, MutableList<CallToInline>>()

        fun visitFunctionCall(call: BirFunctionAccessExpression) {
            val callee = call.target as BirFunction
            if (callee.needsInlining && !isTypeOfIntrinsic(callee)) {
                val actualCallee = inlineFunctionResolver.getFunctionDeclaration(callee)
                val callToInline = CallToInline(call, actualCallee)
                allCallsToInline += callToInline

                call.ancestors().firstIsInstanceOrNull<BirFunction>()?.let { currentFunction ->
                    functionsWithCallsToInline.computeIfAbsent(currentFunction) { mutableListOf() }.add(callToInline)
                }
            }
        }
        getElementsOfClass<BirCall>().forEach(::visitFunctionCall)
        getElementsOfClass<BirConstructorCall>().forEach(::visitFunctionCall)

        fun tryToInline(call: CallToInline) {
            // First inline everything inside the function we try to inline
            functionsWithCallsToInline[call.callee]?.forEach {
                tryToInline(it)
            }

            if (!call.alreadyInlined) {
                val scope = call.callSite.ancestors().firstIsInstanceOrNull<BirDeclarationHost>()
                    ?: call.callSite.ancestors().firstIsInstanceOrNull<BirDeclaration>()
                    ?: call.callee
                val inlinedCall = Inliner(call.callSite, call.callee, TemporaryVariablesScope(scope as BirSymbolElement)).inline()
                call.callSite.replaceWith(inlinedCall)
                call.alreadyInlined = true
            }
        }

        for (call in allCallsToInline) {
            tryToInline(call)
        }
    }

    private val BirFunction.needsInlining get() = this.isInline && (allowExternalInlining || !this.isExternal)

    private inner class Inliner(
        private val callSite: BirFunctionAccessExpression,
        private val callee: BirFunction,
        private val currentScope: TemporaryVariablesScope,
    ) {
        private val copier: BirTreeDeepCopierForInliner
        val substituteMap = mutableMapOf<BirValueParameter, BirExpression>()

        context(BirBackendContext)
        init {
            val typeParameters = if (callee is BirConstructor)
                callee.parentAsClass.typeParameters
            else callee.typeParameters

            @Suppress("UNCHECKED_CAST")
            val typeArgumentsMap = (typeParameters zip callSite.typeArguments)
                .filter { it.second != null }
                .toMap() as Map<BirTypeParameter, BirType>
            copier = BirTreeDeepCopierForInliner(typeArgumentsMap)
        }

        fun inline() = inlineFunction(callSite, callee, inlineFunctionResolver.getFunctionSymbol(callee) as BirFunction, true)

        @OptIn(ObsoleteDescriptorBasedAPI::class)
        private fun inlineFunction(
            callSite: BirFunctionAccessExpression,
            callee: BirFunction,
            originalInlinedElement: BirElement,
            performRecursiveInline: Boolean,
        ): BirReturnableBlock {
            val copiedCallee = callee.deepCopy(copier)
            if (performRecursiveInline) {
                // but nothing to do here, right?
            }

            val evaluationStatements = evaluateArguments(callSite, copiedCallee)
            val copiedBody = copiedCallee.body as? BirBlockBody
                ?: error("Body not found for function ${callee.render()}")
            val statements = copiedBody.statements.toList()
            copiedBody.statements.clear()

            val newStatements = statements.map { it.substituteParameters() }

            val inlinedBlock = BirInlinedFunctionBlockImpl(
                sourceSpan = callSite.sourceSpan,
                type = callSite.type,
                inlineCall = callSite,
                inlinedElement = originalInlinedElement,
                origin = null,
            )
            inlinedBlock.statements += evaluationStatements
            inlinedBlock.statements += newStatements

            // Note: here we wrap `BirInlinedFunctionBlock` inside `BirReturnableBlock` because such way it is easier to
            // control special composite blocks that are inside `BirInlinedFunctionBlock`
            val inlinedCall = BirReturnableBlockImpl(
                sourceSpan = callSite.sourceSpan,
                type = callSite.type,
                origin = null,
                _descriptor = null
            )
            inlinedCall.statements += inlinedBlock

            inlinedCall.traverseStackBased { element ->
                if (regenerateInlinedAnonymousObjects && element is BirAttributeContainer) {
                    if (element.attributeOwnerId != element) {
                        element[GlobalBirElementAuxStorageTokens.OriginalBeforeInline] = element.attributeOwnerId
                        element.attributeOwnerId = element
                    }
                }

                if (element is BirReturn) {
                    if (element.returnTarget == copiedCallee) {
                        val newExpr = element.value.also { element.value = BirNoExpression() }.doImplicitCastIfNeededTo(callSite.type)
                        element.value = newExpr
                        element.type = birBuiltIns.nothingType
                        element.returnTarget = inlinedCall
                    }
                }
                element.walkIntoChildren()
            }

            return inlinedCall
        }

        //---------------------------------------------------------------------//

        fun BirStatement.substituteParameters(): BirStatement {
            return traverseStackBased { element ->
                if (element is BirGetValue) {
                    val newExpression = element
                    substituteMap[newExpression.target]?.let { argument ->
                        argument.walkIntoChildren() // Default argument can contain subjects for substitution.
                        val ret =
                            if (argument.sourceSpan == NO_LOCATION_YET) {
                                BirGetValueImpl(newExpression.sourceSpan, element.type, element.target, element.origin)
                            } else {
                                argument.deepCopy(copier)
                            }
                        element.replaceWith(ret.doImplicitCastIfNeededTo(newExpression.type))
                    }
                }
                if (element is BirCall) {
                    // TODO extract to common utils OR reuse ContractDSLRemoverLowering
                    if (element.target.asElement.hasAnnotation(ContractsDslNames.CONTRACTS_DSL_ANNOTATION_FQN)) {
                        element.replaceWith(BirCompositeImpl(element.sourceSpan, birBuiltIns.unitType, null))
                    }

                    if (isLambdaCall(element)) {
                        val dispatchReceiver = element.dispatchReceiver?.unwrapAdditionalImplicitCastsIfNeeded() as BirGetValue
                        substituteMap[dispatchReceiver.target]?.let { functionArgument ->
                            if ((dispatchReceiver.target as? BirValueParameter)?.isNoinline != true) {
                                when {
                                    functionArgument is BirFunctionReference ->
                                        inlineFunctionReference(element, functionArgument, functionArgument.target as BirFunction)

                                    functionArgument is BirPropertyReference && functionArgument.field != null ->
                                        inlineField(element, functionArgument)

                                    functionArgument is BirPropertyReference -> inlinePropertyReference(element, functionArgument)

                                    functionArgument.isAdaptedFunctionReference() ->
                                        inlineAdaptedFunctionReference(element, functionArgument as BirBlock)

                                    functionArgument is BirFunctionExpression ->
                                        inlineFunctionExpression(element, functionArgument)

                                    else -> null
                                }?.let {
                                    element.replaceWith(it)
                                }
                            }
                        }
                    }
                }
            } as BirStatement
        }

        fun inlineFunctionExpression(call: BirCall, functionExpression: BirFunctionExpression): BirExpression {
            // Inline the lambda. Lambda parameters will be substituted with lambda arguments.
            val newExpression = inlineFunction(
                call, functionExpression.function, functionExpression, false
            )
            // Substitute lambda arguments with target function arguments.
            return newExpression.substituteParameters() as BirExpression
        }

        private fun inlineField(invokeCall: BirCall, propertyReference: BirPropertyReference): BirExpression {
            return wrapInStubFunction(invokeCall, invokeCall, propertyReference)
        }

        private fun inlinePropertyReference(expression: BirCall, propertyReference: BirPropertyReference): BirExpression {
            val getterCall = BirCall.build {
                sourceSpan = expression.sourceSpan
                type = expression.type
                target = propertyReference.getter!!
                origin = INLINED_FUNCTION_REFERENCE
            }

            fun tryToGetArg(i: Int): BirExpression? {
                if (i >= expression.valueArguments.size) return null
                return expression.valueArguments.elementAt(i).takeUnless { it is BirNoExpression }?.substituteParameters() as BirExpression?
            }

            val receiverFromField = propertyReference.dispatchReceiver ?: propertyReference.extensionReceiver
            getterCall.dispatchReceiver = getterCall.target.asElement.dispatchReceiverParameter?.let {
                receiverFromField ?: tryToGetArg(0)
            }
            getterCall.extensionReceiver = getterCall.target.asElement.extensionReceiverParameter?.let {
                when (getterCall.target.asElement.dispatchReceiverParameter) {
                    null -> receiverFromField ?: tryToGetArg(0)
                    else -> tryToGetArg(if (receiverFromField != null) 0 else 1)
                }
            }

            return wrapInStubFunction(getterCall.substituteParameters() as BirExpression, expression, propertyReference)
        }

        private fun wrapInStubFunction(
            inlinedCall: BirExpression, invokeCall: BirFunctionAccessExpression, reference: BirCallableReference<*>
        ): BirReturnableBlock {
            // Note: This function is not exist in tree. It is appeared only in `BirInlinedFunctionBlock` as intermediate callee.
            val stubForInline = BirSimpleFunction.build {
                sourceSpan = inlinedCall.sourceSpan
                name = Name.identifier("stub_for_ir_inlining")
                visibility = DescriptorVisibilities.LOCAL
                returnType = inlinedCall.type
                isSuspend = (reference.target as? BirSimpleFunction)?.isSuspend == true
            }
            stubForInline.body = BirBlockBodyImpl(SourceSpan.UNDEFINED).apply {
                statements += if (reference is BirPropertyReference && reference.field != null) {
                    val field = reference.field!!.asElement
                    val boundReceiver = reference.dispatchReceiver ?: reference.extensionReceiver
                    val fieldReceiver = if (field.isStatic) null else boundReceiver
                    BirGetFieldImpl(SourceSpan.UNDEFINED, field.type, field, null, fieldReceiver, null)
                } else {
                    BirReturnImpl(SourceSpan.UNDEFINED, birBuiltIns.nothingType, inlinedCall, stubForInline)
                }
            }

            return inlineFunction(invokeCall, stubForInline, reference, false)
        }

        context(BirTreeStackBasedTraverseScope)
        fun inlineAdaptedFunctionReference(irCall: BirCall, irBlock: BirBlock): BirExpression {
            val irFunction = irBlock.statements.first().let {
                it.walkIntoChildren()
                it.deepCopy(copier) as BirFunction
            }
            val irFunctionReference = irBlock.statements.elementAt(1) as BirFunctionReference
            val inlinedFunctionReference = inlineFunctionReference(irCall, irFunctionReference, irFunction)
            return BirBlockImpl(
                irCall.sourceSpan,
                inlinedFunctionReference.type,
                origin = null,
            ).apply {
                statements += irFunction
                statements += inlinedFunctionReference
            }
        }

        context(BirTreeStackBasedTraverseScope)
        fun inlineFunctionReference(
            irCall: BirCall,
            functionReference: BirFunctionReference,
            inlinedFunction: BirFunction
        ): BirExpression {
            functionReference.walkIntoChildren()

            val function = functionReference.target as BirFunction
            val functionParameters = function.explicitParameters
            val boundFunctionParameters = functionReference.getArgumentsWithBir()
            val unboundFunctionParameters = functionParameters - boundFunctionParameters.map { it.first }
            val boundFunctionParametersMap = boundFunctionParameters.associate { it.first to it.second }

            var unboundIndex = 0
            val unboundArgsSet = unboundFunctionParameters.toSet()
            val valueParameters = irCall.getArgumentsWithBir().drop(1) // Skip dispatch receiver.

            val superType = functionReference.type as BirSimpleType
            val superTypeArgumentsMap = (irCall.target.asElement.parentAsClass.typeParameters zip superType.arguments)
                .associate<_, BirTypeParameterSymbol, BirType> { it.first to it.second.typeOrNull!! }

            val immediateCall = when (inlinedFunction) {
                is BirConstructor -> {
                    val classTypeParametersCount = inlinedFunction.parentAsClass.typeParameters.size
                    BirConstructorCall.build {
                        sourceSpan = if (inlineArgumentsWithTheirOriginalTypeAndOffset) functionReference.sourceSpan else irCall.sourceSpan
                        type = inlinedFunction.returnType
                        target = inlinedFunction
                        constructorTypeArgumentsCount = classTypeParametersCount
                        origin = INLINED_FUNCTION_REFERENCE
                    }
                }
                is BirSimpleFunction ->
                    BirCall.build {
                        sourceSpan = if (inlineArgumentsWithTheirOriginalTypeAndOffset) functionReference.sourceSpan else irCall.sourceSpan
                        type = inlinedFunction.returnType
                        target = inlinedFunction
                        origin = INLINED_FUNCTION_REFERENCE
                    }
                else -> error("Unknown function kind : ${inlinedFunction.render()}")
            }.apply {
                for (parameter in functionParameters) {
                    val argument =
                        if (parameter !in unboundArgsSet) {
                            val arg = boundFunctionParametersMap[parameter]!!
                            if (arg is BirGetValue && arg.sourceSpan == NO_LOCATION_YET) {
                                BirGetValueImpl(irCall.sourceSpan, arg.type, arg.target, arg.origin)
                            } else arg.deepCopy(copier)
                        } else {
                            if (unboundIndex == valueParameters.size && parameter.defaultValue != null)
                                parameter.defaultValue!!.expression.deepCopy(copier)
                            else if (!parameter.isVararg) {
                                assert(unboundIndex < valueParameters.size) {
                                    "Attempt to use unbound parameter outside of the callee's value parameters"
                                }
                                valueParameters[unboundIndex++].second
                            } else {
                                val elements = mutableListOf<BirVarargElement>()
                                while (unboundIndex < valueParameters.size) {
                                    val (param, value) = valueParameters[unboundIndex++]
                                    val substitutedParamType = param.type.substitute(superTypeArgumentsMap)
                                    if (substitutedParamType == parameter.varargElementType!!)
                                        elements += value
                                    else
                                        elements += BirSpreadElementImpl(irCall.sourceSpan, value)
                                }
                                BirVarargImpl(
                                    irCall.sourceSpan,
                                    parameter.type,
                                    parameter.varargElementType!!,
                                ).also {
                                    it.elements += elements
                                }
                            }
                        }
                    when (parameter) {
                        function.dispatchReceiverParameter ->
                            this.dispatchReceiver = argument.doImplicitCastIfNeededTo(inlinedFunction.dispatchReceiverParameter!!.type)

                        function.extensionReceiverParameter ->
                            this.extensionReceiver = argument.doImplicitCastIfNeededTo(inlinedFunction.extensionReceiverParameter!!.type)

                        else -> {
                            val i = parameter.getIndex()
                            valueArguments.setElementAt(
                                i,
                                argument.doImplicitCastIfNeededTo(inlinedFunction.valueParameters.elementAt(i).type)
                            )
                        }
                    }
                }
                assert(unboundIndex == valueParameters.size) { "Not all arguments of the callee are used" }
                typeArguments = functionReference.typeArguments
            }

            return if (inlinedFunction.needsInlining && inlinedFunction.body != null) {
                inlineFunction(immediateCall, inlinedFunction, functionReference, performRecursiveInline = true)
            } else {
                val transformedExpression = immediateCall.substituteParameters() as BirExpression
                wrapInStubFunction(transformedExpression, irCall, functionReference)
            }.doImplicitCastIfNeededTo(irCall.type)
        }

        private fun BirExpression.doImplicitCastIfNeededTo(type: BirType): BirExpression {
            if (!insertAdditionalImplicitCasts) return this
            return this.implicitCastIfNeededTo(type)
        }

        // With `insertAdditionalImplicitCasts` flag we sometimes insert
        // casts to inline lambda parameters before calling `invoke` on them.
        // Unwrapping these casts helps us satisfy inline lambda call detection logic.
        private fun BirExpression.unwrapAdditionalImplicitCastsIfNeeded(): BirExpression {
            if (insertAdditionalImplicitCasts && this is BirTypeOperatorCall && this.operator == IrTypeOperator.IMPLICIT_CAST) {
                return this.argument.unwrapAdditionalImplicitCastsIfNeeded()
            }
            return this
        }

        private fun isLambdaCall(call: BirCall): Boolean {
            val callee = call.target.asElement
            val dispatchReceiver = callee.dispatchReceiverParameter ?: return false
            // Uncomment or delete depending on KT-57249 status
//            assert(!dispatchReceiver.type.isKFunction())

            return dispatchReceiver.type.let { it.isFunction() || it.isKFunction() || it.isSuspendFunction() }
                    && callee.name == OperatorNameConventions.INVOKE
                    && call.dispatchReceiver?.unwrapAdditionalImplicitCastsIfNeeded() is BirGetValue
        }

        //---------------------------------------------------------------------//

        private inner class ParameterToArgument(
            val parameter: BirValueParameter,
            val argumentExpression: BirExpression,
            val isDefaultArg: Boolean = false
        ) {
            val isInlinableLambdaArgument: Boolean
                // must take "original" parameter because it can have generic type and so considered as no inline; see `lambdaAsGeneric.kt`
                get() = parameter.getOriginalParameter().isInlineParameter() &&
                        (argumentExpression is BirFunctionReference
                                || argumentExpression is BirFunctionExpression
                                || argumentExpression.isAdaptedFunctionReference())

            val isInlinablePropertyReference: Boolean
                // must take "original" parameter because it can have generic type and so considered as no inline; see `lambdaAsGeneric.kt`
                get() = parameter.getOriginalParameter().isInlineParameter() && argumentExpression is BirPropertyReference

            val isImmutableVariableLoad: Boolean
                get() = argumentExpression.let { argument ->
                    argument is BirGetValue && !argument.target.let { it is BirVariable && it.isVar }
                }
        }

        private fun ParameterToArgument.andAllOuterClasses(): List<ParameterToArgument> {
            val allParametersReplacements = mutableListOf(this)

            if (innerClassesSupport == null) return allParametersReplacements

            var currentThisSymbol = parameter
            var parameterClassDeclaration = parameter.type.classifierOrNull as? BirClass ?: return allParametersReplacements

            while (parameterClassDeclaration.isInner) {
                val outerClass = parameterClassDeclaration.parentAsClass
                val outerClassThis = outerClass.thisReceiver ?: error("${outerClass.name} has a null `thisReceiver` property")

                val parameterToArgument = ParameterToArgument(
                    parameter = outerClassThis,
                    argumentExpression = BirGetFieldImpl(
                        SourceSpan.UNDEFINED,
                        outerClassThis.type,
                        innerClassesSupport.getOuterThisField(parameterClassDeclaration),
                        null,
                        BirGetValueImpl(SourceSpan.UNDEFINED, currentThisSymbol.type, currentThisSymbol, null),
                        null,
                    )
                )

                allParametersReplacements.add(parameterToArgument)

                currentThisSymbol = outerClassThis
                parameterClassDeclaration = outerClass
            }


            return allParametersReplacements
        }

        // callee might be a copied version of callsite.symbol.owner
        private fun buildParameterToArgument(callSite: BirFunctionAccessExpression, callee: BirFunction): List<ParameterToArgument> {
            val parameterToArgument = mutableListOf<ParameterToArgument>()

            if (callSite.dispatchReceiver != null && callee.dispatchReceiverParameter != null)
                parameterToArgument += ParameterToArgument(
                    parameter = callee.dispatchReceiverParameter!!,
                    argumentExpression = callSite.dispatchReceiver!!
                ).andAllOuterClasses()

            val valueArguments = callSite.valueArguments.toMutableList()

            if (callee.extensionReceiverParameter != null) {
                parameterToArgument += ParameterToArgument(
                    parameter = callee.extensionReceiverParameter!!,
                    argumentExpression = if (callSite.extensionReceiver != null) {
                        callSite.extensionReceiver!!
                    } else {
                        // Special case: lambda with receiver is called as usual lambda:
                        valueArguments.removeAt(0)
                    }
                )
            } else if (callSite.extensionReceiver != null) {
                // Special case: usual lambda is called as lambda with receiver:
                valueArguments.add(0, callSite.extensionReceiver!!)
            }

            val parametersWithDefaultToArgument = mutableListOf<ParameterToArgument>()
            for ((parameter, argument) in callee.valueParameters zip valueArguments) {
                when {
                    argument !is BirNoExpression -> {
                        parameterToArgument += ParameterToArgument(
                            parameter = parameter,
                            argumentExpression = argument
                        )
                    }

                    // After ExpectDeclarationsRemoving pass default values from expect declarations
                    // are represented correctly in BIR.
                    parameter.defaultValue != null -> {  // There is no argument - try default value.
                        parametersWithDefaultToArgument += ParameterToArgument(
                            parameter = parameter,
                            argumentExpression = parameter.defaultValue!!.expression,
                            isDefaultArg = true
                        )
                    }

                    parameter.varargElementType != null -> {
                        val emptyArray = BirVarargImpl(
                            sourceSpan = callSite.sourceSpan,
                            type = parameter.type,
                            varargElementType = parameter.varargElementType!!
                        )
                        parameterToArgument += ParameterToArgument(
                            parameter = parameter,
                            argumentExpression = emptyArray
                        )
                    }

                    else -> error("Incomplete expression: call to ${callee.render()} has no argument at index ${parameter.getIndex()}")
                }
            }
            // All arguments except default are evaluated at callsite,
            // but default arguments are evaluated inside callee.
            return parameterToArgument + parametersWithDefaultToArgument
        }

        //-------------------------------------------------------------------------//

        private fun evaluateArguments(callSite: BirFunctionAccessExpression, callee: BirFunction): List<BirStatement> {
            val arguments = buildParameterToArgument(callSite, callee)
            val evaluationStatements = mutableListOf<BirVariable>()
            val evaluationStatementsFromDefault = mutableListOf<BirVariable>()
            arguments.forEach { argument ->
                val parameter = argument.parameter
                /*
                 * We need to create temporary variable for each argument except inlinable lambda arguments.
                 * For simplicity and to produce simpler IR we don't create temporaries for every immutable variable,
                 * not only for those referring to inlinable lambdas.
                 */
                if (argument.isInlinableLambdaArgument || argument.isInlinablePropertyReference) {
                    substituteMap[parameter] = argument.argumentExpression
                    val arg = argument.argumentExpression
                    when {
                        // This first branch is required to avoid assertion in `getArgumentsWithBir`
                        arg is BirPropertyReference && arg.field != null -> evaluateReceiverForPropertyWithField(arg)?.let { evaluationStatements += it }
                        arg is BirCallableReference<*> -> evaluationStatements += evaluateArguments(arg)
                        arg is BirBlock -> if (arg.origin == IrStatementOrigin.ADAPTED_FUNCTION_REFERENCE) {
                            evaluationStatements += evaluateArguments(arg.statements.last() as BirFunctionReference)
                        }
                    }

                    return@forEach
                }

                // Arguments may reference the previous ones - substitute them.
                val variableInitializer = argument.argumentExpression.substituteParameters() as BirExpression
                val shouldCreateTemporaryVariable =
                    (alwaysCreateTemporaryVariablesForArguments && !parameter.isInlineParameter()) ||
                            argument.shouldBeSubstitutedViaTemporaryVariable()

                if (shouldCreateTemporaryVariable) {
                    val newVariable = createTemporaryVariable(
                        parameter,
                        variableInitializer.deepCopy(copier), // nb: new copy,
                        argument.isDefaultArg,
                        callee
                    )
                    if (argument.isDefaultArg) evaluationStatementsFromDefault.add(newVariable) else evaluationStatements.add(newVariable)
                    substituteMap[parameter] = BirGetValueImpl(NO_LOCATION_YET, newVariable.type, newVariable, null)
                    return@forEach
                }

                substituteMap[parameter] = if (variableInitializer is BirGetValue) {
                    val value = variableInitializer.target
                    BirGetValueImpl(NO_LOCATION_YET, value.type, value, null)
                } else {
                    variableInitializer
                }
            }


            // Next two composite blocks are used just as containers for two types of variables.
            // First one store temp variables that represent non default arguments of inline call and second one store defaults.
            // This is needed because these two groups of variables need slightly different processing on (JVM) backend.
            val blockForNewStatements = BirCompositeImpl(
                SourceSpan.UNDEFINED, birBuiltIns.unitType,
                INLINED_FUNCTION_ARGUMENTS
            ).apply {
                statements += evaluationStatements
            }

            val blockForNewStatementsFromDefault = BirCompositeImpl(
                SourceSpan.UNDEFINED, birBuiltIns.unitType,
                INLINED_FUNCTION_DEFAULT_ARGUMENTS
            ).apply {
                statements += evaluationStatementsFromDefault
            }

            return listOfNotNull(
                blockForNewStatements.takeIf { evaluationStatements.isNotEmpty() },
                blockForNewStatementsFromDefault.takeIf { evaluationStatementsFromDefault.isNotEmpty() }
            )
        }

        private fun evaluateReceiverForPropertyWithField(reference: BirPropertyReference): BirVariable? {
            val argument = reference.dispatchReceiver ?: reference.extensionReceiver ?: return null
            // Arguments may reference the previous ones - substitute them.
            val expression = argument.substituteParameters() as BirExpression

            val newVariable = BirVariable.build {
                name = Name.identifier(currentScope.inventNameForTemporary(nameHint = callee.name.asStringStripSpecialMarkers() + "_this"))
                sourceSpan = SourceSpan.UNDEFINED
                type = expression.type
                initializer = expression
                origin = IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
                isVar = false
            }

            val newArgument = BirGetValueImpl(NO_LOCATION_YET, newVariable.type, newVariable, null)
            when {
                reference.dispatchReceiver != null -> reference.dispatchReceiver = newArgument
                reference.extensionReceiver != null -> reference.extensionReceiver = newArgument
            }

            return newVariable
        }

        private val BirFunction.originalFunction: BirFunction
            get() = (this as? BirAttributeContainer)?.attributeOwnerId as? BirFunction ?: this

        private fun BirValueParameter.getOriginalParameter(): BirValueParameter {
            if (this.parent !is BirFunction) return this
            val original = (this.parent as BirFunction).originalFunction
            return original.allParameters.singleOrNull { it.name == this.name && it.sourceSpan == this.sourceSpan } ?: this
        }

        // In short this is needed for `kt44429` test. We need to get original generic type to trick type system on JVM backend.
        // Probably this it is relevant only for numeric types in JVM.
        private fun BirValueParameter.getOriginalType(): BirType {
            if (this.parent !is BirFunction) return type
            val copy = this.parent as BirFunction // contains substituted type parameters with corresponding type arguments
            val original = copy.originalFunction // contains original unsubstituted type parameters

            // Note 1: the following method will replace super types fow the owner type parameter. So in every other BirSimpleType that
            // refers this type parameter we will see substituted values. This should not be a problem because earlier we replace all type
            // parameters with corresponding type arguments.
            // Note 2: this substitution can be dropped if we will learn how to copy Bir function and leave its type parameters as they are.
            // But this sounds a little complicated.
            fun BirType.substituteSuperTypes(): BirType {
                val typeClassifier = this.classifierOrNull as? BirTypeParameter ?: return this
                typeClassifier.superTypes = original.typeParameters.elementAt(typeClassifier.getIndex()).superTypes.map {
                    val superTypeClassifier = it.classifierOrNull as? BirTypeParameter ?: return@map it
                    copy.typeParameters.elementAt(superTypeClassifier.getIndex()).defaultType.substituteSuperTypes()
                }
                return this
            }

            fun BirValueParameter?.getTypeIfFromTypeParameter(): BirType? {
                val typeClassifier = this?.type?.classifierOrNull as? BirTypeParameter ?: return null
                if (typeClassifier.parent != this.parent) return null

                // We take type parameter from copied callee and not from original because we need an actual copy. Without this copy,
                // in case of recursive call, we can get a situation there the same type parameter will be mapped on different type arguments.
                // (see compiler/testData/codegen/boxInline/complex/use.kt test file)
                return copy.typeParameters.elementAt(typeClassifier.getIndex()).defaultType.substituteSuperTypes()
            }

            return when (this) {
                copy.dispatchReceiverParameter -> original.dispatchReceiverParameter?.getTypeIfFromTypeParameter()
                    ?: copy.dispatchReceiverParameter!!.type
                copy.extensionReceiverParameter -> original.extensionReceiverParameter?.getTypeIfFromTypeParameter()
                    ?: copy.extensionReceiverParameter!!.type
                else -> copy.valueParameters.first { it == this }.let { valueParameter ->
                    original.valueParameters.elementAt(valueParameter.getIndex()).getTypeIfFromTypeParameter()
                        ?: valueParameter.type
                }
            }
        }

        private fun evaluateArguments(reference: BirCallableReference<*>): List<BirVariable> {
            val arguments = reference.getArgumentsWithBir().map { ParameterToArgument(it.first, it.second) }
            val evaluationStatements = mutableListOf<BirVariable>()
            val referenced = when (reference) {
                is BirFunctionReference -> reference.target as BirFunction
                is BirPropertyReference -> reference.getter!!.asElement
                else -> error(this)
            }
            arguments.forEach {
                // Arguments may reference the previous ones - substitute them.
                val expression = (it.argumentExpression.substituteParameters() as BirExpression)
                val newArgument = if (it.isImmutableVariableLoad) {
                    val value = (expression as BirGetValue).target
                    BirGetValueImpl(NO_LOCATION_YET, value.type, value, null)
                } else {
                    val newVariable = BirVariable.build {
                        sourceSpan = if (it.isDefaultArg) expression.sourceSpan else SourceSpan.UNDEFINED
                        setTemporary(callee.name.asStringStripSpecialMarkers() + "_" + it.parameter.name.asStringStripSpecialMarkers())
                        type = if (inlineArgumentsWithTheirOriginalTypeAndOffset) it.parameter.getOriginalType() else expression.type
                        isVar = false
                        initializer = expression.deepCopy(copier) // nb: new copy
                    }

                    evaluationStatements.add(newVariable)

                    BirGetValueImpl(NO_LOCATION_YET, newVariable.type, newVariable, null)
                }
                when (it.parameter) {
                    referenced.dispatchReceiverParameter -> reference.dispatchReceiver = newArgument
                    referenced.extensionReceiverParameter -> reference.extensionReceiver = newArgument
                    else -> reference.valueArguments.setElementAt(it.parameter.getIndex(), newArgument)
                }
            }
            return evaluationStatements
        }

        private fun ParameterToArgument.shouldBeSubstitutedViaTemporaryVariable(): Boolean =
            !(isImmutableVariableLoad && parameter.getIndex() >= 0) &&
                    !(argumentExpression.isPure(false) && inlinePureArguments)

        private fun createTemporaryVariable(
            parameter: BirValueParameter,
            variableInitializer: BirExpression,
            isDefaultArg: Boolean,
            callee: BirFunction
        ): BirVariable {
            return BirVariable.build {
                name = if (alwaysCreateTemporaryVariablesForArguments) {
                    parameter.name
                } else {
                    Name.identifier(currentScope.inventNameForTemporary(nameHint = callee.name.asStringStripSpecialMarkers()))
                }
                origin = if (parameter == callee.extensionReceiverParameter) {
                    IrDeclarationOrigin.IR_TEMPORARY_VARIABLE_FOR_INLINED_EXTENSION_RECEIVER
                } else {
                    IrDeclarationOrigin.IR_TEMPORARY_VARIABLE_FOR_INLINED_PARAMETER
                }
                type = variableInitializer.type
                initializer = BirBlockImpl(
                    if (isDefaultArg) variableInitializer.sourceSpan else SourceSpan.UNDEFINED,
                    if (inlineArgumentsWithTheirOriginalTypeAndOffset) parameter.getOriginalType() else variableInitializer.type,
                    InlinerExpressionLocationHint(currentScope.scopeOwnerSymbol)
                ).apply {
                    statements.add(variableInitializer)
                }
            }
        }
    }

    companion object {
        private val NO_LOCATION_YET = SourceSpan(-3L)
    }
}

context (BirTreeContext)
private class BirTreeDeepCopierForInliner(
    private val typeArgumentsMap: Map<BirTypeParameter, BirType>,
) : BirTreeDeepCopier() {
    override fun copyTypeOperatorCall(old: BirTypeOperatorCall): BirTypeOperatorCall {
        val new = super.copyTypeOperatorCall(old)
        new.type = remapTypeAndOptionallyErase(old.type, erase = true)
        new.typeOperand = remapTypeAndOptionallyErase(old.typeOperand, erase = true)
        return new
    }

    override fun <S : BirSymbol> remapSymbol(old: S): S {
        val result = super.remapSymbol(old)
        if (result is BirTypeParameterSymbol) {
            assert(result is BirTypeParameter)
            return typeArgumentsMap[result]?.classifierOrNull as S? ?: result
        }
        return result
    }

    override fun remapType(old: BirType) = remapTypeAndOptionallyErase(old, erase = false)

    fun remapTypeAndOptionallyErase(type: BirType, erase: Boolean): BirType {
        val erasedParams = if (erase) mutableSetOf<BirTypeParameterSymbol>() else null
        return remapTypeAndOptionallyErase(type, erasedParams) ?: error("Cannot substitute type ${type.render()}")
    }

    private fun remapTypeAndOptionallyErase(type: BirType, erasedParameters: MutableSet<BirTypeParameterSymbol>?): BirType? {
        if (type !is BirSimpleType) return type

        val classifier = type.classifier
        val substitutedType = typeArgumentsMap[classifier]

        // Erase non-reified type parameter if asked to.
        if (erasedParameters != null && substitutedType != null && (classifier as? BirTypeParameterSymbol)?.asElement?.isReified == false) {
            if (classifier in erasedParameters) {
                return null
            }

            erasedParameters.add(classifier)

            // Pick the (necessarily unique) non-interface upper bound if it exists.
            val superTypes = classifier.asElement.superTypes
            val superClass = superTypes.firstOrNull {
                it.classOrNull?.asElement?.kind.let { it != null && it != ClassKind.INTERFACE }
            }

            val upperBound = superClass ?: superTypes.first()

            // TODO: Think about how to reduce complexity from k^N to N^k
            val erasedUpperBound = remapTypeAndOptionallyErase(upperBound, erasedParameters)
                ?: error("Cannot erase upperbound ${upperBound.render()}")

            erasedParameters.remove(classifier)

            return erasedUpperBound.mergeNullability(type)
        }

        if (substitutedType is BirDynamicType) {
            return substitutedType
        }

        if (substitutedType is BirSimpleType) {
            return substitutedType.mergeNullability(type)
        }

        val newClassifier = remapSymbol(classifier)
        val newArguments = remapTypeArguments(type.arguments, erasedParameters)
        return if (newClassifier === classifier && newArguments === type.arguments) {
            type
        } else BirSimpleTypeImpl(
            kotlinType = null,
            classifier = newClassifier,
            arguments = newArguments,
            annotations = type.annotations.memoryOptimizedMap { copyElement(it) },
            nullability = SimpleTypeNullability.NOT_SPECIFIED,
            abbreviation = null,
        )
    }

    private fun remapTypeArguments(
        arguments: List<BirTypeArgument>,
        erasedParameters: MutableSet<BirTypeParameterSymbol>?
    ): List<BirTypeArgument> = arguments.mapOrTakeThisIfIdentity { argument ->
        if (argument is BirTypeProjection) {
            remapTypeAndOptionallyErase(argument.type, erasedParameters)?.let { newType ->
                makeTypeProjection(newType, argument.variance)
            } ?: BirStarProjection
        } else {
            argument
        }
    }
}

class InlinerExpressionLocationHint(val inlineAtElement: BirSymbol) : IrStatementOrigin {
    override fun toString(): String =
        "(${this.javaClass.simpleName} : $functionNameOrDefaultToString @${functionFileOrNull?.fileEntry?.name})"

    private val functionFileOrNull: BirFile?
        get() = (inlineAtElement as? BirFunction)?.ancestors()?.firstIsInstanceOrNull<BirFile>()

    private val functionNameOrDefaultToString: String
        get() = (inlineAtElement as? BirFunction)?.name?.asString() ?: inlineAtElement.toString()
}
