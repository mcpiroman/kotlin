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
import org.jetbrains.kotlin.utils.mapOrTakeThisIfIdentity
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import java.util.*

context (BirTreeContext)
fun <E : BirElement> E.deepCopy(
    copier: BirTreeDeepCopier = BirTreeDeepCopier(),
): E {
    return copier.copyTree(this)
}

context (BirTreeContext)
@OptIn(ObsoleteDescriptorBasedAPI::class)
open class BirTreeDeepCopier() {
    protected var rootElement: BirElementBase? = null
    private var lastDeferredInitialization: (() -> Unit)? = null

    protected val modules by lazy(LazyThreadSafetyMode.NONE) { createElementMap<BirModuleFragment>() }
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

    open protected fun <E : BirElement> createElementMap(): MutableMap<E, E> = IdentityHashMap<E, E>()


    fun <E : BirElement> copyElement(old: E): E {
        val new = doCopyElement(old)
        ensureLastElementIsFinished()
        return new
    }

    fun <E : BirElement> copyElementPossiblyUnfinished(old: E): E {
        return doCopyElement(old)
    }

    private fun deferInitialization(initialize: () -> Unit) {
        ensureLastElementIsFinished()
        lastDeferredInitialization = initialize
    }

    fun ensureLastElementIsFinished() {
        lastDeferredInitialization?.let {
            lastDeferredInitialization = null
            it()
        }
    }

    open protected fun <E : BirElement> copyNotReferencedElement(old: E, copy: () -> E): E = copy()

    open protected fun <ME : BirElement, SE : ME> copyReferencedElement(
        old: SE,
        map: MutableMap<ME, ME>,
        copy: () -> SE,
    ): SE {
        return map.computeIfAbsent(old) {
            copy()
        } as SE
    }

    protected fun <E : BirElement> BirChildElementList<E>.copyElements(from: BirChildElementList<E>) {
        for (element in from) {
            this += copyElementPossiblyUnfinished(element)
        }
    }

    open protected fun BirElementBase.copyAuxData(from: BirElementBase) {
        tmpCopyAuxData(from)
    }

    protected fun BirAttributeContainer.copyAttributes(other: BirAttributeContainer) {
        attributeOwnerId = other.attributeOwnerId
        //todo: originalBeforeInline = other.originalBeforeInline
    }

    fun <E : BirElement> remapElement(old: E): E {
        old as BirElementBase
        val rootElement = rootElement
        return if (rootElement == null || old === rootElement || rootElement.isAncestorOf(old))
            copyElement(old)
        else old
    }

    open fun <S : BirSymbol> remapSymbol(old: S): S {
        if (old is BirElementBase) {
            return remapElement(old)
        } else {
            return old
        }
    }

    // unlike the impl at org.jetbrains.kotlin.ir.util.DeepCopyTypeRemapper,
    //  this also remaps classes of types other than BirSimpleType
    // todo: check what's bout `annotation` - esp. how they can be copied/reused
    open fun remapType(old: BirType): BirType = when (old) {
        is BirSimpleType -> remapSimpleType(old)
        is BirDynamicType -> old
        is BirErrorType -> old
        else -> TODO(old.toString())
    }

    protected open fun remapSimpleType(old: BirSimpleType): BirSimpleType {
        val classifier = remapSymbol(old.classifier)
        val arguments = old.arguments.mapOrTakeThisIfIdentity { remapTypeArgument(it) }
        val abbreviation = old.abbreviation?.let { remapTypeAbbreviation(it) }

        return if (classifier === old.classifier && arguments === old.arguments && abbreviation === old.abbreviation) {
            old
        } else {
            BirSimpleTypeImpl(
                old.kotlinType,
                classifier,
                old.nullability,
                arguments,
                old.annotations.memoryOptimizedMap { copyElement(it) },
                abbreviation,
            )
        }
    }

    open fun remapTypeArgument(old: BirTypeArgument): BirTypeArgument = when (old) {
        is BirStarProjection -> old
        is BirTypeProjectionImpl -> {
            val newType = remapType(old.type)
            if (newType === old.type) {
                old
            } else {
                BirTypeProjectionImpl(newType, old.variance)
            }
        }
        is BirType -> remapType(old) as BirTypeArgument
        else -> error(old)
    }

