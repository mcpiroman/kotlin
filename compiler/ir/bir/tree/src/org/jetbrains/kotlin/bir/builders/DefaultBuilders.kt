/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.builders

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.declarations.impl.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.expressions.impl.*
import org.jetbrains.kotlin.bir.symbols.*
import org.jetbrains.kotlin.bir.types.BirUninitializedType
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirAnonymousInitializer.Companion.build(init: BirAnonymousInitializerImpl.() -> Unit): BirAnonymousInitializerImpl =
    BirAnonymousInitializerImpl(
        sourceSpan = SourceSpan.UNDEFINED,
        annotations = emptyList(),
        _descriptor = null,
        origin = IrDeclarationOrigin.DEFINED,
        isStatic = false,
        body = BirBlockBodyImpl(SourceSpan.UNDEFINED),
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirConstructor.Companion.build(init: BirConstructorImpl.() -> Unit): BirConstructorImpl =
    BirConstructorImpl(
        sourceSpan = SourceSpan.UNDEFINED,
        name = UninitializedName,
        _descriptor = null,
        origin = IrDeclarationOrigin.DEFINED,
        annotations = emptyList(),
        visibility = DescriptorVisibilities.PUBLIC,
        dispatchReceiverParameter = null,
        extensionReceiverParameter = null,
        contextReceiverParametersCount = 0,
        body = null,
        returnType = BirUninitializedType,
        isExternal = false,
        isInline = false,
        isExpect = false,
        isPrimary = false,
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirClass.Companion.build(init: BirClassImpl.() -> Unit): BirClassImpl =
    BirClassImpl(
        sourceSpan = SourceSpan.UNDEFINED,
        name = UninitializedName,
        _descriptor = null,
        origin = IrDeclarationOrigin.DEFINED,
        source = SourceElement.NO_SOURCE,
        annotations = emptyList(),
        kind = ClassKind.CLASS,
        visibility = DescriptorVisibilities.PUBLIC,
        modality = Modality.FINAL,
        superTypes = emptyList(),
        thisReceiver = null,
        isExternal = false,
        isExpect = false,
        isCompanion = false,
        isData = false,
        isFun = false,
        isInner = false,
        isValue = false,
        valueClassRepresentation = null,
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirEnumEntry.Companion.build(init: BirEnumEntryImpl.() -> Unit): BirEnumEntryImpl =
    BirEnumEntryImpl(
        sourceSpan = SourceSpan.UNDEFINED,
        name = UninitializedName,
        annotations = emptyList(),
        _descriptor = null,
        origin = IrDeclarationOrigin.DEFINED,
        initializerExpression = null,
        correspondingClass = null,
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirField.Companion.build(init: BirFieldImpl.() -> Unit): BirFieldImpl =
    BirFieldImpl(
        sourceSpan = SourceSpan.UNDEFINED,
        name = UninitializedName,
        _descriptor = null,
        origin = IrDeclarationOrigin.DEFINED,
        annotations = emptyList(),
        visibility = DescriptorVisibilities.PUBLIC,
        isExternal = false,
        type = BirUninitializedType,
        isFinal = false,
        isStatic = false,
        correspondingProperty = null,
        initializer = null,
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirLocalDelegatedProperty.Companion.build(init: BirLocalDelegatedPropertyImpl.() -> Unit): BirLocalDelegatedPropertyImpl =
    BirLocalDelegatedPropertyImpl(
        sourceSpan = SourceSpan.UNDEFINED,
        name = UninitializedName,
        _descriptor = null,
        origin = IrDeclarationOrigin.DEFINED,
        annotations = emptyList(),
        type = BirUninitializedType,
        isVar = false,
        delegate = BirVariable.build {},
        getter = BirSimpleFunction.build { },
        setter = null,
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirProperty.Companion.build(init: BirPropertyImpl.() -> Unit): BirPropertyImpl =
    BirPropertyImpl(
        sourceSpan = SourceSpan.UNDEFINED,
        name = UninitializedName,
        _descriptor = null,
        origin = IrDeclarationOrigin.DEFINED,
        annotations = emptyList(),
        visibility = DescriptorVisibilities.PUBLIC,
        modality = Modality.FINAL,
        isExternal = false,
        isExpect = false,
        backingField = null,
        getter = null,
        setter = null,
        isFakeOverride = false,
        overriddenSymbols = emptyList(),
        isVar = false,
        isConst = false,
        isLateinit = false,
        isDelegated = false,
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirSimpleFunction.Companion.build(init: BirSimpleFunctionImpl.() -> Unit): BirSimpleFunctionImpl =
    BirSimpleFunctionImpl(
        sourceSpan = SourceSpan.UNDEFINED,
        name = UninitializedName,
        _descriptor = null,
        origin = IrDeclarationOrigin.DEFINED,
        annotations = emptyList(),
        visibility = DescriptorVisibilities.PUBLIC,
        modality = Modality.FINAL,
        dispatchReceiverParameter = null,
        extensionReceiverParameter = null,
        contextReceiverParametersCount = 0,
        body = null,
        returnType = BirUninitializedType,
        isExternal = false,
        isInline = false,
        isExpect = false,
        isFakeOverride = false,
        isTailrec = false,
        isSuspend = false,
        isOperator = false,
        isInfix = false,
        overriddenSymbols = emptyList(),
        correspondingProperty = null,
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirTypeParameter.Companion.build(init: BirTypeParameterImpl.() -> Unit): BirTypeParameterImpl =
    BirTypeParameterImpl(
        sourceSpan = SourceSpan.UNDEFINED,
        name = UninitializedName,
        annotations = emptyList(),
        _descriptor = null,
        origin = IrDeclarationOrigin.DEFINED,
        variance = Variance.INVARIANT,
        superTypes = emptyList(),
        isReified = false,
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirValueParameter.Companion.build(init: BirValueParameterImpl.() -> Unit): BirValueParameterImpl =
    BirValueParameterImpl(
        sourceSpan = SourceSpan.UNDEFINED,
        name = UninitializedName,
        annotations = emptyList(),
        _descriptor = null,
        origin = IrDeclarationOrigin.DEFINED,
        type = BirUninitializedType,
        isAssignable = false,
        varargElementType = null,
        isCrossinline = false,
        isNoinline = false,
        isHidden = false,
        defaultValue = null,
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirVariable.Companion.build(init: BirVariableImpl.() -> Unit): BirVariableImpl =
    BirVariableImpl(
        sourceSpan = SourceSpan.UNDEFINED,
        name = UninitializedName,
        _descriptor = null,
        origin = IrDeclarationOrigin.DEFINED,
        annotations = emptyList(),
        type = BirUninitializedType,
        isAssignable = true,
        isVar = false,
        isConst = false,
        isLateinit = false,
        initializer = null,
    ).apply(init)


@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirBlockBody.Companion.build(init: BirBlockBodyImpl.() -> Unit): BirBlockBodyImpl =
    BirBlockBodyImpl(
        SourceSpan.UNDEFINED
    ).apply(init)


@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirBlock.Companion.build(init: BirBlockImpl.() -> Unit): BirBlockImpl =
    BirBlockImpl(
        SourceSpan.UNDEFINED,
        type = BirUninitializedType,
        origin = null,
    ).apply(init)


@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirCall.Companion.build(init: BirCallImpl.() -> Unit) =
    BirCallImpl(
        SourceSpan.UNDEFINED,
        type = BirUninitializedType,
        target = UninitializedBirSymbol.SimpleFunctionSymbol,
        dispatchReceiver = null,
        extensionReceiver = null,
        origin = null,
        typeArguments = emptyList(),
        contextReceiversCount = 0,
        superQualifier = null,
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirConstructorCall.Companion.build(init: BirConstructorCallImpl.() -> Unit) =
    BirConstructorCallImpl(
        SourceSpan.UNDEFINED,
        type = BirUninitializedType,
        target = UninitializedBirSymbol.ConstructorSymbol,
        dispatchReceiver = null,
        extensionReceiver = null,
        origin = null,
        source = SourceElement.NO_SOURCE,
        typeArguments = emptyList(),
        constructorTypeArgumentsCount = 0,
        contextReceiversCount = 0,
    ).apply(init)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirConst.Companion.build(init: BirConstImpl<Any?>.() -> Unit) =
    BirConstImpl<Any?>(
        SourceSpan.UNDEFINED,
        type = BirUninitializedType,
        kind = IrConstKind.Null as IrConstKind<Any?>,
        value = null,
    ).apply(init)

fun BirNoExpression() = BirNoExpressionImpl(SourceSpan.UNDEFINED, BirUninitializedType)

private val UninitializedName = Name.identifier("UNNAMED")

private abstract class UninitializedBirSymbol<E : BirElement>() : BirPossiblyElementSymbol<E> {
    final override val signature: IdSignature
        get() = error("Not bound")

    object FileSymbol : UninitializedBirSymbol<BirFile>(), BirFileSymbol
    object ExternalPackageFragmentSymbol : UninitializedBirSymbol<BirExternalPackageFragment>(), BirExternalPackageFragmentSymbol
    object AnonymousInitializerSymbol : UninitializedBirSymbol<BirAnonymousInitializer>(), BirAnonymousInitializerSymbol
    object EnumEntrySymbol : UninitializedBirSymbol<BirEnumEntry>(), BirEnumEntrySymbol
    object FieldSymbol : UninitializedBirSymbol<BirField>(), BirFieldSymbol
    object ClassSymbol : UninitializedBirSymbol<BirClass>(), BirClassSymbol
    object ScriptSymbol : UninitializedBirSymbol<BirScript>(), BirScriptSymbol
    object TypeParameterSymbol : UninitializedBirSymbol<BirTypeParameter>(), BirTypeParameterSymbol
    object ValueParameterSymbol : UninitializedBirSymbol<BirValueParameter>(), BirValueParameterSymbol
    object VariableSymbol : UninitializedBirSymbol<BirVariable>(), BirVariableSymbol
    object ConstructorSymbol : UninitializedBirSymbol<BirConstructor>(), BirConstructorSymbol
    object SimpleFunctionSymbol : UninitializedBirSymbol<BirSimpleFunction>(), BirSimpleFunctionSymbol
    object ReturnableBlockSymbol : UninitializedBirSymbol<BirReturnableBlock>(), BirReturnableBlockSymbol
    object PropertySymbol : UninitializedBirSymbol<BirProperty>(), BirPropertySymbol
    object LocalDelegatedPropertySymbol : UninitializedBirSymbol<BirLocalDelegatedProperty>(), BirLocalDelegatedPropertySymbol
    object TypeAliasSymbol : UninitializedBirSymbol<BirTypeAlias>(), BirTypeAliasSymbol
}
