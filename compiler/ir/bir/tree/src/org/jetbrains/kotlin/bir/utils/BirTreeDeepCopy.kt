/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.utils

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.declarations.impl.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.expressions.impl.*
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.types.*
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import java.util.*

context (BirTreeContext)
fun <E : BirElement> E.deepCopy(
    copier: BirTreeDeepCopier = BirTreeDeepCopier(this)
): E {
    return copier.copyElement(this)
}

context (BirTreeContext)
@OptIn(ObsoleteDescriptorBasedAPI::class)
open class BirTreeDeepCopier(
    rootElement: BirElement
) {
    val rootElement = rootElement as BirElementBase

    protected val classes by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirClass>() }
    protected val scripts by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirScript>() }
    protected val constructors by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirConstructor>() }
    protected val enumEntries by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirEnumEntry>() }
    protected val externalPackageFragments by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirExternalPackageFragment>() }
    protected val fields by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirField>() }
    protected val files by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirFile>() }
    protected val functions by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirSimpleFunction>() }
    protected val properties by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirProperty>() }
    protected val returnableBlocks by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirReturnableBlock>() }
    protected val typeParameters by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirTypeParameter>() }
    protected val valueParameters by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirValueParameter>() }
    protected val variables by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirVariable>() }
    protected val localDelegatedProperties by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirLocalDelegatedProperty>() }
    protected val typeAliases by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirTypeAlias>() }
    protected val loops by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirLoop>() }

    protected fun <E : BirElement> createElementMap(): MutableMap<E, E> = IdentityHashMap<E, E>()

    protected inline fun <E : BirElement> doCopyElement(old: E, copy: () -> E): E {
        return copy()
    }

    protected fun <ME : BirElement, SE : ME> doCopyElement(
        old: SE,
        map: MutableMap<ME, ME>,
        copy: () -> SE,
        lateInitialize: (SE) -> Unit
    ): SE {
        var copied = false
        val new = map.computeIfAbsent(old) {
            copied = true
            copy()
        } as SE
        if (copied) {
            lateInitialize(new)
        }
        return new
    }

    protected fun <E : BirElement> BirChildElementList<E>.copyElements(from: BirChildElementList<E>) {
        for (element in from) {
            this += copyElement(element)
        }
    }

    protected fun BirElementBase.copyAuxData(from: BirElementBase) {
        tmpCopyAuxData(from)
    }

    protected fun BirAttributeContainer.copyAttributes(other: BirAttributeContainer) {
        attributeOwnerId = other.attributeOwnerId
        //originalBeforeInline = other.originalBeforeInline
    }

    fun <E : BirElement> remapElement(old: E): E {
        old as BirElementBase
        val rootElement = rootElement
        return if (old === rootElement || rootElement.isAncestorOf(old))
            copyElement(old)
        else old
    }

    fun <S : BirSymbol> remapSymbol(old: S): S {
        if (old is BirElementBase) {
            return remapElement(old)
        } else {
            return old
        }
    }

    fun remapType(old: BirType): BirType = when (old) {
        is BirSimpleType -> BirSimpleTypeImpl(
            old.kotlinType,
            remapSymbol(old.classifier),
            old.nullability,
            old.arguments.memoryOptimizedMap { remapTypeArgument(it) },
            old.annotations.memoryOptimizedMap { remapElement(it) },
            old.abbreviation?.let { abbreviation ->
                remapTypeAbbreviation(abbreviation)
            },
        )
        is BirDynamicType -> BirDynamicType(
            old.kotlinType,
            old.annotations.memoryOptimizedMap { remapElement(it) },
            old.variance,
        )
        is BirErrorType -> BirErrorType(
            old.kotlinType,
            old.annotations.memoryOptimizedMap { remapElement(it) },
            old.variance,
            old.isMarkedNullable,
        )
        else -> TODO(old.toString())
    }

    fun remapTypeArgument(old: BirTypeArgument): BirTypeArgument = when (old) {
        is BirStarProjection -> BirStarProjection
        is BirTypeProjectionImpl -> BirTypeProjectionImpl(remapType(old.type), old.variance)
        is BirType -> remapType(old) as BirTypeArgument
        else -> error(old)
    }

    fun remapTypeAbbreviation(old: BirTypeAbbreviation) = BirTypeAbbreviation(
        remapSymbol(old.typeAlias),
        old.hasQuestionMark,
        old.arguments.memoryOptimizedMap { remapTypeArgument(it) },
        old.annotations.memoryOptimizedMap { remapElement(it) },
    )

    fun <T : BirElement> copyElement(old: T): T = when (old) {
        is BirValueParameter -> copyValueParameter(old)
        is BirClass -> copyClass(old)
        is BirAnonymousInitializer -> copyAnonymousInitializer(old)
        is BirTypeParameter -> copyTypeParameter(old)
        is BirConstructor -> copyConstructor(old)
        is BirEnumEntry -> copyEnumEntry(old)
        is BirErrorDeclaration -> copyErrorDeclaration(old)
        is BirFunctionWithLateBindingImpl -> copyFunctionWithLateBinding(old)
        is BirPropertyWithLateBindingImpl -> copyPropertyWithLateBinding(old)
        is BirField -> copyField(old)
        is BirLocalDelegatedProperty -> copyLocalDelegatedProperty(old)
        is BirModuleFragment -> copyModuleFragment(old)
        is BirProperty -> copyProperty(old)
        is BirScript -> copyScript(old)
        is BirSimpleFunction -> copySimpleFunction(old)
        is BirTypeAlias -> copyTypeAlias(old)
        is BirVariable -> copyVariable(old)
        is BirExternalPackageFragment -> copyExternalPackageFragment(old)
        is BirFile -> copyFile(old)
        is BirExpressionBody -> copyExpressionBody(old)
        is BirBlockBody -> copyBlockBody(old)
        is BirConstructorCall -> copyConstructorCall(old)
        is BirGetObjectValue -> copyGetObjectValue(old)
        is BirGetEnumValue -> copyGetEnumValue(old)
        is BirRawFunctionReference -> copyRawFunctionReference(old)
        is BirComposite -> copyComposite(old)
        is BirReturnableBlock -> copyReturnableBlock(old)
        is BirInlinedFunctionBlock -> copyInlinedFunctionBlock(old)
        is BirBlock -> copyBlock(old)
        is BirSyntheticBody -> copySyntheticBody(old)
        is BirBreak -> copyBreak(old)
        is BirContinue -> copyContinue(old)
        is BirCall -> copyCall(old)
        is BirFunctionReference -> copyFunctionReference(old)
        is BirPropertyReference -> copyPropertyReference(old)
        is BirLocalDelegatedPropertyReference -> copyLocalDelegatedPropertyReference(old)
        is BirClassReference -> copyClassReference(old)
        is BirConst<*> -> copyConst(old)
        is BirConstantPrimitive -> copyConstantPrimitive(old)
        is BirConstantObject -> copyConstantObject(old)
        is BirConstantArray -> copyConstantArray(old)
        is BirDelegatingConstructorCall -> copyDelegatingConstructorCall(old)
        is BirDynamicOperatorExpression -> copyDynamicOperatorExpression(old)
        is BirDynamicMemberExpression -> copyDynamicMemberExpression(old)
        is BirEnumConstructorCall -> copyEnumConstructorCall(old)
        is BirErrorCallExpression -> copyErrorCallExpression(old)
        is BirGetField -> copyGetField(old)
        is BirSetField -> copySetField(old)
        is BirFunctionExpression -> copyFunctionExpression(old)
        is BirGetClass -> copyGetClass(old)
        is BirInstanceInitializerCall -> copyInstanceInitializerCall(old)
        is BirWhileLoop -> copyWhileLoop(old)
        is BirDoWhileLoop -> copyDoWhileLoop(old)
        is BirReturn -> copyReturn(old)
        is BirStringConcatenation -> copyStringConcatenation(old)
        is BirSuspensionPoint -> copySuspensionPoint(old)
        is BirSuspendableExpression -> copySuspendableExpression(old)
        is BirThrow -> copyThrow(old)
        is BirTry -> copyTry(old)
        is BirCatch -> copyCatch(old)
        is BirTypeOperatorCall -> copyTypeOperatorCall(old)
        is BirGetValue -> copyGetValue(old)
        is BirSetValue -> copySetValue(old)
        is BirVararg -> copyVararg(old)
        is BirSpreadElement -> copySpreadElement(old)
        is BirWhen -> copyWhen(old)
        is BirElseBranch -> copyElseBranch(old)
        is BirBranch -> copyBranch(old)
        else -> error(old)
    } as T

    open fun copyValueParameter(old: BirValueParameter): BirValueParameter = doCopyElement(old, valueParameters, {
        BirValueParameterImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            name = old.name,
            type = BirUninitializedType,
            isAssignable = old.isAssignable,
            varargElementType = null,
            isCrossinline = old.isCrossinline,
            isNoinline = old.isNoinline,
            isHidden = old.isHidden,
            defaultValue = null,
        )
    }) { new ->
        new.defaultValue = old.defaultValue?.let { copyElement(it) }
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.varargElementType = old.varargElementType?.let { remapType(it) }
        new.copyAuxData(old)
    }

    open fun copyClass(old: BirClass): BirClass = doCopyElement(old, classes, {
        BirClassImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            visibility = old.visibility,
            name = old.name,
            isExternal = old.isExternal,
            kind = old.kind,
            modality = old.modality,
            isCompanion = old.isCompanion,
            isInner = old.isInner,
            isData = old.isData,
            isValue = old.isValue,
            isExpect = old.isExpect,
            isFun = old.isFun,
            source = old.source,
            superTypes = emptyList(),
            thisReceiver = null,
            valueClassRepresentation = null,
        )
    }) { new ->
        new.copyAttributes(old)
        new.thisReceiver = old.thisReceiver?.let { copyElement(it) }
        new.typeParameters.copyElements(old.typeParameters)
        new.declarations.copyElements(old.declarations)
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.superTypes = old.superTypes.memoryOptimizedMap { remapType(it) }
        new.valueClassRepresentation = old.valueClassRepresentation?.mapUnderlyingType { remapType(it) as BirSimpleType }
        new.copyAuxData(old)
    }

    open fun copyAnonymousInitializer(old: BirAnonymousInitializer): BirAnonymousInitializer {
        val new = BirAnonymousInitializerImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            isStatic = old.isStatic,
            body = copyElement(old.body),
        )
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.copyAuxData(old)
        return new
    }

    open fun copyTypeParameter(old: BirTypeParameter): BirTypeParameter = doCopyElement(old, typeParameters, {
        BirTypeParameterImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            name = old.name,
            variance = old.variance,
            isReified = old.isReified,
            superTypes = emptyList(),
        )
    }) { new ->
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.superTypes = old.superTypes.memoryOptimizedMap { remapType(it) }
        new.copyAuxData(old)
    }

    open fun copyConstructor(old: BirConstructor): BirConstructor = doCopyElement(old, constructors, {
        BirConstructorImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            visibility = old.visibility,
            name = old.name,
            isExternal = old.isExternal,
            isInline = old.isInline,
            isExpect = old.isExpect,
            returnType = BirUninitializedType,
            dispatchReceiverParameter = null,
            extensionReceiverParameter = null,
            contextReceiverParametersCount = old.contextReceiverParametersCount,
            body = null,
            isPrimary = old.isPrimary,
        )
    }) { new ->
        new.dispatchReceiverParameter = old.dispatchReceiverParameter?.let { copyElement(it) }
        new.extensionReceiverParameter = old.extensionReceiverParameter?.let { copyElement(it) }
        new.valueParameters.copyElements(old.valueParameters)
        new.body = old.body?.let { copyElement(it) }
        new.typeParameters.copyElements(old.typeParameters)
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.returnType = remapType(old.returnType)
        new.copyAuxData(old)
    }

    open fun copyEnumEntry(old: BirEnumEntry): BirEnumEntry = doCopyElement(old, enumEntries, {
        BirEnumEntryImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            name = old.name,
            initializerExpression = null,
            correspondingClass = null,
        )
    }) { new ->
        new.initializerExpression = old.initializerExpression?.let { copyElement(it) }
        new.correspondingClass = old.correspondingClass?.let { copyElement(it) }
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.copyAuxData(old)
    }

    open fun copyErrorDeclaration(old: BirErrorDeclaration): BirErrorDeclaration {
        val new = BirErrorDeclarationImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
        )
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.copyAuxData(old)
        return new
    }


    open fun copyFunctionWithLateBinding(old: BirFunctionWithLateBindingImpl): BirFunctionWithLateBinding {
        val new = BirFunctionWithLateBindingImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            visibility = old.visibility,
            name = old.name,
            isExternal = old.isExternal,
            isInline = old.isInline,
            isExpect = old.isExpect,
            returnType = BirUninitializedType,
            dispatchReceiverParameter = null,
            extensionReceiverParameter = null,
            contextReceiverParametersCount = old.contextReceiverParametersCount,
            body = null,
            modality = old.modality,
            isFakeOverride = old.isFakeOverride,
            overriddenSymbols = emptyList(),
            isTailrec = old.isTailrec,
            isSuspend = old.isSuspend,
            isOperator = old.isOperator,
            isInfix = old.isInfix,
            correspondingProperty = null,
            isElementBound = old.isElementBound,
        )
        new.copyAttributes(old)
        new.dispatchReceiverParameter = old.dispatchReceiverParameter?.let { copyElement(it) }
        new.extensionReceiverParameter = old.extensionReceiverParameter?.let { copyElement(it) }
        new.valueParameters.copyElements(old.valueParameters)
        new.body = old.body?.let { copyElement(it) }
        new.typeParameters.copyElements(old.typeParameters)
        new.correspondingProperty = old.correspondingProperty?.let { remapSymbol(it) }
        new.overriddenSymbols = old.overriddenSymbols.memoryOptimizedMap { remapSymbol(it) }
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.returnType = remapType(old.returnType)
        new.copyAuxData(old)
        return new
    }

    open fun copyPropertyWithLateBinding(old: BirPropertyWithLateBindingImpl): BirPropertyWithLateBinding {
        val new = BirPropertyWithLateBindingImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            name = old.name,
            isExternal = old.isExternal,
            visibility = old.visibility,
            modality = old.modality,
            isFakeOverride = old.isFakeOverride,
            overriddenSymbols = emptyList(),
            isVar = old.isVar,
            isConst = old.isConst,
            isLateinit = old.isLateinit,
            isDelegated = old.isDelegated,
            isExpect = old.isExpect,
            backingField = null,
            getter = null,
            setter = null,
            isElementBound = old.isElementBound,
        )
        new.copyAttributes(old)
        new.getter = old.getter?.let { copyElement(it) }
        new.setter = old.setter?.let { copyElement(it) }
        new.backingField = old.backingField?.let { copyElement(it) }
        new.overriddenSymbols = old.overriddenSymbols.memoryOptimizedMap { remapSymbol(it) }
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.copyAuxData(old)
        return new
    }

    open fun copyField(old: BirField): BirField = doCopyElement(old, fields, {
        BirFieldImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            visibility = old.visibility,
            name = old.name,
            isExternal = old.isExternal,
            type = BirUninitializedType,
            isFinal = old.isFinal,
            isStatic = old.isStatic,
            initializer = null,
            correspondingProperty = null,
        )
    }) { new ->
        new.initializer = old.initializer?.let { copyElement(it) }
        new.correspondingProperty = old.correspondingProperty?.let { remapSymbol(it) }
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.type = remapType(old.type)
        new.copyAuxData(old)
    }

    open fun copyLocalDelegatedProperty(old: BirLocalDelegatedProperty): BirLocalDelegatedProperty =
        doCopyElement(old, localDelegatedProperties, {
            BirLocalDelegatedPropertyImpl(
                sourceSpan = old.sourceSpan,
                annotations = emptyList(),
                _descriptor = old._descriptor,
                origin = old.origin,
                name = old.name,
                type = BirUninitializedType,
                isVar = old.isVar,
                delegate = copyElement(old.delegate),
                getter = copyElement(old.getter),
                setter = null,
            )
        }) { new ->
            new.setter = old.setter?.let { copyElement(it) }
            new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
            new.type = remapType(old.type)
            new.copyAuxData(old)
        }

    open fun copyModuleFragment(old: BirModuleFragment): BirModuleFragment {
        val new = BirModuleFragmentImpl(
            sourceSpan = old.sourceSpan,
            _descriptor = old._descriptor,
            name = old.name,
        )
        new.files.copyElements(old.files)
        new.copyAuxData(old)
        return new
    }

    open fun copyProperty(old: BirProperty): BirProperty = doCopyElement(old, properties, {
        BirPropertyImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            name = old.name,
            isExternal = old.isExternal,
            visibility = old.visibility,
            modality = old.modality,
            isFakeOverride = old.isFakeOverride,
            overriddenSymbols = emptyList(),
            isVar = old.isVar,
            isConst = old.isConst,
            isLateinit = old.isLateinit,
            isDelegated = old.isDelegated,
            isExpect = old.isExpect,
            backingField = null,
            getter = null,
            setter = null,
        )
    }) { new ->
        new.copyAttributes(old)
        new.backingField = old.backingField?.let { copyElement(it) }
        new.getter = old.getter?.let { copyElement(it) }
        new.setter = old.setter?.let { copyElement(it) }
        new.overriddenSymbols = old.overriddenSymbols.memoryOptimizedMap { remapSymbol(it) }
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.copyAuxData(old)
    }

    open fun copyScript(old: BirScript): BirScript = doCopyElement(old, scripts, {
        BirScriptImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor as ScriptDescriptor,
            origin = old.origin,
            name = old.name,
            thisReceiver = null,
            baseClass = null,
            providedProperties = old.providedProperties.memoryOptimizedMap { remapSymbol(it) },
            resultProperty = old.resultProperty?.let { remapSymbol(it) },
            earlierScriptsParameter = null,
            earlierScripts = old.earlierScripts?.memoryOptimizedMap { remapSymbol(it) },
            targetClass = old.targetClass?.let { remapSymbol(it) },
            constructor = null,
        )
    }) { new ->
        new.thisReceiver = old.thisReceiver?.let { copyElement(it) }
        new.explicitCallParameters.copyElements(old.explicitCallParameters)
        new.implicitReceiversParameters.copyElements(old.implicitReceiversParameters)
        new.providedPropertiesParameters.copyElements(old.providedPropertiesParameters)
        new.earlierScriptsParameter = old.earlierScriptsParameter?.let { copyElement(it) }
        new.constructor = old.constructor?.let { remapElement(it) }
        new.statements.copyElements(old.statements)
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.baseClass = old.baseClass?.let { remapType(it) }
        new.copyAuxData(old)
    }

    open fun copySimpleFunction(old: BirSimpleFunction): BirSimpleFunction = doCopyElement(old, functions, {
        BirSimpleFunctionImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            visibility = old.visibility,
            name = old.name,
            isExternal = old.isExternal,
            isInline = old.isInline,
            isExpect = old.isExpect,
            returnType = BirUninitializedType,
            dispatchReceiverParameter = null,
            extensionReceiverParameter = null,
            contextReceiverParametersCount = old.contextReceiverParametersCount,
            body = null,
            modality = old.modality,
            isFakeOverride = old.isFakeOverride,
            overriddenSymbols = emptyList(),
            isTailrec = old.isTailrec,
            isSuspend = old.isSuspend,
            isOperator = old.isOperator,
            isInfix = old.isInfix,
            correspondingProperty = null,
        )
    }) { new ->
        new.copyAttributes(old)
        new.dispatchReceiverParameter = old.dispatchReceiverParameter?.let { copyElement(it) }
        new.extensionReceiverParameter = old.extensionReceiverParameter?.let { copyElement(it) }
        new.valueParameters.copyElements(old.valueParameters)
        new.body = old.body?.let { copyElement(it) }
        new.typeParameters.copyElements(old.typeParameters)
        new.overriddenSymbols = old.overriddenSymbols.memoryOptimizedMap { remapSymbol(it) }
        new.correspondingProperty = old.correspondingProperty?.let { remapSymbol(it) }
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.returnType = remapType(old.returnType)
        new.copyAuxData(old)
    }

    open fun copyTypeAlias(old: BirTypeAlias): BirTypeAlias = doCopyElement(old, typeAliases, {
        BirTypeAliasImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            name = old.name,
            visibility = old.visibility,
            isActual = old.isActual,
            expandedType = BirUninitializedType,
        )
    }) { new ->
        new.typeParameters.copyElements(old.typeParameters)
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.expandedType = remapType(old.expandedType)
        new.copyAuxData(old)
    }

    open fun copyVariable(old: BirVariable): BirVariable = doCopyElement(old, variables, {
        BirVariableImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            name = old.name,
            type = BirUninitializedType,
            isAssignable = old.isAssignable,
            isVar = old.isVar,
            isConst = old.isConst,
            isLateinit = old.isLateinit,
            initializer = null,
        )
    }) { new ->
        new.initializer = old.initializer?.let { copyElement(it) }
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.type = remapType(old.type)
        new.copyAuxData(old)
    }

    open fun copyExternalPackageFragment(old: BirExternalPackageFragment): BirExternalPackageFragment =
        doCopyElement(old, externalPackageFragments, {
            BirExternalPackageFragmentImpl(
                sourceSpan = old.sourceSpan,
                _descriptor = old._descriptor,
                fqName = old.fqName,
                containerSource = old.containerSource,
            )
        }) { new ->
            new.declarations.copyElements(old.declarations)
            new.copyAuxData(old)
        }

    open fun copyFile(old: BirFile): BirFile = doCopyElement(old, files, {
        BirFileImpl(
            sourceSpan = old.sourceSpan,
            _descriptor = old._descriptor,
            fqName = old.fqName,
            annotations = emptyList(),
            module = remapElement(old.module),
            fileEntry = old.fileEntry,
        )
    }) { new ->
        new.declarations.copyElements(old.declarations)
        new.annotations = old.annotations.memoryOptimizedMap { copyElement(it) }
        new.copyAuxData(old)
    }

    open fun copyExpressionBody(old: BirExpressionBody): BirExpressionBody {
        val new = BirExpressionBodyImpl(
            sourceSpan = old.sourceSpan,
            expression = copyElement(old.expression),
        )
        new.copyAuxData(old)
        return new
    }

    open fun copyBlockBody(old: BirBlockBody): BirBlockBody {
        val new = BirBlockBodyImpl(
            sourceSpan = old.sourceSpan,
        )
        new.statements.copyElements(old.statements)
        new.copyAuxData(old)
        return new
    }

    open fun copyConstructorCall(old: BirConstructorCall): BirConstructorCall {
        val new = BirConstructorCallImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = emptyList(),
            contextReceiversCount = old.contextReceiversCount,
            source = old.source,
            constructorTypeArgumentsCount = old.constructorTypeArgumentsCount,
        )
        new.copyAttributes(old)
        new.dispatchReceiver = old.dispatchReceiver?.let { copyElement(it) }
        new.extensionReceiver = old.extensionReceiver?.let { copyElement(it) }
        new.valueArguments.copyElements(old.valueArguments)
        new.typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } }
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyGetObjectValue(old: BirGetObjectValue): BirGetObjectValue {
        val new = BirGetObjectValueImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyGetEnumValue(old: BirGetEnumValue): BirGetEnumValue {
        val new = BirGetEnumValueImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyRawFunctionReference(old: BirRawFunctionReference): BirRawFunctionReference {
        val new = BirRawFunctionReferenceImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyBlock(old: BirBlock): BirBlock {
        val new = BirBlockImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            origin = old.origin,
        )
        new.copyAttributes(old)
        new.statements.copyElements(old.statements)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyComposite(old: BirComposite): BirComposite {
        val new = BirCompositeImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            origin = old.origin,
        )
        new.copyAttributes(old)
        new.statements.copyElements(old.statements)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyReturnableBlock(old: BirReturnableBlock): BirReturnableBlock = doCopyElement(old, returnableBlocks, {
        BirReturnableBlockImpl(
            sourceSpan = old.sourceSpan,
            _descriptor = old._descriptor,
            type = BirUninitializedType,
            origin = old.origin,
        )
    }) { new ->
        new.copyAttributes(old)
        new.statements.copyElements(old.statements)
        new.type = remapType(old.type)
        new.copyAuxData(old)
    }

    open fun copyInlinedFunctionBlock(old: BirInlinedFunctionBlock): BirInlinedFunctionBlock {
        val new = BirInlinedFunctionBlockImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            origin = old.origin,
            inlineCall = old.inlineCall, // no remap
            inlinedElement = old.inlinedElement, // no remap
        )
        new.copyAttributes(old)
        new.statements.copyElements(old.statements)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copySyntheticBody(old: BirSyntheticBody): BirSyntheticBody {
        val new = BirSyntheticBodyImpl(
            sourceSpan = old.sourceSpan,
            kind = old.kind,
        )
        new.copyAuxData(old)
        return new
    }

    open fun copyBreak(old: BirBreak): BirBreak {
        val new = BirBreakImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            loop = remapElement(old.loop),
            label = old.label,
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyContinue(old: BirContinue): BirContinue {
        val new = BirContinueImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            loop = remapElement(old.loop),
            label = old.label,
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyCall(old: BirCall): BirCall {
        val new = BirCallImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = emptyList(),
            contextReceiversCount = old.contextReceiversCount,
            superQualifier = null,
        )
        new.copyAttributes(old)
        new.superQualifier = old.superQualifier?.let { remapSymbol(it) }
        new.dispatchReceiver = old.dispatchReceiver?.let { copyElement(it) }
        new.extensionReceiver = old.extensionReceiver?.let { copyElement(it) }
        new.valueArguments.copyElements(old.valueArguments)
        new.typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } }
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyFunctionReference(old: BirFunctionReference): BirFunctionReference {
        val new = BirFunctionReferenceImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = emptyList(),
            reflectionTarget = null,
        )
        new.copyAttributes(old)
        new.reflectionTarget = old.reflectionTarget?.let { remapSymbol(it) }
        new.dispatchReceiver = old.dispatchReceiver?.let { copyElement(it) }
        new.extensionReceiver = old.extensionReceiver?.let { copyElement(it) }
        new.valueArguments.copyElements(old.valueArguments)
        new.typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } }
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyPropertyReference(old: BirPropertyReference): BirPropertyReference {
        val new = BirPropertyReferenceImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = emptyList(),
            field = null,
            getter = null,
            setter = null,
        )
        new.copyAttributes(old)
        new.field = old.field?.let { remapSymbol(it) }
        new.getter = old.getter?.let { remapSymbol(it) }
        new.setter = old.setter?.let { remapSymbol(it) }
        new.dispatchReceiver = old.dispatchReceiver?.let { copyElement(it) }
        new.extensionReceiver = old.extensionReceiver?.let { copyElement(it) }
        new.valueArguments.copyElements(old.valueArguments)
        new.typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } }
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyLocalDelegatedPropertyReference(old: BirLocalDelegatedPropertyReference): BirLocalDelegatedPropertyReference {
        val new = BirLocalDelegatedPropertyReferenceImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = emptyList(),
            delegate = remapElement(old.delegate),
            getter = remapSymbol(old.getter),
            setter = null,
        )
        new.copyAttributes(old)
        new.setter = old.setter?.let { remapSymbol(it) }
        new.dispatchReceiver = old.dispatchReceiver?.let { copyElement(it) }
        new.extensionReceiver = old.extensionReceiver?.let { copyElement(it) }
        new.valueArguments.copyElements(old.valueArguments)
        new.typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } }
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyClassReference(old: BirClassReference): BirClassReference {
        val new = BirClassReferenceImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
            classType = BirUninitializedType,
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.classType = remapType(old.classType)
        new.copyAuxData(old)
        return new
    }

    open fun <T> copyConst(old: BirConst<T>): BirConst<T> {
        val new = BirConstImpl<T>(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            kind = old.kind,
            value = old.value,
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyConstantPrimitive(old: BirConstantPrimitive): BirConstantPrimitive {
        val new = BirConstantPrimitiveImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            value = copyElement(old.value),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyConstantObject(old: BirConstantObject): BirConstantObject {
        val new = BirConstantObjectImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            constructor = remapSymbol(old.constructor),
            typeArguments = old.typeArguments.memoryOptimizedMap { remapType(it) },
        )
        new.copyAttributes(old)
        new.valueArguments.copyElements(old.valueArguments)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyConstantArray(old: BirConstantArray): BirConstantArray {
        val new = BirConstantArrayImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
        )
        new.copyAttributes(old)
        new.elements.copyElements(old.elements)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyDelegatingConstructorCall(old: BirDelegatingConstructorCall): BirDelegatingConstructorCall {
        val new = BirDelegatingConstructorCallImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = emptyList(),
            contextReceiversCount = old.contextReceiversCount,
        )
        new.copyAttributes(old)
        new.dispatchReceiver = old.dispatchReceiver?.let { copyElement(it) }
        new.extensionReceiver = old.extensionReceiver?.let { copyElement(it) }
        new.valueArguments.copyElements(old.valueArguments)
        new.typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } }
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyDynamicOperatorExpression(old: BirDynamicOperatorExpression): BirDynamicOperatorExpression {
        val new = BirDynamicOperatorExpressionImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            operator = old.operator,
            receiver = copyElement(old.receiver),
        )
        new.copyAttributes(old)
        new.arguments.copyElements(old.arguments)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyDynamicMemberExpression(old: BirDynamicMemberExpression): BirDynamicMemberExpression {
        val new = BirDynamicMemberExpressionImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            memberName = old.memberName,
            receiver = copyElement(old.receiver),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyEnumConstructorCall(old: BirEnumConstructorCall): BirEnumConstructorCall {
        val new = BirEnumConstructorCallImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = emptyList(),
            contextReceiversCount = old.contextReceiversCount,
        )
        new.copyAttributes(old)
        new.dispatchReceiver = old.dispatchReceiver?.let { copyElement(it) }
        new.extensionReceiver = old.extensionReceiver?.let { copyElement(it) }
        new.valueArguments.copyElements(old.valueArguments)
        new.typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } }
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyErrorCallExpression(old: BirErrorCallExpression): BirErrorCallExpression {
        val new = BirErrorCallExpressionImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            description = old.description,
            explicitReceiver = null,
        )
        new.copyAttributes(old)
        new.explicitReceiver = old.explicitReceiver?.let { copyElement(it) }
        new.arguments.copyElements(old.arguments)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyGetField(old: BirGetField): BirGetField {
        val new = BirGetFieldImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
            superQualifier = null,
            receiver = null,
            origin = old.origin,
        )
        new.copyAttributes(old)
        new.superQualifier = old.superQualifier?.let { remapSymbol(it) }
        new.receiver = old.receiver?.let { copyElement(it) }
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copySetField(old: BirSetField): BirSetField {
        val new = BirSetFieldImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapSymbol(old.target),
            superQualifier = null,
            receiver = null,
            origin = old.origin,
            value = copyElement(old.value),
        )
        new.copyAttributes(old)
        new.superQualifier = old.superQualifier?.let { remapSymbol(it) }
        new.receiver = old.receiver?.let { copyElement(it) }
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyFunctionExpression(old: BirFunctionExpression): BirFunctionExpression {
        val new = BirFunctionExpressionImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            origin = old.origin,
            function = copyElement(old.function),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyGetClass(old: BirGetClass): BirGetClass {
        val new = BirGetClassImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            argument = copyElement(old.argument),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyInstanceInitializerCall(old: BirInstanceInitializerCall): BirInstanceInitializerCall {
        val new = BirInstanceInitializerCallImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            `class` = remapSymbol(old.`class`),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyWhileLoop(old: BirWhileLoop): BirWhileLoop = doCopyElement(old, loops, {
        BirWhileLoopImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            origin = old.origin,
            body = null,
            // nb: this may be a problem if there is a ref to the loop from within the condition (language seems to not allow that however).
            //  In such case the simples solution is to do what IR does right now - make condition property lateinit.
            condition = copyElement(old.condition),
            label = old.label,
        )
    }) { new ->
        new.copyAttributes(old)
        new.body = old.body?.let { copyElement(it) }
        new.type = remapType(old.type)
        new.copyAuxData(old)
    }

    open fun copyDoWhileLoop(old: BirDoWhileLoop): BirDoWhileLoop = doCopyElement(old, loops, {
        BirDoWhileLoopImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            origin = old.origin,
            body = null,
            condition = copyElement(old.condition),
            label = old.label,
        )
    }) { new ->
        new.copyAttributes(old)
        new.body = old.body?.let { copyElement(it) }
        new.type = remapType(old.type)
        new.copyAuxData(old)
    }

    open fun copyReturn(old: BirReturn): BirReturn {
        val new = BirReturnImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            value = copyElement(old.value),
            returnTarget = remapSymbol(old.returnTarget),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyStringConcatenation(old: BirStringConcatenation): BirStringConcatenation {
        val new = BirStringConcatenationImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
        )
        new.copyAttributes(old)
        new.arguments.copyElements(old.arguments)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copySuspensionPoint(old: BirSuspensionPoint): BirSuspensionPoint {
        val new = BirSuspensionPointImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            suspensionPointIdParameter = copyElement(old.suspensionPointIdParameter),
            result = copyElement(old.result),
            resumeResult = copyElement(old.resumeResult),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copySuspendableExpression(old: BirSuspendableExpression): BirSuspendableExpression {
        val new = BirSuspendableExpressionImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            suspensionPointId = copyElement(old.suspensionPointId),
            result = copyElement(old.result),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyThrow(old: BirThrow): BirThrow {
        val new = BirThrowImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            value = copyElement(old.value),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyTry(old: BirTry): BirTry {
        val new = BirTryImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            tryResult = copyElement(old.tryResult),
            finallyExpression = null,
        )
        new.copyAttributes(old)
        new.catches.copyElements(old.catches)
        new.finallyExpression = old.finallyExpression?.let { copyElement(it) }
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyCatch(old: BirCatch): BirCatch {
        val new = BirCatchImpl(
            sourceSpan = old.sourceSpan,
            catchParameter = copyElement(old.catchParameter),
            result = copyElement(old.result),
        )
        new.copyAuxData(old)
        return new
    }

    open fun copyTypeOperatorCall(old: BirTypeOperatorCall): BirTypeOperatorCall {
        val new = BirTypeOperatorCallImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            operator = old.operator,
            argument = copyElement(old.argument),
            typeOperand = BirUninitializedType,
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.typeOperand = remapType(old.typeOperand)
        new.copyAuxData(old)
        return new
    }

    open fun copyGetValue(old: BirGetValue): BirGetValue {
        val new = BirGetValueImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapElement(old.target),
            origin = old.origin,
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copySetValue(old: BirSetValue): BirSetValue {
        val new = BirSetValueImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            target = remapElement(old.target),
            origin = old.origin,
            value = copyElement(old.value),
        )
        new.copyAttributes(old)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyVararg(old: BirVararg): BirVararg {
        val new = BirVarargImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            varargElementType = BirUninitializedType,
        )
        new.copyAttributes(old)
        new.elements.copyElements(old.elements)
        new.type = remapType(old.type)
        new.varargElementType = remapType(old.varargElementType)
        new.copyAuxData(old)
        return new
    }

    open fun copySpreadElement(old: BirSpreadElement): BirSpreadElement {
        val new = BirSpreadElementImpl(
            sourceSpan = old.sourceSpan,
            expression = copyElement(old.expression),
        )
        new.copyAuxData(old)
        return new
    }

    open fun copyWhen(old: BirWhen): BirWhen {
        val new = BirWhenImpl(
            sourceSpan = old.sourceSpan,
            type = BirUninitializedType,
            origin = old.origin,
        )
        new.copyAttributes(old)
        new.branches.copyElements(old.branches)
        new.type = remapType(old.type)
        new.copyAuxData(old)
        return new
    }

    open fun copyBranch(old: BirBranch): BirBranch {
        val new = BirBranchImpl(
            sourceSpan = old.sourceSpan,
            condition = copyElement(old.condition),
            result = copyElement(old.result),
        )
        new.copyAuxData(old)
        return new
    }

    open fun copyElseBranch(old: BirElseBranch): BirElseBranch {
        val new = BirElseBranchImpl(
            sourceSpan = old.sourceSpan,
            condition = copyElement(old.condition),
            result = copyElement(old.result),
        )
        new.copyAuxData(old)
        return new
    }
}