    open fun remapTypeAbbreviation(old: BirTypeAbbreviation): BirTypeAbbreviation {
        val typeAlias = remapSymbol(old.typeAlias)
        val arguments = old.arguments.mapOrTakeThisIfIdentity { remapTypeArgument(it) }

        return if (typeAlias === old.typeAlias && arguments === old.arguments) {
            old
        } else {
            BirTypeAbbreviation(
                typeAlias,
                old.hasQuestionMark,
                arguments,
                old.annotations.memoryOptimizedMap { copyElement(it) },
            )
        }
    }

    fun <T : BirElement> copyTree(root: T): T {
        require(rootElement == null) { "Trying to recursively copy a tree" }
        rootElement = root as BirElementBase
        val new = copyElement(root)
        rootElement = null
        return new
    }


    protected open fun <T : BirElement> doCopyElement(old: T): T = when (old) {
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
        is BirNoExpression -> copyNoExpression(old)
        else -> error(old)
    } as T

    open fun copyValueParameter(old: BirValueParameter): BirValueParameter = copyReferencedElement(old, valueParameters) {
        val new = BirValueParameterImpl(
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
        new.copyAuxData(old)
        deferInitialization {
            new.defaultValue = old.defaultValue?.let { copyElementPossiblyUnfinished(it) }
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
            new.type = remapType(old.type)
            new.varargElementType = old.varargElementType?.let { remapType(it) }
        }
        new
    }

    open fun copyClass(old: BirClass): BirClass = copyReferencedElement(old, classes) {
        val new = BirClassImpl(
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
        new.copyAuxData(old)
        deferInitialization {
            new.copyAttributes(old)
            new.thisReceiver = old.thisReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.typeParameters.copyElements(old.typeParameters)
            new.declarations.copyElements(old.declarations)
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
            new.superTypes = old.superTypes.memoryOptimizedMap { remapType(it) }
            new.valueClassRepresentation = old.valueClassRepresentation?.mapUnderlyingType { remapType(it) as BirSimpleType }
        }
        new
    }

    open fun copyAnonymousInitializer(old: BirAnonymousInitializer): BirAnonymousInitializer = copyNotReferencedElement(old) {
        val new = BirAnonymousInitializerImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            isStatic = old.isStatic,
            body = copyElementPossiblyUnfinished(old.body),
        )
        new.copyAuxData(old)
        deferInitialization {
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
        }
        new
    }

    open fun copyTypeParameter(old: BirTypeParameter): BirTypeParameter = copyReferencedElement(old, typeParameters) {
        val new = BirTypeParameterImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            name = old.name,
            variance = old.variance,
            isReified = old.isReified,
            superTypes = emptyList(),
        )
        new.copyAuxData(old)
        deferInitialization {
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
            new.superTypes = old.superTypes.memoryOptimizedMap { remapType(it) }
        }
        new
    }

    open fun copyConstructor(old: BirConstructor): BirConstructor = copyReferencedElement(old, constructors) {
        val new = BirConstructorImpl(
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
        new.copyAuxData(old)
        deferInitialization {
            new.dispatchReceiverParameter = old.dispatchReceiverParameter?.let { copyElementPossiblyUnfinished(it) }
            new.extensionReceiverParameter = old.extensionReceiverParameter?.let { copyElementPossiblyUnfinished(it) }
            new.valueParameters.copyElements(old.valueParameters)
            new.body = old.body?.let { copyElementPossiblyUnfinished(it) }
            new.typeParameters.copyElements(old.typeParameters)
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
            new.returnType = remapType(old.returnType)
        }
        new
    }

    open fun copyEnumEntry(old: BirEnumEntry): BirEnumEntry = copyReferencedElement(old, enumEntries) {
        val new = BirEnumEntryImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            name = old.name,
            initializerExpression = null,
            correspondingClass = null,
        )
        new.copyAuxData(old)
        deferInitialization {
            new.initializerExpression = old.initializerExpression?.let { copyElementPossiblyUnfinished(it) }
            new.correspondingClass = old.correspondingClass?.let { copyElementPossiblyUnfinished(it) }
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
        }
        new
    }

