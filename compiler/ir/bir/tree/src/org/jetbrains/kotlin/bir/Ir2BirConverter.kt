/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/ir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.declarations.impl.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.expressions.impl.*
import org.jetbrains.kotlin.bir.types.BirSimpleType
import org.jetbrains.kotlin.bir.types.BirUninitializedType
import org.jetbrains.kotlin.descriptors.InlineClassRepresentation
import org.jetbrains.kotlin.descriptors.MultiFieldValueClassRepresentation
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionWithLateBindingImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrPropertyWithLateBindingImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.IrSimpleType

@ObsoleteDescriptorBasedAPI
class Ir2BirConverter : Ir2BirConverterBase() {
    context(BirTreeContext)
    override fun convertIrElement(ir: IrElement): BirElement = when (ir) {
        is IrValueParameter -> convertValueParameter(ir)
        is IrClass -> convertClass(ir)
        is IrAnonymousInitializer -> convertAnonymousInitializer(ir)
        is IrTypeParameter -> convertTypeParameter(ir)
        is IrConstructor -> convertConstructor(ir)
        is IrEnumEntry -> convertEnumEntry(ir)
        is IrErrorDeclaration -> convertErrorDeclaration(ir)
        is IrFunctionWithLateBindingImpl -> convertFunctionWithLateBinding(ir)
        is IrPropertyWithLateBindingImpl -> convertPropertyWithLateBinding(ir)
        is IrField -> convertField(ir)
        is IrLocalDelegatedProperty -> convertLocalDelegatedProperty(ir)
        is IrModuleFragment -> convertModuleFragment(ir)
        is IrProperty -> convertProperty(ir)
        is IrScript -> convertScript(ir)
        is IrSimpleFunction -> convertSimpleFunction(ir)
        is IrTypeAlias -> convertTypeAlias(ir)
        is IrVariable -> convertVariable(ir)
        is IrExternalPackageFragment -> convertExternalPackageFragment(ir)
        is IrFile -> convertFile(ir)
        is IrExpressionBody -> convertExpressionBody(ir)
        is IrBlockBody -> convertBlockBody(ir)
        is IrConstructorCall -> convertConstructorCall(ir)
        is IrGetObjectValue -> convertGetObjectValue(ir)
        is IrGetEnumValue -> convertGetEnumValue(ir)
        is IrRawFunctionReference -> convertRawFunctionReference(ir)
        is IrComposite -> convertComposite(ir)
        is IrReturnableBlock -> convertReturnableBlock(ir)
        is IrInlinedFunctionBlock -> convertInlinedFunctionBlock(ir)
        is IrBlock -> convertBlock(ir)
        is IrSyntheticBody -> convertSyntheticBody(ir)
        is IrBreak -> convertBreak(ir)
        is IrContinue -> convertContinue(ir)
        is IrCall -> convertCall(ir)
        is IrFunctionReference -> convertFunctionReference(ir)
        is IrPropertyReference -> convertPropertyReference(ir)
        is IrLocalDelegatedPropertyReference -> convertLocalDelegatedPropertyReference(ir)
        is IrClassReference -> convertClassReference(ir)
        is IrConst<*> -> convertConst(ir)
        is IrConstantPrimitive -> convertConstantPrimitive(ir)
        is IrConstantObject -> convertConstantObject(ir)
        is IrConstantArray -> convertConstantArray(ir)
        is IrDelegatingConstructorCall -> convertDelegatingConstructorCall(ir)
        is IrDynamicOperatorExpression -> convertDynamicOperatorExpression(ir)
        is IrDynamicMemberExpression -> convertDynamicMemberExpression(ir)
        is IrEnumConstructorCall -> convertEnumConstructorCall(ir)
        is IrErrorCallExpression -> convertErrorCallExpression(ir)
        is IrGetField -> convertGetField(ir)
        is IrSetField -> convertSetField(ir)
        is IrFunctionExpression -> convertFunctionExpression(ir)
        is IrGetClass -> convertGetClass(ir)
        is IrInstanceInitializerCall -> convertInstanceInitializerCall(ir)
        is IrWhileLoop -> convertWhileLoop(ir)
        is IrDoWhileLoop -> convertDoWhileLoop(ir)
        is IrReturn -> convertReturn(ir)
        is IrStringConcatenation -> convertStringConcatenation(ir)
        is IrSuspensionPoint -> convertSuspensionPoint(ir)
        is IrSuspendableExpression -> convertSuspendableExpression(ir)
        is IrThrow -> convertThrow(ir)
        is IrTry -> convertTry(ir)
        is IrCatch -> convertCatch(ir)
        is IrTypeOperatorCall -> convertTypeOperatorCall(ir)
        is IrGetValue -> convertGetValue(ir)
        is IrSetValue -> convertSetValue(ir)
        is IrVararg -> convertVararg(ir)
        is IrSpreadElement -> convertSpreadElement(ir)
        is IrWhen -> convertWhen(ir)
        is IrElseBranch -> convertElseBranch(ir)
        is IrBranch -> convertBranch(ir)
        else -> error(ir.javaClass)
    }

