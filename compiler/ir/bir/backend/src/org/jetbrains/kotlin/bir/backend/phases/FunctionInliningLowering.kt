/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.backend.common.lower.inline.INLINED_FUNCTION_ARGUMENTS
import org.jetbrains.kotlin.backend.common.lower.inline.INLINED_FUNCTION_DEFAULT_ARGUMENTS
import org.jetbrains.kotlin.backend.common.lower.inline.INLINED_FUNCTION_REFERENCE
import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.backend.BirBackendContext
import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.InnerClassesSupport
import org.jetbrains.kotlin.bir.backend.utils.implicitCastIfNeededTo
import org.jetbrains.kotlin.bir.backend.utils.int
import org.jetbrains.kotlin.bir.backend.utils.isPure
import org.jetbrains.kotlin.bir.backend.utils.isTypeOfIntrinsic
import org.jetbrains.kotlin.bir.builders.BirNoExpression
import org.jetbrains.kotlin.bir.builders.build
import org.jetbrains.kotlin.bir.builders.setTemporary
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.expressions.impl.*
import org.jetbrains.kotlin.bir.symbols.*
import org.jetbrains.kotlin.bir.types.*
import org.jetbrains.kotlin.bir.types.utils.*
import org.jetbrains.kotlin.bir.utils.*
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassKind
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
class FunctionInliningLowering(
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
                Inliner(call.callSite, call.callee, TemporaryVariablesScope(scope as BirSymbolElement)).inline()
                call.alreadyInlined = true
            }
        }

        for (call in allCallsToInline) {
            tryToInline(call)
        }
    }

    private val BirFunction.needsInlining get() = this.isInline && (allowExternalInlining || !this.isExternal)

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private inner class Inliner(
        private val callSite: BirFunctionAccessExpression,
        private val callee: BirFunction,
        private val currentScope: TemporaryVariablesScope,
    ) : BirTreeDeepCopier() {
        private val typeArgumentsMap: MutableMap<BirTypeParameter, BirType>
        val substituteMap = hashMapOf<BirValueParameter, ParameterToArgument>()
        var currentlyInlinedNewCallSite: BirReturnableBlock? = null
        var currentlyInlinedOldCallSite: BirFunctionAccessExpression? = null
        var currentlyInlinedCallee: BirFunction? = null

        init {
            val typeParameters = if (callee is BirConstructor)
                callee.parentAsClass.typeParameters
            else callee.typeParameters

            @Suppress("UNCHECKED_CAST")
            typeArgumentsMap = (typeParameters zip callSite.typeArguments)
                .filter { it.second != null }
                .toMap().toMutableMap() as MutableMap<BirTypeParameter, BirType>
        }

        fun inline() {
            rootElement = callee.body
            inlineFunction(callSite, callee, inlineFunctionResolver.getFunctionSymbol(callee) as BirFunction, isTopLevel = true) {
                callSite.replaceWith(it)
            }
            rootElement = null
        }

        private fun <R> inlineInScopeOfFunction(
            oldInlinedCallSite: BirFunctionAccessExpression?,
            callee: BirFunction?,
            newInlinedCallSite: BirReturnableBlock?,
            block: () -> R,
        ): R {
            val parentInlinedOldCallSite = currentlyInlinedOldCallSite
            val parentInlinedNewCallSite = currentlyInlinedNewCallSite
            val parentInlinedCallee = currentlyInlinedCallee
            currentlyInlinedOldCallSite = oldInlinedCallSite
            currentlyInlinedNewCallSite = newInlinedCallSite
            currentlyInlinedCallee = callee

            val result = block()

            currentlyInlinedOldCallSite = parentInlinedOldCallSite
            currentlyInlinedNewCallSite = parentInlinedNewCallSite
            currentlyInlinedCallee = parentInlinedCallee

            return result
        }

        fun inlineFunction(
            callSite: BirFunctionAccessExpression,
            callee: BirFunction,
            originalInlinedElement: BirElement,
            isTopLevel: Boolean = false,
            replaceWithInlined: (BirReturnableBlock) -> Unit = {},
        ): BirReturnableBlock {
            val (innerInlineBlock, outerInlineBlock) = createInlinedCallStructure(callSite, originalInlinedElement)

            inlineInScopeOfFunction(callSite, callee, outerInlineBlock) {
                val evaluationStatements = evaluateArguments(callSite, callee)

                replaceWithInlined(outerInlineBlock)

                val sourceBody = callee.body as? BirBlockBody
                    ?: error("Body not found for function ${callee.render()}")
                innerInlineBlock.statements.addAll(evaluationStatements)
                for (statement in sourceBody.statements) {
                    innerInlineBlock.statements += copyElementPossiblyUnfinished(statement)
                }
                ensureLastElementIsFinished()
            }

            return outerInlineBlock
        }

        private fun createInlinedCallStructure(
            originalCallSite: BirFunctionAccessExpression,
            originalInlinedElement: BirElement,
        ): Pair<BirInlinedFunctionBlock, BirReturnableBlockImpl> {
            val outerInlineBlock = BirReturnableBlockImpl(
                sourceSpan = originalCallSite.sourceSpan,
                type = originalCallSite.type,
                origin = null,
                _descriptor = null
            )

            // Note: here we wrap `BirInlinedFunctionBlock` inside `BirReturnableBlock` because such way it is easier to
            // control special composite blocks that are inside `BirInlinedFunctionBlock`
            val innerInlineBlock = BirInlinedFunctionBlockImpl(
                sourceSpan = originalCallSite.sourceSpan,
                type = originalCallSite.type,
                inlineCall = originalCallSite,
                inlinedElement = originalInlinedElement,
                origin = null,
            )
            outerInlineBlock.statements += innerInlineBlock

            return innerInlineBlock to outerInlineBlock
        }

        private inner class ParameterToArgument(
            val parameter: BirValueParameter,
            val argumentExpression: BirExpression,
            val isDefaultArg: Boolean = false,
        ) {
            var storedResult: BirValueDeclaration? = null

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

                    else -> {
                        //error("Incomplete expression: call to ${callee.render()} has no argument at index ${parameter.getIndex()}")
                        parameterToArgument += ParameterToArgument(
                            parameter = parameter,
                            argumentExpression = BirConst.int(value = 123456789)
                        )
                    }
                }
            }
            // All arguments except default are evaluated at callsite,
            // but default arguments are evaluated inside callee.
            return parameterToArgument + parametersWithDefaultToArgument
        }

        //-------------------------------------------------------------------------//

        private fun evaluateArguments(callSite: BirFunctionAccessExpression, callee: BirFunction): List<BirStatement> {
            val arguments = buildParameterToArgument(callSite, callee)
            arguments.associateByTo(substituteMap) { it.parameter }

            val evaluationStatements = mutableListOf<BirVariable>()
            val evaluationStatementsFromDefault = mutableListOf<BirVariable>()
            for (argument in arguments) {
                val parameter = argument.parameter
                /*
                 * We need to create temporary variable for each argument except inlinable lambda arguments.
                 * For simplicity and to produce simpler IR we don't create temporaries for every immutable variable,
                 * not only for those referring to inlinable lambdas.
                 */
                if (argument.isInlinableLambdaArgument || argument.isInlinablePropertyReference) {
                    val arg = argument.argumentExpression
                    when {
                        // This first branch is required to avoid assertion in `getArgumentsWithBir`
                        arg is BirPropertyReference && arg.field != null -> {
                            evaluateReceiverForPropertyWithField(arg)?.let {
                                evaluationStatements += it
                            }
                        }
                        arg is BirCallableReference<*> -> {
                            evaluationStatements += evaluateArguments(arg)
                        }
                        arg is BirBlock -> {
                            if (arg.origin == IrStatementOrigin.ADAPTED_FUNCTION_REFERENCE) {
                                evaluationStatements += evaluateArguments(arg.statements.last() as BirFunctionReference)
                            }
                        }
                    }
                } else {
                    val variableInitializer = argument.argumentExpression
                    val shouldCreateTemporaryVariable =
                        (alwaysCreateTemporaryVariablesForArguments && !parameter.isInlineParameter()) ||
                                argument.shouldBeSubstitutedViaTemporaryVariable()

                    if (shouldCreateTemporaryVariable) {
                        val newVariable = createTemporaryVariable(
                            parameter,
                            variableInitializer.also { it.replaceWith(BirNoExpression()) },
                            argument.isDefaultArg,
                            callee
                        )
                        if (argument.isDefaultArg) evaluationStatementsFromDefault.add(newVariable) else evaluationStatements.add(
                            newVariable
                        )
                        argument.storedResult = newVariable
                    } else {
                        if (variableInitializer is BirGetValue) {
                            argument.storedResult = variableInitializer.target
                        }
                    }
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
            val newVariable = BirVariable.build {
                name = Name.identifier(currentScope.inventNameForTemporary(nameHint = callee.name.asStringStripSpecialMarkers() + "_this"))
                sourceSpan = SourceSpan.UNDEFINED
                type = argument.type
                initializer = argument
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
                val expression = it.argumentExpression
                val newArgument = if (it.isImmutableVariableLoad) {
                    val value = (expression as BirGetValue).target
                    BirGetValueImpl(NO_LOCATION_YET, value.type, value, null)
                } else {
                    val newVariable = BirVariable.build {
                        sourceSpan = if (it.isDefaultArg) expression.sourceSpan else SourceSpan.UNDEFINED
                        setTemporary(callee.name.asStringStripSpecialMarkers() + "_" + it.parameter.name.asStringStripSpecialMarkers())
                        type = if (inlineArgumentsWithTheirOriginalTypeAndOffset) it.parameter.getOriginalType() else expression.type
                        isVar = false
                        initializer = expression.also { expression.replaceWith(BirNoExpression()) }
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
            callee: BirFunction,
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

        private fun BirExpression.doImplicitCastIfNeededTo(type: BirType): BirExpression {
            if (!insertAdditionalImplicitCasts) return this
            return this.implicitCastIfNeededTo(type)
        }

        //-------------------------------------------------------------------------//

        override fun copyGetValue(old: BirGetValue): BirElement {
            val argument = substituteMap[old.target]
            return if (argument != null) {
                val value = argument.storedResult?.let {
                    BirGetValueImpl(old.sourceSpan, it.type, it, null)
                } ?: copyElementPossiblyUnfinished(argument.argumentExpression)
                value.doImplicitCastIfNeededTo(remapType(old.type)) // maybe also need to apply the custom copyTypeOperatorCall
            } else {
                super.copyGetValue(old)
            }
        }

        override fun copyCall(old: BirCall): BirElement {
            if (isLambdaCall(old)) {
                val dispatchReceiver = old.dispatchReceiver?.unwrapAdditionalImplicitCastsIfNeeded() as BirGetValue
                substituteMap[dispatchReceiver.target]?.let { argument ->
                    val functionArgument = argument.argumentExpression
                    if ((dispatchReceiver.target as? BirValueParameter)?.isNoinline != true) {
                        return when {
                            functionArgument is BirFunctionReference ->
                                inlineFunctionReference(old, functionArgument, functionArgument.target as BirFunction)

                            functionArgument is BirPropertyReference && functionArgument.field != null ->
                                inlineField(old, functionArgument)

                            functionArgument is BirPropertyReference -> inlinePropertyReference(old, functionArgument)

                            functionArgument.isAdaptedFunctionReference() ->
                                inlineAdaptedFunctionReference(old, functionArgument as BirBlock)

                            functionArgument is BirFunctionExpression ->
                                inlineFunctionExpression(old, functionArgument)

                            else -> super.copyCall(old)
                        }
                    }
                }
            }

            return super.copyCall(old)
        }

        override fun copyReturn(old: BirReturn): BirElement {
            val new = super.copyReturn(old) as BirReturn
            if (old.returnTarget == currentlyInlinedCallee) {
                new.returnTarget = currentlyInlinedNewCallSite!!
                new.type = birBuiltIns.nothingType
                new.value = new.value
                    .also { new.value = BirNoExpression() }
                    .doImplicitCastIfNeededTo(remapType(currentlyInlinedOldCallSite!!.type))
            }
            return new
        }

        override fun copyTypeOperatorCall(old: BirTypeOperatorCall): BirElement {
            val new = super.copyTypeOperatorCall(old) as BirTypeOperatorCall
            new.type = remapTypeAndOptionallyErase(old.type, erase = true)
            new.typeOperand = remapTypeAndOptionallyErase(old.typeOperand, erase = true)
            return new
        }

        //---------------------------------------------------------------------//

        private fun inlineFunctionExpression(call: BirCall, functionExpression: BirFunctionExpression): BirExpression {
            // Inline the lambda. Lambda parameters will be substituted with lambda arguments.
            return inlineFunction(call, functionExpression.function, functionExpression)
        }

        private fun inlineField(invokeCall: BirCall, propertyReference: BirPropertyReference): BirExpression {
            val field = remapElement(propertyReference.field!!.asElement)

            val boundReceiver = propertyReference.dispatchReceiver ?: propertyReference.extensionReceiver
            val fieldReceiver = if (field.isStatic) null
            else boundReceiver?.let { remapElement(it) }

            val (innerInlineBlock, outerInlineBlock) = createInlinedCallStructure(invokeCall, propertyReference)
            innerInlineBlock.statements += BirGetFieldImpl(SourceSpan.UNDEFINED, field.type, field, null, fieldReceiver, null)
            return outerInlineBlock
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
                return expression.valueArguments.elementAt(i).takeUnless { it is BirNoExpression }
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

            val (innerInlineBlock, outerInlineBlock) = createInlinedCallStructure(expression, propertyReference)
            innerInlineBlock.statements += BirReturnImpl(SourceSpan.UNDEFINED, birBuiltIns.nothingType, getterCall, outerInlineBlock)
            return outerInlineBlock
        }

        private fun inlineAdaptedFunctionReference(originalCall: BirCall, irBlock: BirBlock): BirExpression {
            val irFunction = irBlock.statements.first() as BirFunction
            val irFunctionReference = irBlock.statements.elementAt(1) as BirFunctionReference
            val inlinedFunctionReference = inlineFunctionReference(originalCall, irFunctionReference, irFunction)
            return BirBlockImpl(
                originalCall.sourceSpan,
                inlinedFunctionReference.type,
                origin = null,
            ).apply {
                statements += irFunction
                statements += inlinedFunctionReference
            }
        }

        private fun inlineFunctionReference(
            originalCall: BirCall,
            functionReference: BirFunctionReference,
            inlinedFunction: BirFunction,
        ): BirExpression {
            val function = functionReference.target as BirFunction
            val functionParameters = function.explicitParameters
            val boundFunctionParameters = functionReference.getArgumentsWithBir()
            val boundFunctionParametersMap = boundFunctionParameters.associate { it.first to it.second }
            val unboundFunctionParameters = functionParameters - boundFunctionParametersMap.keys

            var unboundIndex = 0
            val unboundArgsSet = unboundFunctionParameters.toSet()
            val valueParameters = originalCall.getArgumentsWithBir().drop(1) // Skip dispatch receiver.

            val superType = functionReference.type as BirSimpleType
            val superTypeArgumentsMap = (originalCall.target.asElement.parentAsClass.typeParameters zip superType.arguments)
                .associate<_, BirTypeParameterSymbol, BirType> { it.first to it.second.typeOrNull!! }

            val immediateCall = when (inlinedFunction) {
                is BirConstructor -> {
                    val classTypeParametersCount = inlinedFunction.parentAsClass.typeParameters.size
                    BirConstructorCall.build {
                        sourceSpan =
                            if (inlineArgumentsWithTheirOriginalTypeAndOffset) functionReference.sourceSpan else originalCall.sourceSpan
                        type = inlinedFunction.returnType
                        target = inlinedFunction
                        constructorTypeArgumentsCount = classTypeParametersCount
                        origin = INLINED_FUNCTION_REFERENCE
                    }
                }
                is BirSimpleFunction ->
                    BirCall.build {
                        sourceSpan =
                            if (inlineArgumentsWithTheirOriginalTypeAndOffset) functionReference.sourceSpan else originalCall.sourceSpan
                        type = inlinedFunction.returnType
                        target = inlinedFunction
                        origin = INLINED_FUNCTION_REFERENCE
                    }
                else -> error("Unknown function kind : ${inlinedFunction.render()}")
            }.apply {
                for (parameter in functionParameters) {
                    val originalArgument = if (parameter !in unboundArgsSet) {
                        boundFunctionParametersMap[parameter]!!
                    } else {
                        if (unboundIndex == valueParameters.size && parameter.defaultValue != null) {
                            parameter.defaultValue!!.expression
                        } else if (!parameter.isVararg) {
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
                                    elements += BirSpreadElementImpl(originalCall.sourceSpan, value)
                            }
                            BirVarargImpl(
                                originalCall.sourceSpan,
                                parameter.type,
                                parameter.varargElementType!!,
                            ).also {
                                it.elements += elements
                            }
                        }
                    }

                    val argument = copyElement(originalArgument)
                    when (parameter) {
                        function.dispatchReceiverParameter ->
                            this.dispatchReceiver =
                                argument.doImplicitCastIfNeededTo(remapType(inlinedFunction.dispatchReceiverParameter!!.type))

                        function.extensionReceiverParameter ->
                            this.extensionReceiver =
                                argument.doImplicitCastIfNeededTo(remapType(inlinedFunction.extensionReceiverParameter!!.type))

                        else -> {
                            val i = parameter.getIndex()
                            valueArguments.add(
                                argument.doImplicitCastIfNeededTo(remapType(inlinedFunction.valueParameters.elementAt(i).type))
                            )
                        }
                    }
                }
                assert(unboundIndex == valueParameters.size) { "Not all arguments of the callee are used" }
                typeArguments = functionReference.typeArguments
            }

            return if (inlinedFunction.needsInlining && inlinedFunction.body != null) {
                inlineFunction(immediateCall, inlinedFunction, functionReference)
            } else {
                val (innerInlineBlock, outerInlineBlock) = createInlinedCallStructure(originalCall, functionReference)
                innerInlineBlock.statements += BirReturnImpl(SourceSpan.UNDEFINED, birBuiltIns.nothingType, immediateCall, outerInlineBlock)
                outerInlineBlock
            }.doImplicitCastIfNeededTo(remapType(originalCall.type))
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

        // With `insertAdditionalImplicitCasts` flag we sometimes insert
        // casts to inline lambda parameters before calling `invoke` on them.
        // Unwrapping these casts helps us satisfy inline lambda call detection logic.
        private fun BirExpression.unwrapAdditionalImplicitCastsIfNeeded(): BirExpression {
            if (insertAdditionalImplicitCasts && this is BirTypeOperatorCall && this.operator == IrTypeOperator.IMPLICIT_CAST) {
                return this.argument.unwrapAdditionalImplicitCastsIfNeeded()
            }
            return this
        }

        //---------------------------------------------------------------------//

        override fun <S : BirSymbol> remapSymbol(old: S): S {
            if (old is BirTypeParameter) {
                typeArgumentsMap[old]?.let { newType ->
                    newType.classifierOrNull?.let { newClassifier ->
                        val new = super.remapSymbol(old)
                        typeArgumentsMap[new] = newType
                        return newClassifier as S
                    }
                }
            }
            return super.remapSymbol(old)
        }

        override fun BirAttributeContainer.copyAttributes(other: BirAttributeContainer) {
            if (regenerateInlinedAnonymousObjects) {
                this[GlobalBirElementAuxStorageTokens.OriginalBeforeInline] = other.attributeOwnerId
                attributeOwnerId = this
            } else {
                attributeOwnerId = other.attributeOwnerId
            }
        }

        override fun remapType(old: BirType) = remapTypeAndOptionallyErase(old, erase = false)

        fun remapTypeAndOptionallyErase(type: BirType, erase: Boolean): BirType {
            val erasedParams = if (erase) hashSetOf<BirTypeParameterSymbol>() else null
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
            erasedParameters: MutableSet<BirTypeParameterSymbol>?,
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

    companion object {
        private val NO_LOCATION_YET = SourceSpan(-3L)
    }
}

// This implementation isn't the most sensible, it just mimics org.jetbrains.kotlin.backend.common.lower.inline.InlinerExpressionLocationHint
class InlinerExpressionLocationHint(val inlineAtElement: BirSymbol) : IrStatementOrigin {
    override fun toString(): String =
        "(${this.javaClass.simpleName} : $functionNameOrDefaultToString @${functionFileOrNull?.fileEntry?.name})"

    private val functionFileOrNull: BirFile?
        get() = null

    private val functionNameOrDefaultToString: String
        get() = (inlineAtElement as? BirElement)?.render() ?: inlineAtElement.toString()
}