    open fun copyErrorDeclaration(old: BirErrorDeclaration): BirErrorDeclaration = copyNotReferencedElement(old) {
        val new = BirErrorDeclarationImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
        )
        new.copyAuxData(old)
        deferInitialization {
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
        }
        new
    }

    open fun copyFunctionWithLateBinding(old: BirFunctionWithLateBindingImpl): BirFunctionWithLateBindingImpl =
        copyReferencedElement(old, functions) {
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
            new.copyAuxData(old)
            deferInitialization {
                new.copyAttributes(old)
                new.dispatchReceiverParameter = old.dispatchReceiverParameter?.let { copyElementPossiblyUnfinished(it) }
                new.extensionReceiverParameter = old.extensionReceiverParameter?.let { copyElementPossiblyUnfinished(it) }
                new.valueParameters.copyElements(old.valueParameters)
                new.body = old.body?.let { copyElementPossiblyUnfinished(it) }
                new.typeParameters.copyElements(old.typeParameters)
                new.correspondingProperty = old.correspondingProperty?.let { remapSymbol(it) }
                new.overriddenSymbols = old.overriddenSymbols.memoryOptimizedMap { remapSymbol(it) }
                new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
                new.returnType = remapType(old.returnType)
            }

            new
        }

    open fun copyPropertyWithLateBinding(old: BirPropertyWithLateBindingImpl): BirPropertyWithLateBindingImpl =
        copyReferencedElement(old, properties) {
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
            new.copyAuxData(old)
            deferInitialization {
                new.copyAttributes(old)
                new.getter = old.getter?.let { copyElementPossiblyUnfinished(it) }
                new.setter = old.setter?.let { copyElementPossiblyUnfinished(it) }
                new.backingField = old.backingField?.let { copyElementPossiblyUnfinished(it) }
                new.overriddenSymbols = old.overriddenSymbols.memoryOptimizedMap { remapSymbol(it) }
                new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
            }

            new
        }

    open fun copyField(old: BirField): BirField = copyReferencedElement(old, fields) {
        val new = BirFieldImpl(
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
        new.copyAuxData(old)
        deferInitialization {
            new.initializer = old.initializer?.let { copyElementPossiblyUnfinished(it) }
            new.correspondingProperty = old.correspondingProperty?.let { remapSymbol(it) }
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
            new.type = remapType(old.type)
        }
        new
    }

    open fun copyLocalDelegatedProperty(old: BirLocalDelegatedProperty): BirLocalDelegatedProperty =
        copyReferencedElement(old, localDelegatedProperties) {
            val new = BirLocalDelegatedPropertyImpl(
                sourceSpan = old.sourceSpan,
                annotations = emptyList(),
                _descriptor = old._descriptor,
                origin = old.origin,
                name = old.name,
                type = BirUninitializedType,
                isVar = old.isVar,
                delegate = copyElementPossiblyUnfinished(old.delegate),
                getter = copyElementPossiblyUnfinished(old.getter),
                setter = null,
            )
            new.copyAuxData(old)
            deferInitialization {
                new.setter = old.setter?.let { copyElementPossiblyUnfinished(it) }
                new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
                new.type = remapType(old.type)
            }

            new
        }

    open fun copyModuleFragment(old: BirModuleFragment): BirModuleFragment = copyReferencedElement(old, modules) {
        val new = BirModuleFragmentImpl(
            sourceSpan = old.sourceSpan,
            _descriptor = old._descriptor,
            name = old.name,
        )
        new.copyAuxData(old)
        deferInitialization {
            new.files.copyElements(old.files)
        }
        new
    }

    open fun copyProperty(old: BirProperty): BirProperty = copyReferencedElement(old, properties) {
        val new = BirPropertyImpl(
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
        new.copyAuxData(old)
        deferInitialization {
            new.copyAttributes(old)
            new.backingField = old.backingField?.let { copyElementPossiblyUnfinished(it) }
            new.getter = old.getter?.let { copyElementPossiblyUnfinished(it) }
            new.setter = old.setter?.let { copyElementPossiblyUnfinished(it) }
            new.overriddenSymbols = old.overriddenSymbols.memoryOptimizedMap { remapSymbol(it) }
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
        }
        new
    }

    open fun copyScript(old: BirScript): BirScript = copyReferencedElement(old, scripts) {
        val new = BirScriptImpl(
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
        new.copyAuxData(old)
        deferInitialization {
            new.thisReceiver = old.thisReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.explicitCallParameters.copyElements(old.explicitCallParameters)
            new.implicitReceiversParameters.copyElements(old.implicitReceiversParameters)
            new.providedPropertiesParameters.copyElements(old.providedPropertiesParameters)
            new.earlierScriptsParameter = old.earlierScriptsParameter?.let { copyElementPossiblyUnfinished(it) }
            new.constructor = old.constructor?.let { remapElement(it) }
            new.statements.copyElements(old.statements)
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
            new.baseClass = old.baseClass?.let { remapType(it) }
        }
        new
    }

    open fun copySimpleFunction(old: BirSimpleFunction): BirSimpleFunction = copyReferencedElement(old, functions) {
        val new = BirSimpleFunctionImpl(
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
        new.copyAuxData(old)
        deferInitialization {
            new.copyAttributes(old)
            new.dispatchReceiverParameter = old.dispatchReceiverParameter?.let { copyElementPossiblyUnfinished(it) }
            new.extensionReceiverParameter = old.extensionReceiverParameter?.let { copyElementPossiblyUnfinished(it) }
            new.valueParameters.copyElements(old.valueParameters)
            new.body = old.body?.let { copyElementPossiblyUnfinished(it) }
            new.typeParameters.copyElements(old.typeParameters)
            new.overriddenSymbols = old.overriddenSymbols.memoryOptimizedMap { remapSymbol(it) }
            new.correspondingProperty = old.correspondingProperty?.let { remapSymbol(it) }
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
            new.returnType = remapType(old.returnType)
        }
        new
    }

    open fun copyTypeAlias(old: BirTypeAlias): BirTypeAlias = copyReferencedElement(old, typeAliases) {
        val new = BirTypeAliasImpl(
            sourceSpan = old.sourceSpan,
            annotations = emptyList(),
            _descriptor = old._descriptor,
            origin = old.origin,
            name = old.name,
            visibility = old.visibility,
            isActual = old.isActual,
            expandedType = BirUninitializedType,
        )
        new.copyAuxData(old)
        deferInitialization {
            new.typeParameters.copyElements(old.typeParameters)
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
            new.expandedType = remapType(old.expandedType)
        }
        new
    }

    open fun copyVariable(old: BirVariable): BirVariable = copyReferencedElement(old, variables) {
        val new = BirVariableImpl(
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
        new.copyAuxData(old)
        deferInitialization {
            new.initializer = old.initializer?.let { copyElementPossiblyUnfinished(it) }
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
            new.type = remapType(old.type)
        }
        new
    }

    open fun copyExternalPackageFragment(old: BirExternalPackageFragment): BirExternalPackageFragment =
        copyReferencedElement(old, externalPackageFragments) {
            val new = BirExternalPackageFragmentImpl(
                sourceSpan = old.sourceSpan,
                _descriptor = old._descriptor,
                fqName = old.fqName,
                containerSource = old.containerSource,
            )
            new.copyAuxData(old)
            deferInitialization {
                new.declarations.copyElements(old.declarations)
            }

            new
        }

    open fun copyFile(old: BirFile): BirFile = copyReferencedElement(old, files) {
        val new = BirFileImpl(
            sourceSpan = old.sourceSpan,
            _descriptor = old._descriptor,
            fqName = old.fqName,
            annotations = emptyList(),
            fileEntry = old.fileEntry,
        )
        new.copyAuxData(old)
        deferInitialization {
            new.declarations.copyElements(old.declarations)
            new.annotations = old.annotations.memoryOptimizedMap { copyElementPossiblyUnfinished(it) }
        }
        new
    }

    open fun copyExpressionBody(old: BirExpressionBody): BirExpressionBody = copyNotReferencedElement(old) {
        val new = BirExpressionBodyImpl(
            sourceSpan = old.sourceSpan,
            expression = copyElementPossiblyUnfinished(old.expression),
        )
        new.copyAuxData(old)
        new
    }

    open fun copyBlockBody(old: BirBlockBody): BirBlockBody = copyNotReferencedElement(old) {
        val new = BirBlockBodyImpl(
            sourceSpan = old.sourceSpan,
        )
        new.copyAuxData(old)
        deferInitialization {
            new.statements.copyElements(old.statements)
        }
        new
    }

    open fun copyConstructorCall(old: BirConstructorCall): BirConstructorCall = copyNotReferencedElement(old) {
        val new = BirConstructorCallImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } },
            contextReceiversCount = old.contextReceiversCount,
            source = old.source,
            constructorTypeArgumentsCount = old.constructorTypeArgumentsCount,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.dispatchReceiver = old.dispatchReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.extensionReceiver = old.extensionReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.valueArguments.copyElements(old.valueArguments)
        }
        new
    }

    open fun copyGetObjectValue(old: BirGetObjectValue): BirGetObjectValue = copyNotReferencedElement(old) {
        val new = BirGetObjectValueImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapSymbol(old.target),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyGetEnumValue(old: BirGetEnumValue): BirGetEnumValue = copyNotReferencedElement(old) {
        val new = BirGetEnumValueImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapSymbol(old.target),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyRawFunctionReference(old: BirRawFunctionReference): BirRawFunctionReference = copyNotReferencedElement(old) {
        val new = BirRawFunctionReferenceImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapSymbol(old.target),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyBlock(old: BirBlock): BirBlock = copyNotReferencedElement(old) {
        val new = BirBlockImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            origin = old.origin,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.statements.copyElements(old.statements)
        }
        new
    }

    open fun copyComposite(old: BirComposite): BirComposite = copyNotReferencedElement(old) {
        val new = BirCompositeImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            origin = old.origin,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.statements.copyElements(old.statements)
        }
        new
    }

    open fun copyReturnableBlock(old: BirReturnableBlock): BirReturnableBlock = copyReferencedElement(old, returnableBlocks) {
        val new = BirReturnableBlockImpl(
            sourceSpan = old.sourceSpan,
            _descriptor = old._descriptor,
            type = remapType(old.type),
            origin = old.origin,
        )
        new.copyAuxData(old)
        deferInitialization {
            new.copyAttributes(old)
            new.statements.copyElements(old.statements)
        }
        new
    }

    open fun copyInlinedFunctionBlock(old: BirInlinedFunctionBlock): BirInlinedFunctionBlock = copyNotReferencedElement(old) {
        val new = BirInlinedFunctionBlockImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            origin = old.origin,
            inlineCall = old.inlineCall, // no remap
            inlinedElement = old.inlinedElement, // no remap
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.statements.copyElements(old.statements)
        }
        new
    }

    open fun copySyntheticBody(old: BirSyntheticBody): BirSyntheticBody = copyNotReferencedElement(old) {
        val new = BirSyntheticBodyImpl(
            sourceSpan = old.sourceSpan,
            kind = old.kind,
        )
        new.copyAuxData(old)
        new
    }

    open fun copyBreak(old: BirBreak): BirBreak = copyNotReferencedElement(old) {
        val new = BirBreakImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            loop = remapElement(old.loop),
            label = old.label,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyContinue(old: BirContinue): BirContinue = copyNotReferencedElement(old) {
        val new = BirContinueImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            loop = remapElement(old.loop),
            label = old.label,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyCall(old: BirCall): BirCall = copyNotReferencedElement(old) {
        val new = BirCallImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } },
            contextReceiversCount = old.contextReceiversCount,
            superQualifier = old.superQualifier?.let { remapSymbol(it) },
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.dispatchReceiver = old.dispatchReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.extensionReceiver = old.extensionReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.valueArguments.copyElements(old.valueArguments)
        }
        new
    }

    open fun copyFunctionReference(old: BirFunctionReference): BirFunctionReference = copyNotReferencedElement(old) {
        val new = BirFunctionReferenceImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } },
            reflectionTarget = old.reflectionTarget?.let { remapSymbol(it) },
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.dispatchReceiver = old.dispatchReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.extensionReceiver = old.extensionReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.valueArguments.copyElements(old.valueArguments)
        }
        new
    }

    open fun copyPropertyReference(old: BirPropertyReference): BirPropertyReference = copyNotReferencedElement(old) {
        val new = BirPropertyReferenceImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } },
            field = old.field?.let { remapSymbol(it) },
            getter = old.getter?.let { remapSymbol(it) },
            setter = old.setter?.let { remapSymbol(it) },
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.dispatchReceiver = old.dispatchReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.extensionReceiver = old.extensionReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.valueArguments.copyElements(old.valueArguments)
        }
        new
    }

    open fun copyLocalDelegatedPropertyReference(old: BirLocalDelegatedPropertyReference): BirLocalDelegatedPropertyReference =
        copyNotReferencedElement(old) {
            val new = BirLocalDelegatedPropertyReferenceImpl(
                sourceSpan = old.sourceSpan,
                type = remapType(old.type),
                target = remapSymbol(old.target),
                dispatchReceiver = null,
                extensionReceiver = null,
                origin = old.origin,
                typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } },
                delegate = remapElement(old.delegate),
                getter = remapSymbol(old.getter),
                setter = old.setter?.let { remapSymbol(it) },
            )
            new.copyAuxData(old)
            new.copyAttributes(old)
            deferInitialization {
                new.dispatchReceiver = old.dispatchReceiver?.let { copyElementPossiblyUnfinished(it) }
                new.extensionReceiver = old.extensionReceiver?.let { copyElementPossiblyUnfinished(it) }
                new.valueArguments.copyElements(old.valueArguments)
            }
            new
        }

    open fun copyClassReference(old: BirClassReference): BirClassReference = copyNotReferencedElement(old) {
        val new = BirClassReferenceImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapSymbol(old.target),
            classType = remapType(old.type),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun <T> copyConst(old: BirConst<T>): BirConst<T> = copyNotReferencedElement(old) {
        val new = BirConstImpl<T>(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            kind = old.kind,
            value = old.value,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyConstantPrimitive(old: BirConstantPrimitive): BirConstantPrimitive = copyNotReferencedElement(old) {
        val new = BirConstantPrimitiveImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            value = copyElementPossiblyUnfinished(old.value),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyConstantObject(old: BirConstantObject): BirConstantObject = copyNotReferencedElement(old) {
        val new = BirConstantObjectImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            constructor = remapSymbol(old.constructor),
            typeArguments = old.typeArguments.memoryOptimizedMap { remapType(it) },
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.valueArguments.copyElements(old.valueArguments)
        }
        new
    }

    open fun copyConstantArray(old: BirConstantArray): BirConstantArray = copyNotReferencedElement(old) {
        val new = BirConstantArrayImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.elements.copyElements(old.elements)
        }
        new
    }

    open fun copyDelegatingConstructorCall(old: BirDelegatingConstructorCall): BirDelegatingConstructorCall =
        copyNotReferencedElement(old) {
            val new = BirDelegatingConstructorCallImpl(
                sourceSpan = old.sourceSpan,
                type = remapType(old.type),
                target = remapSymbol(old.target),
                dispatchReceiver = null,
                extensionReceiver = null,
                origin = old.origin,
                typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } },
                contextReceiversCount = old.contextReceiversCount,
            )
            new.copyAuxData(old)
            new.copyAttributes(old)
            deferInitialization {
                new.dispatchReceiver = old.dispatchReceiver?.let { copyElementPossiblyUnfinished(it) }
                new.extensionReceiver = old.extensionReceiver?.let { copyElementPossiblyUnfinished(it) }
                new.valueArguments.copyElements(old.valueArguments)
            }
            new
        }

    open fun copyDynamicOperatorExpression(old: BirDynamicOperatorExpression): BirDynamicOperatorExpression =
        copyNotReferencedElement(old) {
            val new = BirDynamicOperatorExpressionImpl(
                sourceSpan = old.sourceSpan,
                type = remapType(old.type),
                operator = old.operator,
                receiver = copyElementPossiblyUnfinished(old.receiver),
            )
            new.copyAuxData(old)
            new.copyAttributes(old)
            deferInitialization {
                new.arguments.copyElements(old.arguments)
            }
            new
        }

    open fun copyDynamicMemberExpression(old: BirDynamicMemberExpression): BirDynamicMemberExpression = copyNotReferencedElement(old) {
        val new = BirDynamicMemberExpressionImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            memberName = old.memberName,
            receiver = copyElementPossiblyUnfinished(old.receiver),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyEnumConstructorCall(old: BirEnumConstructorCall): BirEnumConstructorCall = copyNotReferencedElement(old) {
        val new = BirEnumConstructorCallImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapSymbol(old.target),
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = old.origin,
            typeArguments = old.typeArguments.memoryOptimizedMap { it?.let { remapType(it) } },
            contextReceiversCount = old.contextReceiversCount,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.dispatchReceiver = old.dispatchReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.extensionReceiver = old.extensionReceiver?.let { copyElementPossiblyUnfinished(it) }
            new.valueArguments.copyElements(old.valueArguments)
        }
        new
    }

    open fun copyErrorCallExpression(old: BirErrorCallExpression): BirErrorCallExpression = copyNotReferencedElement(old) {
        val new = BirErrorCallExpressionImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            description = old.description,
            explicitReceiver = null,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.arguments.copyElements(old.arguments)
            new.explicitReceiver = old.explicitReceiver?.let { copyElementPossiblyUnfinished(it) }
        }
        new
    }

    open fun copyGetField(old: BirGetField): BirGetField = copyNotReferencedElement(old) {
        val new = BirGetFieldImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapSymbol(old.target),
            superQualifier = old.superQualifier?.let { remapSymbol(it) },
            receiver = null,
            origin = old.origin,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.receiver = old.receiver?.let { copyElementPossiblyUnfinished(it) }
        }
        new
    }

    open fun copySetField(old: BirSetField): BirSetField = copyNotReferencedElement(old) {
        val new = BirSetFieldImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapSymbol(old.target),
            superQualifier = old.superQualifier?.let { remapSymbol(it) },
            receiver = null,
            origin = old.origin,
            value = copyElementPossiblyUnfinished(old.value),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.receiver = old.receiver?.let { copyElementPossiblyUnfinished(it) }
        }
        new
    }

    open fun copyFunctionExpression(old: BirFunctionExpression): BirFunctionExpression = copyNotReferencedElement(old) {
        val new = BirFunctionExpressionImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            origin = old.origin,
            function = copyElementPossiblyUnfinished(old.function),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyGetClass(old: BirGetClass): BirGetClass = copyNotReferencedElement(old) {
        val new = BirGetClassImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            argument = copyElementPossiblyUnfinished(old.argument),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyInstanceInitializerCall(old: BirInstanceInitializerCall): BirInstanceInitializerCall = copyNotReferencedElement(old) {
        val new = BirInstanceInitializerCallImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            `class` = remapSymbol(old.`class`),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyWhileLoop(old: BirWhileLoop): BirWhileLoop = copyReferencedElement(old, loops) {
        val new = BirWhileLoopImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            origin = old.origin,
            body = null,
            // nb: this may be a problem if there is a ref to the loop from within the condition (language seems to not allow that however).
            //  In such case the simples solution is to do what IR does right now - make condition property lateinit.
            condition = copyElementPossiblyUnfinished(old.condition),
            label = old.label,
        )
        new.copyAuxData(old)
        deferInitialization {
            new.copyAttributes(old)
            new.body = old.body?.let { copyElementPossiblyUnfinished(it) }
        }
        new
    }

    open fun copyDoWhileLoop(old: BirDoWhileLoop): BirDoWhileLoop = copyReferencedElement(old, loops) {
        val new = BirDoWhileLoopImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            origin = old.origin,
            body = null,
            condition = copyElementPossiblyUnfinished(old.condition),
            label = old.label,
        )
        new.copyAuxData(old)
        deferInitialization {
            new.copyAttributes(old)
            new.body = old.body?.let { copyElementPossiblyUnfinished(it) }
        }
        new
    }

    open fun copyReturn(old: BirReturn): BirReturn = copyNotReferencedElement(old) {
        val new = BirReturnImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            value = copyElementPossiblyUnfinished(old.value),
            returnTarget = remapSymbol(old.returnTarget),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyStringConcatenation(old: BirStringConcatenation): BirStringConcatenation = copyNotReferencedElement(old) {
        val new = BirStringConcatenationImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.arguments.copyElements(old.arguments)
        }
        new
    }

    open fun copySuspensionPoint(old: BirSuspensionPoint): BirSuspensionPoint = copyNotReferencedElement(old) {
        val new = BirSuspensionPointImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            suspensionPointIdParameter = copyElementPossiblyUnfinished(old.suspensionPointIdParameter),
            result = copyElementPossiblyUnfinished(old.result),
            resumeResult = copyElementPossiblyUnfinished(old.resumeResult),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copySuspendableExpression(old: BirSuspendableExpression): BirSuspendableExpression = copyNotReferencedElement(old) {
        val new = BirSuspendableExpressionImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            suspensionPointId = copyElementPossiblyUnfinished(old.suspensionPointId),
            result = copyElementPossiblyUnfinished(old.result),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyThrow(old: BirThrow): BirThrow = copyNotReferencedElement(old) {
        val new = BirThrowImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            value = copyElementPossiblyUnfinished(old.value),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyTry(old: BirTry): BirTry = copyNotReferencedElement(old) {
        val new = BirTryImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            tryResult = copyElementPossiblyUnfinished(old.tryResult),
            finallyExpression = null,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.catches.copyElements(old.catches)
            new.finallyExpression = old.finallyExpression?.let { copyElementPossiblyUnfinished(it) }
        }
        new
    }

    open fun copyCatch(old: BirCatch): BirCatch = copyNotReferencedElement(old) {
        val new = BirCatchImpl(
            sourceSpan = old.sourceSpan,
            catchParameter = copyElementPossiblyUnfinished(old.catchParameter),
            result = copyElementPossiblyUnfinished(old.result),
        )
        new.copyAuxData(old)
        new
    }

    open fun copyTypeOperatorCall(old: BirTypeOperatorCall): BirTypeOperatorCall = copyNotReferencedElement(old) {
        val new = BirTypeOperatorCallImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            operator = old.operator,
            argument = copyElementPossiblyUnfinished(old.argument),
            typeOperand = remapType(old.typeOperand),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyGetValue(old: BirGetValue): BirGetValue = copyNotReferencedElement(old) {
        val new = BirGetValueImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapElement(old.target),
            origin = old.origin,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copySetValue(old: BirSetValue): BirSetValue = copyNotReferencedElement(old) {
        val new = BirSetValueImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            target = remapElement(old.target),
            origin = old.origin,
            value = copyElementPossiblyUnfinished(old.value),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        new
    }

    open fun copyVararg(old: BirVararg): BirVararg = copyNotReferencedElement(old) {
        val new = BirVarargImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            varargElementType = remapType(old.varargElementType),
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.elements.copyElements(old.elements)
        }
        new
    }

    open fun copySpreadElement(old: BirSpreadElement): BirSpreadElement = copyNotReferencedElement(old) {
        val new = BirSpreadElementImpl(
            sourceSpan = old.sourceSpan,
            expression = copyElementPossiblyUnfinished(old.expression),
        )
        new.copyAuxData(old)
        new
    }

    open fun copyWhen(old: BirWhen): BirWhen = copyNotReferencedElement(old) {
        val new = BirWhenImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
            origin = old.origin,
        )
        new.copyAuxData(old)
        new.copyAttributes(old)
        deferInitialization {
            new.branches.copyElements(old.branches)
        }
        new
    }

    open fun copyBranch(old: BirBranch): BirBranch = copyNotReferencedElement(old) {
        val new = BirBranchImpl(
            sourceSpan = old.sourceSpan,
            condition = copyElementPossiblyUnfinished(old.condition),
            result = copyElementPossiblyUnfinished(old.result),
        )
        new.copyAuxData(old)
        new
    }

    open fun copyElseBranch(old: BirElseBranch): BirElseBranch = copyNotReferencedElement(old) {
        val new = BirElseBranchImpl(
            sourceSpan = old.sourceSpan,
            condition = copyElementPossiblyUnfinished(old.condition),
            result = copyElementPossiblyUnfinished(old.result),
        )
        new.copyAuxData(old)
        new
    }

    open fun copyNoExpression(old: BirNoExpression): BirNoExpression = copyNotReferencedElement(old) {
        val new = BirNoExpressionImpl(
            sourceSpan = old.sourceSpan,
            type = remapType(old.type),
        )
        new.copyAuxData(old)
        new
    }
}
