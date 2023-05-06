/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.backend.common.descriptors.synthesizedString
import org.jetbrains.kotlin.backend.common.lower.BOUND_RECEIVER_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.BOUND_VALUE_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.ConstructorDelegationKind
import org.jetbrains.kotlin.backend.common.lower.LoweredStatementOrigins
import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.builders.build
import org.jetbrains.kotlin.bir.builders.copyAttributes
import org.jetbrains.kotlin.bir.builders.copyFlagsFrom
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.expressions.impl.BirCompositeImpl
import org.jetbrains.kotlin.bir.expressions.impl.BirGetFieldImpl
import org.jetbrains.kotlin.bir.expressions.impl.BirGetValueImpl
import org.jetbrains.kotlin.bir.expressions.impl.BirSetFieldImpl
import org.jetbrains.kotlin.bir.symbols.BirSymbolElement
import org.jetbrains.kotlin.bir.symbols.BirValueSymbol
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.bir.utils.*
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import org.jetbrains.kotlin.utils.memoryOptimizedMap

interface VisibilityPolicy {
    fun forClass(declaration: BirClass, inInlineFunctionScope: Boolean): DescriptorVisibility =
        declaration.visibility

    fun forConstructor(declaration: BirConstructor, inInlineFunctionScope: Boolean): DescriptorVisibility =
        DescriptorVisibilities.PRIVATE

    fun forCapturedField(value: BirValueSymbol): DescriptorVisibility =
        DescriptorVisibilities.PRIVATE

    companion object {
        val DEFAULT = object : VisibilityPolicy {}
    }
}