    override fun elementRefMayAppearTwice(ir: IrElement): Boolean = ir is IrSymbolOwner || ir is
            IrAttributeContainer || ir is IrConstructorCall || ir is IrSimpleFunction || ir is
            IrConstructor || ir is IrModuleFragment || ir is IrFunctionAccessExpression || ir is
            IrLoop

    context(BirTreeContext)
    private fun convertValueParameter(ir: IrValueParameter): BirValueParameter {
        val bir = BirValueParameterImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            varargElementType = null,
            isCrossinline = ir.isCrossinline,
            isNoinline = ir.isNoinline,
            isHidden = ir.isHidden,
            defaultValue = null,
            origin = ir.origin,
            annotations = emptyList(),
            type = BirUninitializedType,
            isAssignable = ir.isAssignable,
            name = ir.name,
        )
        registerNewElement(ir, bir)
        bir.defaultValue = mapIrElement(ir.defaultValue) as BirExpressionBody?
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        bir.varargElementType = ir.varargElementType?.let { convertType(it) }
        return bir
    }

    context(BirTreeContext)
    private fun convertClass(ir: IrClass): BirClass {
        val bir = BirClassImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            kind = ir.kind,
            modality = ir.modality,
            isCompanion = ir.isCompanion,
            isInner = ir.isInner,
            isData = ir.isData,
            isValue = ir.isValue,
            isExpect = ir.isExpect,
            isFun = ir.isFun,
            source = ir.source,
            superTypes = emptyList(),
            thisReceiver = null,
            valueClassRepresentation = null,
            origin = ir.origin,
            annotations = emptyList(),
            isExternal = ir.isExternal,
            name = ir.name,
            visibility = ir.visibility,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.thisReceiver = mapIrElement(ir.thisReceiver) as BirValueParameter?
        moveChildElementList(ir.typeParameters, bir.typeParameters)
        moveChildElementList(ir.declarations, bir.declarations)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        bir.superTypes = ir.superTypes.map { convertType(it) }
        bir.valueClassRepresentation = when (val valueClassRepresentation = ir.valueClassRepresentation) {
            null -> null
            is InlineClassRepresentation<IrSimpleType> -> InlineClassRepresentation<BirSimpleType>(
                valueClassRepresentation.underlyingPropertyName,
                convertType(valueClassRepresentation.underlyingType) as BirSimpleType
            )
            is MultiFieldValueClassRepresentation<IrSimpleType> -> MultiFieldValueClassRepresentation<BirSimpleType>(
                valueClassRepresentation.underlyingPropertyNamesToTypes.map { (name, type) -> name to convertType(type) as BirSimpleType }
            )
        }
        return bir
    }

    context(BirTreeContext)
    private fun convertAnonymousInitializer(ir: IrAnonymousInitializer): BirAnonymousInitializer {
        val bir = BirAnonymousInitializerImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            isStatic = ir.isStatic,
            body = mapIrElement(ir.body) as BirBlockBody,
            origin = ir.origin,
            annotations = emptyList(),
        )
        registerNewElement(ir, bir)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    context(BirTreeContext)
    private fun convertTypeParameter(ir: IrTypeParameter): BirTypeParameter {
        val bir = BirTypeParameterImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            variance = ir.variance,
            isReified = ir.isReified,
            superTypes = emptyList(),
            origin = ir.origin,
            annotations = emptyList(),
            name = ir.name,
        )
        registerNewElement(ir, bir)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        bir.superTypes = ir.superTypes.map { convertType(it) }
        return bir
    }

    context(BirTreeContext)
    private fun convertConstructor(ir: IrConstructor): BirConstructor {
        val bir = BirConstructorImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            isPrimary = ir.isPrimary,
            isInline = ir.isInline,
            isExpect = ir.isExpect,
            returnType = BirUninitializedType,
            dispatchReceiverParameter = null,
            extensionReceiverParameter = null,
            contextReceiverParametersCount = ir.contextReceiverParametersCount,
            body = null,
            origin = ir.origin,
            annotations = emptyList(),
            isExternal = ir.isExternal,
            name = ir.name,
            visibility = ir.visibility,
        )
        registerNewElement(ir, bir)
        bir.dispatchReceiverParameter = mapIrElement(ir.dispatchReceiverParameter) as
                BirValueParameter?
        bir.extensionReceiverParameter = mapIrElement(ir.extensionReceiverParameter) as
                BirValueParameter?
        moveChildElementList(ir.valueParameters, bir.valueParameters)
        bir.body = mapIrElement(ir.body) as BirBody?
        moveChildElementList(ir.typeParameters, bir.typeParameters)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        bir.returnType = convertType(ir.returnType)
        return bir
    }

    context(BirTreeContext)
    private fun convertEnumEntry(ir: IrEnumEntry): BirEnumEntry {
        val bir = BirEnumEntryImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            initializerExpression = null,
            correspondingClass = null,
            origin = ir.origin,
            annotations = emptyList(),
            name = ir.name,
        )
        registerNewElement(ir, bir)
        bir.initializerExpression = mapIrElement(ir.initializerExpression) as BirExpressionBody?
        bir.correspondingClass = mapIrElement(ir.correspondingClass) as BirClass?
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    context(BirTreeContext)
    private fun convertErrorDeclaration(ir: IrErrorDeclaration): BirErrorDeclaration {
        val bir = BirErrorDeclarationImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            origin = ir.origin,
            annotations = emptyList(),
        )
        registerNewElement(ir, bir)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }


    context(BirTreeContext)
    private fun convertFunctionWithLateBinding(ir: IrFunctionWithLateBindingImpl):
            BirFunctionWithLateBinding {
        val bir = BirFunctionWithLateBindingImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            modality = ir.modality,
            isElementBound = ir.isBound,
            origin = ir.origin,
            annotations = emptyList(),
            isTailrec = ir.isTailrec,
            isSuspend = ir.isSuspend,
            isFakeOverride = ir.isFakeOverride,
            isOperator = ir.isOperator,
            isInfix = ir.isInfix,
            correspondingProperty = null,
            overriddenSymbols = emptyList(),
            isInline = ir.isInline,
            isExpect = ir.isExpect,
            returnType = BirUninitializedType,
            dispatchReceiverParameter = null,
            extensionReceiverParameter = null,
            contextReceiverParametersCount = ir.contextReceiverParametersCount,
            body = null,
            isExternal = ir.isExternal,
            name = ir.name,
            visibility = ir.visibility,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.dispatchReceiverParameter = mapIrElement(ir.dispatchReceiverParameter) as
                BirValueParameter?
        bir.extensionReceiverParameter = mapIrElement(ir.extensionReceiverParameter) as
                BirValueParameter?
        moveChildElementList(ir.valueParameters, bir.valueParameters)
        bir.body = mapIrElement(ir.body) as BirBody?
        moveChildElementList(ir.typeParameters, bir.typeParameters)
        bir.correspondingProperty = ir.correspondingPropertySymbol?.let { mapSymbol(ir, it) }
        bir.overriddenSymbols = ir.overriddenSymbols.map { mapSymbol(ir, it) }
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        bir.returnType = convertType(ir.returnType)
        return bir
    }

    context(BirTreeContext)
    private fun convertPropertyWithLateBinding(ir: IrPropertyWithLateBindingImpl):
            BirPropertyWithLateBinding {
        val bir = BirPropertyWithLateBindingImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            modality = ir.modality,
            getter = null,
            setter = null,
            isElementBound = ir.isBound,
            origin = ir.origin,
            annotations = emptyList(),
            isVar = ir.isVar,
            isConst = ir.isConst,
            isLateinit = ir.isLateinit,
            isDelegated = ir.isDelegated,
            isExpect = ir.isExpect,
            isFakeOverride = ir.isFakeOverride,
            backingField = null,
            overriddenSymbols = emptyList(),
            isExternal = ir.isExternal,
            name = ir.name,
            visibility = ir.visibility,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.getter = mapIrElement(ir.getter) as BirSimpleFunction?
        bir.setter = mapIrElement(ir.setter) as BirSimpleFunction?
        bir.backingField = mapIrElement(ir.backingField) as BirField?
        bir.overriddenSymbols = ir.overriddenSymbols.map { mapSymbol(ir, it) }
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    context(BirTreeContext)
    private fun convertField(ir: IrField): BirField {
        val bir = BirFieldImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            type = BirUninitializedType,
            isFinal = ir.isFinal,
            isStatic = ir.isStatic,
            initializer = null,
            correspondingProperty = null,
            origin = ir.origin,
            annotations = emptyList(),
            isExternal = ir.isExternal,
            name = ir.name,
            visibility = ir.visibility,
        )
        registerNewElement(ir, bir)
        bir.initializer = mapIrElement(ir.initializer) as BirExpressionBody?
        bir.correspondingProperty = ir.correspondingPropertySymbol?.let { mapSymbol(ir, it) }
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertLocalDelegatedProperty(ir: IrLocalDelegatedProperty):
            BirLocalDelegatedProperty {
        val bir = BirLocalDelegatedPropertyImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            type = BirUninitializedType,
            isVar = ir.isVar,
            delegate = mapIrElement(ir.delegate) as BirVariable,
            getter = mapIrElement(ir.getter) as BirSimpleFunction,
            setter = null,
            origin = ir.origin,
            annotations = emptyList(),
            name = ir.name,
        )
        registerNewElement(ir, bir)
        bir.setter = mapIrElement(ir.setter) as BirSimpleFunction?
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertModuleFragment(ir: IrModuleFragment): BirModuleFragment {
        val bir = BirModuleFragmentImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            name = ir.name,
        )
        registerNewElement(ir, bir)
        moveChildElementList(ir.files, bir.files)
        return bir
    }

    context(BirTreeContext)
    private fun convertProperty(ir: IrProperty): BirProperty {
        val bir = BirPropertyImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            isVar = ir.isVar,
            isConst = ir.isConst,
            isLateinit = ir.isLateinit,
            isDelegated = ir.isDelegated,
            isExpect = ir.isExpect,
            isFakeOverride = ir.isFakeOverride,
            backingField = null,
            getter = null,
            setter = null,
            overriddenSymbols = emptyList(),
            origin = ir.origin,
            annotations = emptyList(),
            isExternal = ir.isExternal,
            name = ir.name,
            modality = ir.modality,
            visibility = ir.visibility,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.backingField = mapIrElement(ir.backingField) as BirField?
        bir.getter = mapIrElement(ir.getter) as BirSimpleFunction?
        bir.setter = mapIrElement(ir.setter) as BirSimpleFunction?
        bir.overriddenSymbols = ir.overriddenSymbols.map { mapSymbol(ir, it) }
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    context(BirTreeContext)
    private fun convertScript(ir: IrScript): BirScript {
        val bir = BirScriptImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            thisReceiver = null,
            baseClass = null,
            providedProperties = ir.providedProperties.map { mapSymbol(ir, it) },
            resultProperty = ir.resultProperty?.let { mapSymbol(ir, it) },
            earlierScriptsParameter = null,
            earlierScripts = ir.earlierScripts?.map { mapSymbol(ir, it) },
            targetClass = ir.targetClass?.let { mapSymbol(ir, it) },
            constructor = null,
            _descriptor = mapDescriptor(ir.descriptor) as ScriptDescriptor,
            origin = ir.origin,
            annotations = emptyList(),
            name = ir.name,
        )
        registerNewElement(ir, bir)
        bir.thisReceiver = mapIrElement(ir.thisReceiver) as BirValueParameter?
        moveChildElementList(ir.explicitCallParameters, bir.explicitCallParameters)
        moveChildElementList(ir.implicitReceiversParameters, bir.implicitReceiversParameters)
        moveChildElementList(ir.providedPropertiesParameters, bir.providedPropertiesParameters)
        bir.earlierScriptsParameter = mapIrElement(ir.earlierScriptsParameter) as BirValueParameter?
        bir.constructor = mapIrElement(ir.constructor) as BirConstructor?
        moveChildElementList(ir.statements, bir.statements)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        bir.baseClass = ir.baseClass?.let { convertType(it) }
        return bir
    }

    context(BirTreeContext)
    private fun convertSimpleFunction(ir: IrSimpleFunction): BirSimpleFunction {
        val bir = BirSimpleFunctionImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            isTailrec = ir.isTailrec,
            isSuspend = ir.isSuspend,
            isFakeOverride = ir.isFakeOverride,
            isOperator = ir.isOperator,
            isInfix = ir.isInfix,
            correspondingProperty = null,
            overriddenSymbols = emptyList(),
            _descriptor = mapDescriptor(ir.descriptor),
            isInline = ir.isInline,
            isExpect = ir.isExpect,
            returnType = BirUninitializedType,
            dispatchReceiverParameter = null,
            extensionReceiverParameter = null,
            contextReceiverParametersCount = ir.contextReceiverParametersCount,
            body = null,
            origin = ir.origin,
            annotations = emptyList(),
            isExternal = ir.isExternal,
            name = ir.name,
            visibility = ir.visibility,
            modality = ir.modality,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.dispatchReceiverParameter = mapIrElement(ir.dispatchReceiverParameter) as BirValueParameter?
        bir.extensionReceiverParameter = mapIrElement(ir.extensionReceiverParameter) as BirValueParameter?
        moveChildElementList(ir.valueParameters, bir.valueParameters)
        bir.body = mapIrElement(ir.body) as BirBody?
        moveChildElementList(ir.typeParameters, bir.typeParameters)
        bir.overriddenSymbols = ir.overriddenSymbols.map { mapSymbol(ir, it) }
        bir.correspondingProperty = ir.correspondingPropertySymbol?.let { mapSymbol(ir, it) }
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        bir.returnType = convertType(ir.returnType)
        return bir
    }

    context(BirTreeContext)
    private fun convertTypeAlias(ir: IrTypeAlias): BirTypeAlias {
        val bir = BirTypeAliasImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            isActual = ir.isActual,
            expandedType = BirUninitializedType,
            origin = ir.origin,
            annotations = emptyList(),
            name = ir.name,
            visibility = ir.visibility,
        )
        registerNewElement(ir, bir)
        moveChildElementList(ir.typeParameters, bir.typeParameters)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        bir.expandedType = convertType(ir.expandedType)
        return bir
    }

    context(BirTreeContext)
    private fun convertVariable(ir: IrVariable): BirVariable {
        val bir = BirVariableImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.descriptor),
            isVar = ir.isVar,
            isConst = ir.isConst,
            isLateinit = ir.isLateinit,
            initializer = null,
            origin = ir.origin,
            annotations = emptyList(),
            type = BirUninitializedType,
            isAssignable = ir.isAssignable,
            name = ir.name,
        )
        registerNewElement(ir, bir)
        bir.initializer = mapIrElement(ir.initializer) as BirExpression?
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertExternalPackageFragment(ir: IrExternalPackageFragment):
            BirExternalPackageFragment {
        val bir = BirExternalPackageFragmentImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            _descriptor = mapDescriptor(ir.packageFragmentDescriptor),
            fqName = ir.fqName,
            containerSource = ir.containerSource,
        )
        registerNewElement(ir, bir)
        moveChildElementList(ir.declarations, bir.declarations)
        return bir
    }

    context(BirTreeContext)
    private fun convertFile(ir: IrFile): BirFile {
        val bir = BirFileImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            module = mapIrElement(ir.module) as BirModuleFragment,
            fileEntry = ir.fileEntry,
            _descriptor = mapDescriptor(ir.packageFragmentDescriptor),
            fqName = ir.fqName,
            annotations = emptyList(),
        )
        registerNewElement(ir, bir)
        moveChildElementList(ir.declarations, bir.declarations)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    context(BirTreeContext)
    private fun convertExpressionBody(ir: IrExpressionBody): BirExpressionBody {
        val bir = BirExpressionBodyImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            expression = mapIrElement(ir.expression) as BirExpression,
        )
        registerNewElement(ir, bir)
        return bir
    }

    context(BirTreeContext)
    private fun convertBlockBody(ir: IrBlockBody): BirBlockBody {
        val bir = BirBlockBodyImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
        )
        registerNewElement(ir, bir)
        moveChildElementList(ir.statements, bir.statements)
        return bir
    }

    context(BirTreeContext)
    private fun convertConstructorCall(ir: IrConstructorCall): BirConstructorCall {
        val bir = BirConstructorCallImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            source = ir.source,
            constructorTypeArgumentsCount = ir.constructorTypeArgumentsCount,
            contextReceiversCount = ir.contextReceiversCount,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = arrayOfNulls(ir.typeArguments.size),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        ir.typeArguments.forEachIndexed { i, it -> bir.typeArguments[i] = it?.let { convertType(it) } }
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertGetObjectValue(ir: IrGetObjectValue): BirGetObjectValue {
        val bir = BirGetObjectValueImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertGetEnumValue(ir: IrGetEnumValue): BirGetEnumValue {
        val bir = BirGetEnumValueImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertRawFunctionReference(ir: IrRawFunctionReference): BirRawFunctionReference {
        val bir = BirRawFunctionReferenceImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertBlock(ir: IrBlock): BirBlock {
        val bir = BirBlockImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            origin = ir.origin,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.statements, bir.statements)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertComposite(ir: IrComposite): BirComposite {
        val bir = BirCompositeImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            origin = ir.origin,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.statements, bir.statements)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertReturnableBlock(ir: IrReturnableBlock): BirReturnableBlock {
        val bir = BirReturnableBlockImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            origin = ir.origin,
            type = BirUninitializedType,
            _descriptor = mapDescriptor(ir.descriptor),
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.statements, bir.statements)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertInlinedFunctionBlock(ir: IrInlinedFunctionBlock): BirInlinedFunctionBlock {
        val bir = BirInlinedFunctionBlockImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            inlineCall = mapIrElement(ir.inlineCall) as BirFunctionAccessExpression,
            inlinedElement = mapIrElement(ir.inlinedElement),
            origin = ir.origin,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.statements, bir.statements)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertSyntheticBody(ir: IrSyntheticBody): BirSyntheticBody {
        val bir = BirSyntheticBodyImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            kind = ir.kind,
        )
        registerNewElement(ir, bir)
        return bir
    }

    context(BirTreeContext)
    private fun convertBreak(ir: IrBreak): BirBreak {
        val bir = BirBreakImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            loop = mapIrElement(ir.loop) as BirLoop,
            label = ir.label,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertContinue(ir: IrContinue): BirContinue {
        val bir = BirContinueImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            loop = mapIrElement(ir.loop) as BirLoop,
            label = ir.label,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertCall(ir: IrCall): BirCall {
        val bir = BirCallImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            superQualifier = null,
            contextReceiversCount = ir.contextReceiversCount,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = arrayOfNulls(ir.typeArguments.size),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.superQualifier = ir.superQualifierSymbol?.let { mapSymbol(ir, it) }
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        ir.typeArguments.forEachIndexed { i, it -> bir.typeArguments[i] = it?.let { convertType(it) } }
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertFunctionReference(ir: IrFunctionReference): BirFunctionReference {
        val bir = BirFunctionReferenceImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            reflectionTarget = null,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = arrayOfNulls(ir.typeArguments.size),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.reflectionTarget = ir.reflectionTarget?.let { mapSymbol(ir, it) }
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        ir.typeArguments.forEachIndexed { i, it -> bir.typeArguments[i] = it?.let { convertType(it) } }
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertPropertyReference(ir: IrPropertyReference): BirPropertyReference {
        val bir = BirPropertyReferenceImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            field = null,
            getter = null,
            setter = null,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = arrayOfNulls(ir.typeArguments.size),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.field = ir.field?.let { mapSymbol(ir, it) }
        bir.getter = ir.getter?.let { mapSymbol(ir, it) }
        bir.setter = ir.setter?.let { mapSymbol(ir, it) }
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        ir.typeArguments.forEachIndexed { i, it -> bir.typeArguments[i] = it?.let { convertType(it) } }
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertLocalDelegatedPropertyReference(ir: IrLocalDelegatedPropertyReference):
            BirLocalDelegatedPropertyReference {
        val bir = BirLocalDelegatedPropertyReferenceImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            delegate = mapIrElement(ir.delegate.owner) as BirVariable,
            getter = mapSymbol(ir, ir.getter),
            setter = null,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = arrayOfNulls(ir.typeArguments.size),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.setter = ir.setter?.let { mapSymbol(ir, it) }
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        ir.typeArguments.forEachIndexed { i, it -> bir.typeArguments[i] = it?.let { convertType(it) } }
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertClassReference(ir: IrClassReference): BirClassReference {
        val bir = BirClassReferenceImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            classType = BirUninitializedType,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        bir.classType = convertType(ir.classType)
        return bir
    }

    context(BirTreeContext)
    private fun convertConst(ir: IrConst<*>): BirConst<*> {
        val bir = BirConstImpl<Any?>(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            kind = ir.kind as IrConstKind<Any?>,
            value = ir.value,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertConstantPrimitive(ir: IrConstantPrimitive): BirConstantPrimitive {
        val bir = BirConstantPrimitiveImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            value = mapIrElement(ir.value) as BirConst<*>,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertConstantObject(ir: IrConstantObject): BirConstantObject {
        val bir = BirConstantObjectImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            constructor = mapSymbol(ir, ir.constructor),
            typeArguments = ir.typeArguments.map { convertType(it) },
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.valueArguments, bir.valueArguments)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertConstantArray(ir: IrConstantArray): BirConstantArray {
        val bir = BirConstantArrayImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.elements, bir.elements)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertDelegatingConstructorCall(ir: IrDelegatingConstructorCall):
            BirDelegatingConstructorCall {
        val bir = BirDelegatingConstructorCallImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            contextReceiversCount = ir.contextReceiversCount,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = arrayOfNulls(ir.typeArguments.size),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        ir.typeArguments.forEachIndexed { i, it -> bir.typeArguments[i] = it?.let { convertType(it) } }
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertDynamicOperatorExpression(ir: IrDynamicOperatorExpression):
            BirDynamicOperatorExpression {
        val bir = BirDynamicOperatorExpressionImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            operator = ir.operator,
            receiver = mapIrElement(ir.receiver) as BirExpression,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.arguments, bir.arguments)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertDynamicMemberExpression(ir: IrDynamicMemberExpression):
            BirDynamicMemberExpression {
        val bir = BirDynamicMemberExpressionImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            memberName = ir.memberName,
            receiver = mapIrElement(ir.receiver) as BirExpression,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertEnumConstructorCall(ir: IrEnumConstructorCall): BirEnumConstructorCall {
        val bir = BirEnumConstructorCallImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            contextReceiversCount = ir.contextReceiversCount,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = arrayOfNulls(ir.typeArguments.size),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        ir.typeArguments.forEachIndexed { i, it -> bir.typeArguments[i] = it?.let { convertType(it) } }
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertErrorCallExpression(ir: IrErrorCallExpression): BirErrorCallExpression {
        val bir = BirErrorCallExpressionImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            explicitReceiver = null,
            description = ir.description,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.explicitReceiver = mapIrElement(ir.explicitReceiver) as BirExpression?
        moveChildElementList(ir.arguments, bir.arguments)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertGetField(ir: IrGetField): BirGetField {
        val bir = BirGetFieldImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapSymbol(ir, ir.symbol),
            superQualifier = null,
            receiver = null,
            origin = ir.origin,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.superQualifier = ir.superQualifierSymbol?.let { mapSymbol(ir, it) }
        bir.receiver = mapIrElement(ir.receiver) as BirExpression?
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertSetField(ir: IrSetField): BirSetField {
        val bir = BirSetFieldImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            value = mapIrElement(ir.value) as BirExpression,
            target = mapSymbol(ir, ir.symbol),
            superQualifier = null,
            receiver = null,
            origin = ir.origin,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.superQualifier = ir.superQualifierSymbol?.let { mapSymbol(ir, it) }
        bir.receiver = mapIrElement(ir.receiver) as BirExpression?
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertFunctionExpression(ir: IrFunctionExpression): BirFunctionExpression {
        val bir = BirFunctionExpressionImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            origin = ir.origin,
            function = mapIrElement(ir.function) as BirSimpleFunction,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertGetClass(ir: IrGetClass): BirGetClass {
        val bir = BirGetClassImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            argument = mapIrElement(ir.argument) as BirExpression,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertInstanceInitializerCall(ir: IrInstanceInitializerCall):
            BirInstanceInitializerCall {
        val bir = BirInstanceInitializerCallImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            `class` = mapSymbol(ir, ir.classSymbol),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertWhileLoop(ir: IrWhileLoop): BirWhileLoop {
        val bir = BirWhileLoopImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            origin = ir.origin,
            body = null,
            condition = mapIrElement(ir.condition) as BirExpression,
            label = ir.label,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.body = mapIrElement(ir.body) as BirExpression?
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertDoWhileLoop(ir: IrDoWhileLoop): BirDoWhileLoop {
        val bir = BirDoWhileLoopImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            origin = ir.origin,
            body = null,
            condition = mapIrElement(ir.condition) as BirExpression,
            label = ir.label,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.body = mapIrElement(ir.body) as BirExpression?
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertReturn(ir: IrReturn): BirReturn {
        val bir = BirReturnImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            value = mapIrElement(ir.value) as BirExpression,
            returnTarget = mapSymbol(ir, ir.returnTargetSymbol),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertStringConcatenation(ir: IrStringConcatenation): BirStringConcatenation {
        val bir = BirStringConcatenationImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.arguments, bir.arguments)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertSuspensionPoint(ir: IrSuspensionPoint): BirSuspensionPoint {
        val bir = BirSuspensionPointImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            suspensionPointIdParameter = mapIrElement(ir.suspensionPointIdParameter) as BirVariable,
            result = mapIrElement(ir.result) as BirExpression,
            resumeResult = mapIrElement(ir.resumeResult) as BirExpression,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertSuspendableExpression(ir: IrSuspendableExpression):
            BirSuspendableExpression {
        val bir = BirSuspendableExpressionImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            suspensionPointId = mapIrElement(ir.suspensionPointId) as BirExpression,
            result = mapIrElement(ir.result) as BirExpression,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertThrow(ir: IrThrow): BirThrow {
        val bir = BirThrowImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            value = mapIrElement(ir.value) as BirExpression,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertTry(ir: IrTry): BirTry {
        val bir = BirTryImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            tryResult = mapIrElement(ir.tryResult) as BirExpression,
            finallyExpression = null,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.catches, bir.catches)
        bir.finallyExpression = mapIrElement(ir.finallyExpression) as BirExpression?
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertCatch(ir: IrCatch): BirCatch {
        val bir = BirCatchImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            catchParameter = mapIrElement(ir.catchParameter) as BirVariable,
            result = mapIrElement(ir.result) as BirExpression,
        )
        registerNewElement(ir, bir)
        return bir
    }

    context(BirTreeContext)
    private fun convertTypeOperatorCall(ir: IrTypeOperatorCall): BirTypeOperatorCall {
        val bir = BirTypeOperatorCallImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            operator = ir.operator,
            argument = mapIrElement(ir.argument) as BirExpression,
            typeOperand = BirUninitializedType,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        bir.typeOperand = convertType(ir.typeOperand)
        return bir
    }

    context(BirTreeContext)
    private fun convertGetValue(ir: IrGetValue): BirGetValue {
        val bir = BirGetValueImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapIrElement(ir.symbol.owner) as BirValueDeclaration,
            origin = ir.origin,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertSetValue(ir: IrSetValue): BirSetValue {
        val bir = BirSetValueImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            target = mapIrElement(ir.symbol.owner) as BirValueDeclaration,
            value = mapIrElement(ir.value) as BirExpression,
            origin = ir.origin,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertVararg(ir: IrVararg): BirVararg {
        val bir = BirVarargImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            varargElementType = BirUninitializedType,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.elements, bir.elements)
        bir.type = convertType(ir.type)
        bir.varargElementType = convertType(ir.varargElementType)
        return bir
    }

    context(BirTreeContext)
    private fun convertSpreadElement(ir: IrSpreadElement): BirSpreadElement {
        val bir = BirSpreadElementImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            expression = mapIrElement(ir.expression) as BirExpression,
        )
        registerNewElement(ir, bir)
        return bir
    }

    context(BirTreeContext)
    private fun convertWhen(ir: IrWhen): BirWhen {
        val bir = BirWhenImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            origin = ir.origin,
            type = BirUninitializedType,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.branches, bir.branches)
        bir.type = convertType(ir.type)
        return bir
    }

    context(BirTreeContext)
    private fun convertBranch(ir: IrBranch): BirBranch {
        val bir = BirBranchImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            condition = mapIrElement(ir.condition) as BirExpression,
            result = mapIrElement(ir.result) as BirExpression,
        )
        registerNewElement(ir, bir)
        return bir
    }

    context(BirTreeContext)
    private fun convertElseBranch(ir: IrElseBranch): BirElseBranch {
        val bir = BirElseBranchImpl(
            sourceSpan = SourceSpan(ir.startOffset, ir.endOffset),
            condition = mapIrElement(ir.condition) as BirExpression,
            result = mapIrElement(ir.result) as BirExpression,
        )
        registerNewElement(ir, bir)
        return bir
    }
}