context (WasmBirContext)
class LocalDeclarationsLowering(
    val localNameSanitizer: (String) -> String = { it },
    val visibilityPolicy: VisibilityPolicy = VisibilityPolicy.DEFAULT,
    val suggestUniqueNames: Boolean = true, // When `true` appends a `$#index` suffix to lifted declaration names
    val compatibilityModeForInlinedLocalDelegatedPropertyAccessors: Boolean = false, // Keep old names because of KT-49030
    val forceFieldsForInlineCaptures: Boolean = false, // See `LocalClassContext`
    private val postLocalDeclarationLoweringCallback: ((IntermediateDatastructures) -> Unit)? = null
) : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        getElementsOfClass<BirBody>().forEach {
            lower(it, it.ancestors().firstIsInstance<BirDeclaration>())
        }
    }

    fun lower(body: BirBody, container: BirDeclaration) {
        LocalDeclarationsTransformer(body, container).lowerLocalDeclarations()
    }

    fun lower(element: BirElement, container: BirDeclaration, classesToLower: Set<BirClass>) {
        LocalDeclarationsTransformer(element, container, null, classesToLower).lowerLocalDeclarations()
    }

    fun lower(
        block: BirBlock, container: BirDeclaration, closestParent: BirDeclarationHost,
        classesToLower: Set<BirClass>, functionsToSkip: Set<BirSimpleFunction>
    ) {
        LocalDeclarationsTransformer(block, container, closestParent, classesToLower, functionsToSkip).lowerLocalDeclarations()
    }

    private inner class LocalDeclarationsTransformer(
        val rootElement: BirElement,
        val container: BirDeclaration,
        val closestParent: BirDeclarationHost? = null,
        val classesToLower: Set<BirClass>? = null,
        val functionsToSkip: Set<BirSimpleFunction>? = null,
    ) {
        val localFunctions: MutableMap<BirFunction, LocalFunctionContext> = LinkedHashMap()
        val localClasses: MutableMap<BirClass, LocalClassContext> = LinkedHashMap()
        val localClassConstructors: MutableMap<BirConstructor, LocalClassConstructorContext> = LinkedHashMap()

        val transformedDeclarations = mutableMapOf<BirSymbolElement, BirDeclaration>()

        val BirFunction.transformed: BirFunction?
            get() = transformedDeclarations[this] as BirFunction?

        val newParameterToOld: MutableMap<BirValueParameter, BirValueParameter> = mutableMapOf()
        val oldParameterToNew: MutableMap<BirValueParameter, BirValueParameter> = mutableMapOf()
        val newParameterToCaptured: MutableMap<BirValueParameter, BirValueDeclaration> = mutableMapOf()

        fun lowerLocalDeclarations() {
            collectLocalDeclarations()
            if (localFunctions.isEmpty() && localClasses.isEmpty()) return
            collectClosureForLocalDeclarations()
            transformDeclarations()
            rewriteDeclarations()
            insertLoweredDeclarationForLocalFunctions()

            postLocalDeclarationLoweringCallback?.invoke(
                IntermediateDatastructures(localFunctions, newParameterToOld, newParameterToCaptured)
            )
        }

        private fun collectLocalDeclarations() {
            val enclosingClass = container.ancestors().firstIsInstanceOrNull<BirClass>()
            val enclosingPackageFragment = container.ancestors().firstIsInstance<BirPackageFragment>()

            class Scope(val currentClass: ScopeWithCounter?, val isInInlineFunction: Boolean) {
                fun withCurrentClass(currentClass: BirClass): Scope =
                    // Don't cache local declarations
                    Scope(ScopeWithCounter(currentClass), isInInlineFunction)

                fun withInline(isInline: Boolean): Scope =
                    if (isInline && !isInInlineFunction) Scope(currentClass, true) else this

                val inInlineFunctionScope: Boolean
                    get() = isInInlineFunction && container.ancestors().any { it is BirFunction && it.isInline }
            }

            rootElement.traverseStackBased(Scope(null, false)) { element, scope ->
                when (element) {
                    is BirInlinedFunctionBlock -> {
                        element.walkIntoChildren(scope.withInline(element.isFunctionInlining()))
                    }
                    is BirFunctionExpression -> {
                        // TODO: For now IrFunctionExpression can only be encountered here if this was called from the inliner,
                        // then all IrFunctionExpression will be replaced by IrFunctionReferenceExpression.
                        // Don't forget to fix this when that replacement has been dropped.
                        // Also, a note: even if a lambda is not an inline one, there still cannot be a reference to it
                        // from an outside declaration, so it is safe to skip them here and correctly handle later, after the above conversion.
                        element.function.walkIntoChildren(scope)
                    }
                    is BirSimpleFunction -> {
                        if (functionsToSkip?.contains(element) != true) {
                            element.walkIntoChildren(scope.withInline(element.isInline))

                            if (element.visibility == DescriptorVisibilities.LOCAL) {
                                val enclosingScope = scope.currentClass
                                    ?: enclosingClass?.scopeWithCounter
                                    // File is required for K/N because file elements are not split by classes.
                                    ?: enclosingPackageFragment.scopeWithCounter
                                val index =
                                    if (element.name.isSpecial || element.name in enclosingScope.usedLocalFunctionNames)
                                        enclosingScope.counter++
                                    else -1
                                val ownerForLoweredDeclaration =
                                    scope.currentClass?.let { OwnerForLoweredDeclaration.DeclarationContainer(it.element as BirDeclarationContainer) }
                                        ?: (rootElement as? BirBlock)?.let {
                                            OwnerForLoweredDeclaration.Block(it, closestParent!!)
                                        }
                                        ?: OwnerForLoweredDeclaration.DeclarationContainer(enclosingScope.element as BirDeclarationContainer)
                                localFunctions[element] =
                                    LocalFunctionContext(element, index, ownerForLoweredDeclaration)

                                enclosingScope.usedLocalFunctionNames.add(element.name)
                            }
                        }
                    }
                    is BirConstructor -> {
                        element.walkIntoChildren(scope)
                        if (element.constructedClass.isLocalNotInner()) {
                            localClassConstructors[element] = LocalClassConstructorContext(element, scope.inInlineFunctionScope)
                        }
                    }
                    is BirClass -> {
                        if (classesToLower?.contains(element) != false) {
                            element.walkIntoChildren(scope.withCurrentClass(element))

                            if (element.isLocalNotInner()) {
                                // If there are many non-delegating constructors, each copy of the initializer requires different remapping:
                                //   class C {
                                //     constructor() {}
                                //     constructor(x: Int) {}
                                //     val x = y // which constructor's parameter?
                                //   }
                                // TODO: this should ideally run after initializers are added to constructors, but that'd place
                                //   other restrictions on IR (e.g. after the initializers are moved you can no longer create fields
                                //   with initializers) which makes that hard to implement.
                                val constructorContext = element.constructors.mapNotNull { localClassConstructors[it] }
                                    .singleOrNull { it.declaration.determineDelegationKind() == ConstructorDelegationKind.CALLS_SUPER }
                                localClasses[element] = LocalClassContext(element, scope.inInlineFunctionScope, constructorContext)
                            }
                        }
                    }
                }
            }
        }

        private fun collectClosureForLocalDeclarations() {
            //TODO: maybe use for granular declarations
            val annotator = ClosureAnnotator(rootElement, container)

            localFunctions.forEach { (declaration, context) ->
                context.closure = annotator.getFunctionClosure(declaration)
            }

            localClasses.forEach { (declaration, context) ->
                context.closure = annotator.getClassClosure(declaration)
            }
        }

        private fun transformDeclarations() {
            localFunctions.values.forEach {
                createLiftedDeclaration(it)
            }

            localClasses.values.forEach {
                it.declaration.visibility = visibilityPolicy.forClass(it.declaration, it.inInlineFunctionScope)
                it.closure.capturedValues.associateWithTo(it.capturedValueToField) { capturedValue -> PotentiallyUnusedField(capturedValue) }
            }

            localClassConstructors.values.forEach {
                createTransformedConstructorDeclaration(it)
            }
        }


        private fun createLiftedDeclaration(localFunctionContext: LocalFunctionContext) {
            val oldDeclaration = localFunctionContext.declaration
            if (oldDeclaration.dispatchReceiverParameter != null) {
                throw AssertionError("local functions must not have dispatch receiver")
            }

            val owner = localFunctionContext.ownerForLoweredDeclaration
            val ownerParent = owner.closestDeclarationParent()
            val newName = generateNameForLiftedDeclaration(oldDeclaration, ownerParent)

            // TODO: consider using fields to access the closure of enclosing class.
            val (capturedValues, capturedTypeParameters) = localFunctionContext.closure

            val newDeclaration = BirSimpleFunction.build {
                sourceSpan = oldDeclaration.sourceSpan
                copyFlagsFrom(oldDeclaration)
                name = newName
                origin = oldDeclaration.origin
                visibility = if (owner.isLocal) DescriptorVisibilities.LOCAL else DescriptorVisibilities.PRIVATE
                modality = Modality.FINAL
            }

            localFunctionContext.transformedDeclaration = newDeclaration

            val newTypeParameters = newDeclaration.copyTypeParameters(capturedTypeParameters)
            localFunctionContext.capturedTypeParameterToTypeParameter.putAll(
                capturedTypeParameters zip newTypeParameters
            )
            newDeclaration.copyTypeParametersFrom(oldDeclaration, parameterMap = localFunctionContext.capturedTypeParameterToTypeParameter)
            localFunctionContext.capturedTypeParameterToTypeParameter.putAll(
                oldDeclaration.typeParameters zip newDeclaration.typeParameters.drop(newTypeParameters.size)
            )
            // Type parameters of oldDeclaration may depend on captured type parameters, so deal with that after copying.
            newDeclaration.typeParameters.drop(newTypeParameters.size).forEach { tp ->
                tp.superTypes = tp.superTypes.memoryOptimizedMap { localFunctionContext.remapType(it) }
            }

            newDeclaration.returnType = localFunctionContext.remapType(oldDeclaration.returnType)
            newDeclaration.dispatchReceiverParameter = null
            newDeclaration.extensionReceiverParameter = oldDeclaration.extensionReceiverParameter?.run {
                copyTo(newDeclaration, type = localFunctionContext.remapType(this.type)).also {
                    newParameterToOld.putAbsentOrSame(it, this)
                }
            }
            newDeclaration.copyAttributes(oldDeclaration)

            newDeclaration.valueParameters += createTransformedValueParameters(
                capturedValues, localFunctionContext, oldDeclaration, newDeclaration,
                isExplicitLocalFunction = oldDeclaration.origin == IrDeclarationOrigin.LOCAL_FUNCTION
            )
            newDeclaration.recordTransformedValueParameters(localFunctionContext)
            val parametersMapping = buildMap {
                oldDeclaration.extensionReceiverParameter?.let { put(it, newDeclaration.extensionReceiverParameter!!) }
                putAll(
                    oldDeclaration.valueParameters zip newDeclaration.valueParameters.toList().takeLast(oldDeclaration.valueParameters.size)
                )
            }
            //todo: context.remapMultiFieldValueClassStructure(oldFunction, newFunction, parametersMapping)

            newDeclaration.annotations = oldDeclaration.annotations

            transformedDeclarations[oldDeclaration] = newDeclaration
        }

        private fun createTransformedValueParameters(
            capturedValues: List<BirValueDeclaration>,
            localFunctionContext: LocalContext,
            oldDeclaration: BirFunction,
            newDeclaration: BirFunction,
            isExplicitLocalFunction: Boolean = false
        ) = ArrayList<BirValueParameter>(capturedValues.size + oldDeclaration.valueParameters.size).apply {
            val generatedNames = mutableSetOf<String>()
            capturedValues.mapTo(this) { capturedValue ->
                BirValueParameter.build {
                    sourceSpan = capturedValue.sourceSpan
                    origin =
                        if (capturedValue is BirValueParameter && capturedValue.getIndex() < 0 && newDeclaration is BirConstructor) BOUND_RECEIVER_PARAMETER
                        else BOUND_VALUE_PARAMETER
                    name = suggestNameForCapturedValue(capturedValue, generatedNames, isExplicitLocalFunction = isExplicitLocalFunction)
                    type = localFunctionContext.remapType(capturedValue.type)
                    isCrossinline = (capturedValue as? BirValueParameter)?.isCrossinline == true
                    isNoinline = (capturedValue as? BirValueParameter)?.isNoinline == true
                }.also {
                    newParameterToCaptured[it] = capturedValue
                }
            }

            oldDeclaration.valueParameters.mapTo(this) { param ->
                param.copyTo(
                    newDeclaration,
                    type = localFunctionContext.remapType(param.type),
                ).also { new ->
                    new.varargElementType = param.varargElementType?.let { localFunctionContext.remapType(it) }
                    newParameterToOld.putAbsentOrSame(new, param)
                }
            }
        }

        private fun BirFunction.recordTransformedValueParameters(localContext: LocalContextWithClosureAsParameters) {
            valueParameters.forEach {
                val capturedValue = newParameterToCaptured[it]
                if (capturedValue != null) {
                    localContext.capturedValueToParameter[capturedValue] = it
                }
            }

            (listOfNotNull(dispatchReceiverParameter, extensionReceiverParameter) + valueParameters).forEach {
                val oldParameter = newParameterToOld[it]
                if (oldParameter != null) {
                    oldParameterToNew.putAbsentOrSame(oldParameter, it)
                }
            }
        }

        private fun createTransformedConstructorDeclaration(constructorContext: LocalClassConstructorContext) {
            val oldDeclaration = constructorContext.declaration

            val localClassContext = localClasses[oldDeclaration.parent]!!
            val capturedValues = localClassContext.closure.capturedValues

            // Restore context if constructor was cached
            oldDeclaration[GlobalBirElementAuxStorageTokens.CapturedConstructor]?.let { newDeclaration ->
                transformedDeclarations[oldDeclaration] = newDeclaration
                constructorContext.transformedDeclaration = newDeclaration
                newDeclaration.valueParameters.zip(capturedValues).forEach { (it, capturedValue) ->
                    newParameterToCaptured[it] = capturedValue
                }
                oldDeclaration.valueParameters.zip(newDeclaration.valueParameters).forEach { (v, it) ->
                    newParameterToOld.putAbsentOrSame(it, v)
                }
                newDeclaration.recordTransformedValueParameters(constructorContext)
                return
            }

            val newDeclaration = BirConstructor.build {
                sourceSpan = oldDeclaration.sourceSpan
                copyFlagsFrom(oldDeclaration)
                origin = oldDeclaration.origin
                visibility = visibilityPolicy.forConstructor(oldDeclaration, constructorContext.inInlineFunctionScope)
                returnType = oldDeclaration.returnType
            }

            constructorContext.transformedDeclaration = newDeclaration

            newDeclaration.copyTypeParametersFrom(oldDeclaration)

            oldDeclaration.dispatchReceiverParameter?.run {
                throw AssertionError("Local class constructor can't have dispatch receiver: ${oldDeclaration.render()}")
            }
            oldDeclaration.extensionReceiverParameter?.run {
                throw AssertionError("Local class constructor can't have extension receiver: ${oldDeclaration.render()}")
            }

            newDeclaration.valueParameters += createTransformedValueParameters(
                capturedValues, localClassContext, oldDeclaration, newDeclaration
            )
            newDeclaration.recordTransformedValueParameters(constructorContext)

            newDeclaration.annotations = oldDeclaration.annotations

            newDeclaration[GlobalBirElementAuxStorageTokens.Metadata] = oldDeclaration[GlobalBirElementAuxStorageTokens.Metadata]

            transformedDeclarations[oldDeclaration] = newDeclaration
            oldDeclaration[GlobalBirElementAuxStorageTokens.CapturedConstructor] = newDeclaration
        }


        private fun rewriteDeclarations() {
            localFunctions.values.forEach {
                rewriteFunctionBody(it.declaration, it)
            }

            localClassConstructors.values.forEach {
                rewriteFunctionBody(it.declaration, it)
            }

            localClasses.values.forEach {
                rewriteClassMembers(it.declaration, it)
            }

            rewriteFunctionBody(rootElement, null)
        }

        private fun rewriteFunctionBody(declaration: BirElement, localContext: LocalContext?) {
            fun visitMember(declaration: BirDeclaration): Boolean {
                return if (localContext is LocalClassContext && declaration.parent == localContext.declaration) {
                    val classMemberLocalContext = LocalClassMemberContext(declaration, localContext)
                    rewriteFunctionBody(declaration, classMemberLocalContext)
                    true
                } else false
            }

            declaration.traverseStackBased { element ->
                var handled = false
                if (element is BirLocalDelegatedProperty) {
                    // Both accessors extracted as closures.
                    element.delegate.walkIntoChildren()
                    handled = true
                }
                if (element is BirClass) {
                    localClasses[element].let {
                        if (it != null) {
                            element.replaceWith(it.declaration)
                        } else {
                            handled = visitMember(element)
                        }
                    }
                    element.walkIntoChildren()
                }
                if (element is BirConstructor) {
                    // Body is transformed separately. See loop over constructors in rewriteDeclarations().

                    val transformedDeclaration = localClassConstructors[element]?.transformedDeclaration
                    if (transformedDeclaration != null) {
                        element.replaceWith(transformedDeclaration)
                        transformedDeclaration.body = element.body.also { element.body = null }
                        element.valueParameters.filter { it.defaultValue != null }.forEach { argument ->
                            oldParameterToNew[argument]!!.defaultValue = argument.defaultValue
                        }
                        handled = true
                    }

                    element.walkIntoChildren()
                } else if (element is BirFunction) {
                    if (element in localFunctions) {
                        element.replaceWith(BirCompositeImpl(declaration.sourceSpan, birBuiltIns.unitType, null))
                    } else {
                        handled = visitMember(element)
                    }
                    element.walkIntoChildren()
                }

                if (element is BirGetValue) {
                    val target = element.target
                    localContext?.generateGetValue(target.sourceSpan, target)?.also {
                        element.replaceWith(it)
                    } ?: oldParameterToNew[target]?.let {
                        element.target = it
                        element.type = it.type
                    }
                    handled = true
                }
                if (element is BirSetValue) {
                    element.walkIntoChildren()
                    oldParameterToNew[element.target]?.let {
                        element.target = it
                        element.type = it.type
                    }
                    handled = true
                }

                if (element is BirCall) {
                    element.walkIntoChildren()
                    val oldCallee = element.target.asElement

                    oldCallee.transformed?.let { newCallee ->
                        element.target = newCallee as BirSimpleFunction
                        element.typeArguments =
                            element.getLocalTypeArguments(newCallee, oldCallee, newCallee.typeParameters.size - element.typeArguments.size)
                        transformValueArguments(element, newCallee, localContext)
                    }
                    handled = true
                }
                if (element is BirConstructorCall) {
                    element.walkIntoChildren()
                    val oldCallee = element.target.asElement
                    oldCallee.transformed?.let { newCallee ->
                        element.target = newCallee as BirConstructor
                        transformValueArguments(element, newCallee, localContext)
                    }
                    handled = true
                }
                if (element is BirDelegatingConstructorCall) {
                    element.walkIntoChildren()
                    val oldCallee = element.target.asElement
                    oldCallee.transformed?.let { newCallee ->
                        element.target = newCallee as BirConstructor
                        element.type = birBuiltIns.unitType
                        transformValueArguments(element, newCallee, localContext)
                    }
                    handled = true
                }
                if (element is BirFunctionReference) {
                    element.walkIntoChildren()
                    val oldCallee = element.target as BirFunction
                    oldCallee.transformed?.let { newCallee ->
                        val typeParameters = if (newCallee is BirConstructor)
                            newCallee.parentAsClass.typeParameters
                        else
                            newCallee.typeParameters

                        element.target = newCallee
                        element.reflectionTarget = (element.reflectionTarget as BirFunction?)?.let { it.transformed ?: it }
                        element.typeArguments =
                            element.getLocalTypeArguments(newCallee, oldCallee, typeParameters.size - element.typeArguments.size)
                        transformValueArguments(element, newCallee, localContext)
                    }
                    handled = true
                }
                if (element is BirReturn) {
                    element.walkIntoChildren()
                    val oloReturnTarget = element.returnTarget as? BirFunction
                    oloReturnTarget?.transformed?.let { newReturnTarget ->
                        element.returnTarget = newReturnTarget
                        element.type = birBuiltIns.nothingType
                    }
                    handled = true
                }

                if (!handled) {
                    element.walkIntoChildren()

                    if (element is BirDeclarationReference) {
                        if (element.target in transformedDeclarations) {
                            TODO()
                        }
                    }

                    if (element is BirDeclaration) {
                        if (element in transformedDeclarations) {
                            TODO()
                        }
                    }
                }
            }
        }

        private fun BirMemberAccessExpression<*>.getLocalTypeArguments(
            newCallee: BirFunction,
            oldCallee: BirFunction,
            nonLocalArgumentsShift: Int,
        ): MutableList<BirType?> {
            val typeArguments = List<BirType?>(newCallee.valueParameters.size) { null }.toMutableList()
            localFunctions[oldCallee]?.let { context ->
                for ((outerTypeParameter, innerTypeParameter) in context.capturedTypeParameterToTypeParameter) {
                    typeArguments[innerTypeParameter.getIndex()] = outerTypeParameter.defaultType // TODO: remap default type!
                }
            }
            this.typeArguments.forEachIndexed { index, arg ->
                typeArguments[index + nonLocalArgumentsShift] = arg
            }
            return typeArguments
        }

        private fun transformValueArguments(expression: BirMemberAccessExpression<*>, newTarget: BirFunction, localContext: LocalContext?) {
            for ((oldArg, newParam) in expression.valueArguments zip newTarget.valueParameters) {
                val oldParam = newParameterToOld[newParam]
                if (oldParam == null) {
                    val capturedValue = newParameterToCaptured[newParam]
                        ?: throw AssertionError("Non-mapped parameter $newParam")

                    val newArg = localContext?.generateGetValue(expression.sourceSpan, capturedValue) ?: run {
                        val value = oldParameterToNew[capturedValue] ?: capturedValue
                        BirGetValueImpl(expression.sourceSpan, value.type, value, null)
                    }

                    oldArg.replaceWith(newArg)
                }
            }
        }

        private fun rewriteClassMembers(klass: BirClass, localClassContext: LocalClassContext) {
            val constructors = klass.declarations.filterIsInstance<BirConstructor>()

            rewriteFunctionBody(klass, localClassContext)

            // NOTE: if running before InitializersLowering, we can instead look for constructors that have
            //   IrInstanceInitializerCall. However, Native runs these two lowerings in opposite order.
            val constructorsByDelegationKinds: Map<ConstructorDelegationKind, List<LocalClassConstructorContext>> = constructors
                .asSequence()
                .map { localClassConstructors[it]!! }
                .groupBy { it.declaration.determineDelegationKind() }

            val constructorsCallingSuper = constructorsByDelegationKinds[ConstructorDelegationKind.CALLS_SUPER].orEmpty()

            assert(constructorsCallingSuper.isNotEmpty() || constructorsByDelegationKinds[ConstructorDelegationKind.PARTIAL_LINKAGE_ERROR] != null) {
                "Expected at least one constructor calling super; class: $klass"
            }

            val usedCaptureFields = finalizeFieldCreatedForCapturedValues(localClassContext)
            klass.declarations += usedCaptureFields

            klass[CapturedFieldsToken] = klass[CapturedFieldsToken].orEmpty() + usedCaptureFields

            for (constructorContext in constructorsCallingSuper) {
                val blockBody = constructorContext.declaration.body as? BirBlockBody
                    ?: throw AssertionError("Unexpected constructor body: ${constructorContext.declaration.body}")

                // NOTE: It's important to set the fields for captured values in the same order as the arguments,
                // since `AnonymousObjectTransformer` relies on this ordering.
                blockBody.statements.addAll(
                    0,
                    localClassContext.capturedValueToField.mapNotNull { (capturedValue, field) ->
                        val symbol = field.fieldIfUsed ?: return@mapNotNull null
                        BirSetFieldImpl(
                            sourceSpan = SourceSpan.UNDEFINED,
                            type = birBuiltIns.unitType,
                            target = symbol,
                            receiver = BirGetValueImpl(SourceSpan.UNDEFINED, klass.thisReceiver!!.type, klass.thisReceiver!!, null),
                            value = constructorContext.generateGetValue(SourceSpan.UNDEFINED, capturedValue)!!,
                            origin = LoweredStatementOrigins.STATEMENT_ORIGIN_INITIALIZER_OF_FIELD_FOR_CAPTURED_VALUE,
                            superQualifier = null,
                        )
                    }
                )
            }
        }

        private fun finalizeFieldCreatedForCapturedValues(localClassContext: LocalClassContext): List<BirField> {
            val classDeclaration = localClassContext.declaration
            val generatedNames = mutableSetOf<String>()
            return localClassContext.capturedValueToField.mapNotNull { (capturedValue, field) ->
                field.fieldIfUsed?.let {
                    it.sourceSpan = classDeclaration.sourceSpan
                    it.name = suggestNameForCapturedValue(capturedValue, generatedNames)
                    it
                }
            }
        }

        private fun insertLoweredDeclarationForLocalFunctions() {
            localFunctions.values.forEach { localContext ->
                localContext.transformedDeclaration.apply {
                    val original = localContext.declaration

                    // IR version does not clone the body - strange?
                    this.body = original.body?.deepCopy()
                    this.body?.let { localContext.remapTypes(it) }

                    original.valueParameters.filter { v -> v.defaultValue != null }.forEach { argument ->
                        val body = argument.defaultValue!!
                        localContext.remapTypes(body)
                        oldParameterToNew[argument]!!.defaultValue = body
                    }

                    localContext.ownerForLoweredDeclaration.addChild(this)
                }
            }
        }

        private fun suggestLocalName(declaration: BirDeclarationWithName): String {
            val declarationName = localNameSanitizer(declaration.name.asString())
            localFunctions[declaration]?.let {
                val baseName = if (declaration.name.isSpecial) "lambda" else declarationName
                if (it.index >= 0) {
                    if (!suggestUniqueNames) return baseName

                    val separator = if (
                        compatibilityModeForInlinedLocalDelegatedPropertyAccessors &&
                        declaration.origin == IrDeclarationOrigin.DELEGATED_PROPERTY_ACCESSOR &&
                        container is BirFunction && container.isInline
                    ) "-" else "$"
                    return "$baseName$separator${it.index}"
                }
            }

            return declarationName
        }

        private fun generateNameForLiftedDeclaration(
            declaration: BirDeclaration,
            newOwner: BirDeclarationHost
        ): Name {
            val parents = declaration.ancestors(true).takeWhile { it != newOwner }.toList().reversed()
            val nameFromParents = parents.joinToString(separator = "$") { suggestLocalName(it as BirDeclarationWithName) }
            // Local functions declared in anonymous initializers have classes as their parents.
            // Such anonymous initializers, however, are inlined into the constructors delegating to super class constructor.
            // There can be local functions declared in local function in init blocks (and further),
            // but such functions would have proper "safe" names (outerLocalFun1$outerLocalFun2$...$localFun).
            return if (parents.size == 1 && declaration.parent is BirClass)
                Name.identifier("_init_\$$nameFromParents")
            else
                Name.identifier(nameFromParents)
        }

        private fun suggestNameForCapturedValue(
            declaration: BirValueDeclaration,
            usedNames: MutableSet<String>,
            isExplicitLocalFunction: Boolean = false
        ): Name {
            if (declaration is BirValueParameter) {
                if (declaration.name.asString() == "<this>" && declaration.isDispatchReceiver()) {
                    return findFirstUnusedName("this\$0", usedNames) {
                        "this\$$it"
                    }
                } else if (declaration.name.asString() == "<this>" && declaration.isExtensionReceiver()) {
                    val parentNameSuffix = declaration.parentNameSuffixForExtensionReceiver
                    return findFirstUnusedName("\$this_$parentNameSuffix", usedNames) {
                        "\$this_$parentNameSuffix\$$it"
                    }
                } else if (declaration.isCapturedReceiver()) {
                    val baseName = declaration.name.asString().removePrefix(CAPTURED_RECEIVER_PREFIX)
                    return findFirstUnusedName("\$this_$baseName", usedNames) {
                        "\$this_$baseName\$$it"
                    }
                }
            }

            val base = if (declaration.name.isSpecial) {
                declaration.name.asStringStripSpecialMarkers()
            } else {
                declaration.name.asString()
            }

            return if (isExplicitLocalFunction && declaration is BirVariable) {
                findFirstUnusedName(base, usedNames) {
                    "$base$$it"
                }
            } else {
                findFirstUnusedName(base.synthesizedString, usedNames) {
                    "$base$$it".synthesizedString
                }
            }
        }

        private inline fun findFirstUnusedName(initialName: String, usedNames: MutableSet<String>, nextName: (Int) -> String): Name {
            var chosen = initialName
            var suffix = 0
            while (!usedNames.add(chosen))
                chosen = nextName(++suffix)
            return Name.identifier(chosen)
        }

        private fun BirValueParameter.isDispatchReceiver(): Boolean =
            when (val parent = this.parent) {
                is BirFunction ->
                    parent.dispatchReceiverParameter == this
                is BirClass ->
                    parent.thisReceiver == this
                else ->
                    false
            }

        private fun BirValueParameter.isExtensionReceiver(): Boolean {
            val parentFun = parent as? BirFunction ?: return false
            return parentFun.extensionReceiverParameter == this
        }

        private val CAPTURED_RECEIVER_PREFIX = "\$this\$"

        private fun BirValueParameter.isCapturedReceiver(): Boolean =
            name.asString().startsWith(CAPTURED_RECEIVER_PREFIX)

        private val BirValueParameter.parentNameSuffixForExtensionReceiver: String
            get() {
                val parentFun = parent as? BirSimpleFunction
                    ?: throw AssertionError("Extension receiver parent is not a simple function: ${parent?.render()}")
                val correspondingProperty = parentFun.correspondingProperty?.asElement
                return when {
                    correspondingProperty != null ->
                        correspondingProperty.name.asStringStripSpecialMarkers()
                    else ->
                        parentFun.name.asStringStripSpecialMarkers()
                }
            }
    }

    // Need to keep LocalFunctionContext.index
    private val BirSymbolElement.scopeWithCounter: ScopeWithCounter
        get() = this[ScopeWithCounterToken] ?: ScopeWithCounter(this).also {
            this[ScopeWithCounterToken] = it
        }

    internal class ScopeWithCounter(val element: BirElement) {
        // Continuous numbering across all declarations in the container.
        var counter: Int = 0
        val usedLocalFunctionNames: MutableSet<Name> = hashSetOf()
    }

    abstract class LocalContext {
        val capturedTypeParameterToTypeParameter: MutableMap<BirTypeParameter, BirTypeParameter> = mutableMapOf()

        // By the time typeRemapper is used, the map will be already filled
        val typeRemapper = BirTypeParameterRemapper(capturedTypeParameterToTypeParameter)

        /**
         * @return the expression to get the value for given declaration, or `null` if [BirGetValue] should be used.
         */
        context (WasmBirContext)
        abstract fun generateGetValue(sourceSpan: SourceSpan, valueDeclaration: BirValueDeclaration): BirExpression?

        fun remapType(type: BirType): BirType {
            if (capturedTypeParameterToTypeParameter.isEmpty()) return type
            return typeRemapper.remapType(type)
        }

        fun remapTypes(body: BirBody) {
            if (capturedTypeParameterToTypeParameter.isEmpty()) return
            body.remapTypes(typeRemapper)
        }
    }

    abstract class LocalContextWithClosureAsParameters : LocalContext() {
        abstract val declaration: BirFunction
        abstract val transformedDeclaration: BirFunction

        val capturedValueToParameter: MutableMap<BirValueDeclaration, BirValueParameter> = mutableMapOf()

        context (WasmBirContext)
        override fun generateGetValue(sourceSpan: SourceSpan, valueDeclaration: BirValueDeclaration): BirExpression? {
            val parameter = capturedValueToParameter[valueDeclaration] ?: return null

            return BirGetValueImpl(sourceSpan, parameter.type, parameter, null)
        }
    }

    sealed class OwnerForLoweredDeclaration(val isLocal: Boolean) {
        context (WasmBirContext)
        abstract fun addChild(declaration: BirDeclaration)

        abstract fun closestDeclarationParent(): BirDeclarationHost

        // Usually, just move local functions to the nearest class or file.
        class DeclarationContainer(private val irDeclarationContainer: BirDeclarationContainer) : OwnerForLoweredDeclaration(false) {
            context (WasmBirContext)
            override fun addChild(declaration: BirDeclaration) {
                irDeclarationContainer.declarations.add(declaration)
            }

            override fun closestDeclarationParent() = irDeclarationContainer
        }

        // But, local functions defined in an inline lambda need to be popped up to the root inline call.
        class Block(private val irBlock: BirBlock, private val irDeclarationParent: BirDeclarationHost) : OwnerForLoweredDeclaration(true) {
            private val initialStatementsCount = irBlock.statements.size
            context (WasmBirContext)
            override fun addChild(declaration: BirDeclaration) {
                // Place all children at the block's start but in order they are being added.
                irBlock.statements.add(irBlock.statements.size - initialStatementsCount, declaration)
            }

            override fun closestDeclarationParent() = irDeclarationParent
        }
    }

    class LocalFunctionContext(
        override val declaration: BirSimpleFunction,
        val index: Int,
        val ownerForLoweredDeclaration: OwnerForLoweredDeclaration
    ) : LocalContextWithClosureAsParameters() {
        lateinit var closure: Closure
        override lateinit var transformedDeclaration: BirSimpleFunction
    }

    private class LocalClassConstructorContext(
        override val declaration: BirConstructor,
        val inInlineFunctionScope: Boolean
    ) : LocalContextWithClosureAsParameters() {
        override lateinit var transformedDeclaration: BirConstructor
    }

    private inner class PotentiallyUnusedField(val capturedValue: BirValueDeclaration) {
        var fieldIfUsed: BirField? = null
            private set

        fun getField(): BirField {
            if (fieldIfUsed == null) {
                val origin = if (capturedValue is BirValueParameter && capturedValue.isCrossinline)
                    DECLARATION_ORIGIN_FIELD_FOR_CROSSINLINE_CAPTURED_VALUE
                else
                    DECLARATION_ORIGIN_FIELD_FOR_CAPTURED_VALUE
                fieldIfUsed = BirField.build {
                    type = capturedValue.type
                    this.origin = origin
                    visibility = visibilityPolicy.forCapturedField(capturedValue)
                    isFinal = true
                    isExternal = false
                    isStatic = false
                }
            }
            return fieldIfUsed!!
        }
    }

    private inner class LocalClassContext(
        val declaration: BirClass,
        val inInlineFunctionScope: Boolean,
        val constructorContext: LocalContext?
    ) : LocalContext() {
        lateinit var closure: Closure

        // NOTE: This map is iterated over in `rewriteClassMembers` and we're relying on
        // the deterministic iteration order that `mutableMapOf` provides.
        val capturedValueToField: MutableMap<BirValueDeclaration, PotentiallyUnusedField> = mutableMapOf()

        override fun generateGetValue(sourceSpan: SourceSpan, valueDeclaration: BirValueDeclaration): BirExpression? {
            // TODO: this used to be a hack for the JVM bytecode inliner (which misbehaved when inline lambdas had no fields),
            //  but it's no longer necessary. It is only here for backwards compatibility with old kotlinc versions
            //  and can be removed, probably in 1.9.
            if (!forceFieldsForInlineCaptures || !valueDeclaration.isInlineDeclaration()) {
                // We're in the initializer scope, which will be moved to a primary constructor later.
                // Thus we can directly use that constructor's context and read from a parameter instead of a field.
                constructorContext?.generateGetValue(sourceSpan, valueDeclaration)?.let { return it }
            }

            val field = capturedValueToField[valueDeclaration] ?: return null
            val receiver = declaration.thisReceiver!!
            return BirGetFieldImpl(
                sourceSpan, valueDeclaration.type, field.getField(),
                receiver = BirGetValueImpl(sourceSpan, receiver.type, receiver, null),
                superQualifier = null, origin = null,
            )
        }

        private fun BirValueDeclaration.isInlineDeclaration() =
            this is BirValueParameter && parent.let { it is BirFunction && it.isInline } && isInlineParameter()
    }

    private class LocalClassMemberContext(val member: BirDeclaration, val classContext: LocalClassContext) : LocalContext() {
        context (WasmBirContext)
        override fun generateGetValue(sourceSpan: SourceSpan, valueDeclaration: BirValueDeclaration): BirExpression? {
            val field = classContext.capturedValueToField[valueDeclaration] ?: return null
            // This lowering does not process accesses to outer `this`.
            val receiver = (if (member is BirFunction) member.dispatchReceiverParameter else classContext.declaration.thisReceiver)
                ?: error("No dispatch receiver parameter for ${member.render()}")
            return BirGetFieldImpl(
                sourceSpan, valueDeclaration.type, field.getField(),
                receiver = BirGetValueImpl(sourceSpan, receiver.type, receiver, null),
                superQualifier = null, origin = null,
            )
        }
    }

    data class IntermediateDatastructures(
        val localFunctions: Map<BirFunction, LocalFunctionContext>,
        val newParameterToOld: Map<BirValueParameter, BirValueParameter>,
        val newParameterToCaptured: Map<BirValueParameter, BirValueSymbol>
    )

    object DECLARATION_ORIGIN_FIELD_FOR_CAPTURED_VALUE :
        IrDeclarationOriginImpl("FIELD_FOR_CAPTURED_VALUE", isSynthetic = true)

    object DECLARATION_ORIGIN_FIELD_FOR_CROSSINLINE_CAPTURED_VALUE :
        IrDeclarationOriginImpl("FIELD_FOR_CROSSINLINE_CAPTURED_VALUE", isSynthetic = true)

    companion object {
        private val ScopeWithCounterKey = BirElementAuxStorageKey<BirSymbolElement, ScopeWithCounter>()
        private val ScopeWithCounterToken = GlobalBirElementAuxStorageTokens.manager.registerToken(ScopeWithCounterKey)

        private val CapturedFieldsKey = BirElementAuxStorageKey<BirClass, List<BirField>>()
        private val CapturedFieldsToken = GlobalBirElementAuxStorageTokens.manager.registerToken(CapturedFieldsKey)
    }
}

private fun <K, V> MutableMap<K, V>.putAbsentOrSame(key: K, value: V) {
    val current = this.getOrPut(key) { value }

    if (current != value) {
        error("$current != $value")
    }
}