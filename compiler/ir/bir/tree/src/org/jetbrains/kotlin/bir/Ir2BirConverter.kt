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
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionWithLateBindingImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrPropertyWithLateBindingImpl
import org.jetbrains.kotlin.ir.expressions.*

@ObsoleteDescriptorBasedAPI
class Ir2BirConverter : Ir2BirConverterBase() {
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

    private fun convertValueParameter(ir: IrValueParameter): BirValueParameter {
        val bir = BirValueParameterImpl(
            descriptor = ir.descriptor,
            index = ir.index,
            varargElementType = ir.varargElementType,
            isCrossinline = ir.isCrossinline,
            isNoinline = ir.isNoinline,
            isHidden = ir.isHidden,
            defaultValue = null,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            annotations = emptyList(),
            type = ir.type,
            isAssignable = ir.isAssignable,
            name = ir.name,
        )
        registerNewElement(ir, bir)
        bir.defaultValue = mapIrElement(ir.defaultValue) as BirExpressionBody?
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    private fun convertClass(ir: IrClass): BirClass {
        val bir = BirClassImpl(
            descriptor = ir.descriptor,
            kind = ir.kind,
            modality = ir.modality,
            isCompanion = ir.isCompanion,
            isInner = ir.isInner,
            isData = ir.isData,
            isValue = ir.isValue,
            isExpect = ir.isExpect,
            isFun = ir.isFun,
            source = ir.source,
            superTypes = ir.superTypes,
            thisReceiver = null,
            valueClassRepresentation = ir.valueClassRepresentation,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
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
        return bir
    }

    private fun convertAnonymousInitializer(ir: IrAnonymousInitializer): BirAnonymousInitializer {
        val bir = BirAnonymousInitializerImpl(
            descriptor = ir.descriptor,
            isStatic = ir.isStatic,
            body = mapIrElement(ir.body) as BirBlockBody,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            annotations = emptyList(),
        )
        registerNewElement(ir, bir)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    private fun convertTypeParameter(ir: IrTypeParameter): BirTypeParameter {
        val bir = BirTypeParameterImpl(
            descriptor = ir.descriptor,
            variance = ir.variance,
            index = ir.index,
            isReified = ir.isReified,
            superTypes = ir.superTypes,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            annotations = emptyList(),
            name = ir.name,
        )
        registerNewElement(ir, bir)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    private fun convertConstructor(ir: IrConstructor): BirConstructor {
        val bir = BirConstructorImpl(
            descriptor = ir.descriptor,
            isPrimary = ir.isPrimary,
            isInline = ir.isInline,
            isExpect = ir.isExpect,
            returnType = ir.returnType,
            dispatchReceiverParameter = null,
            extensionReceiverParameter = null,
            contextReceiverParametersCount = ir.contextReceiverParametersCount,
            body = null,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
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
        return bir
    }

    private fun convertEnumEntry(ir: IrEnumEntry): BirEnumEntry {
        val bir = BirEnumEntryImpl(
            descriptor = ir.descriptor,
            initializerExpression = null,
            correspondingClass = null,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            annotations = emptyList(),
            name = ir.name,
        )
        registerNewElement(ir, bir)
        bir.initializerExpression = mapIrElement(ir.initializerExpression) as BirExpressionBody?
        bir.correspondingClass = mapIrElement(ir.correspondingClass) as BirClass?
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    private fun convertErrorDeclaration(ir: IrErrorDeclaration): BirErrorDeclaration {
        val bir = BirErrorDeclarationImpl(
            descriptor = ir.descriptor,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            annotations = emptyList(),
        )
        registerNewElement(ir, bir)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }


    private fun convertFunctionWithLateBinding(ir: IrFunctionWithLateBindingImpl):
            BirFunctionWithLateBinding {
        val bir = BirFunctionWithLateBindingImpl(
            descriptor = ir.descriptor,
            modality = ir.modality,
            isElementBound = ir.isBound,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
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
            returnType = ir.returnType,
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
        return bir
    }

    private fun convertPropertyWithLateBinding(ir: IrPropertyWithLateBindingImpl):
            BirPropertyWithLateBinding {
        val bir = BirPropertyWithLateBindingImpl(
            descriptor = ir.descriptor,
            modality = ir.modality,
            getter = null,
            setter = null,
            isElementBound = ir.isBound,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
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

    private fun convertField(ir: IrField): BirField {
        val bir = BirFieldImpl(
            descriptor = ir.descriptor,
            type = ir.type,
            isFinal = ir.isFinal,
            isStatic = ir.isStatic,
            initializer = null,
            correspondingProperty = null,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            annotations = emptyList(),
            isExternal = ir.isExternal,
            name = ir.name,
            visibility = ir.visibility,
        )
        registerNewElement(ir, bir)
        bir.initializer = mapIrElement(ir.initializer) as BirExpressionBody?
        bir.correspondingProperty = ir.correspondingPropertySymbol?.let { mapSymbol(ir, it) }
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    private fun convertLocalDelegatedProperty(ir: IrLocalDelegatedProperty):
            BirLocalDelegatedProperty {
        val bir = BirLocalDelegatedPropertyImpl(
            descriptor = ir.descriptor,
            type = ir.type,
            isVar = ir.isVar,
            delegate = mapIrElement(ir.delegate) as BirVariable,
            getter = mapIrElement(ir.getter) as BirSimpleFunction,
            setter = null,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            annotations = emptyList(),
            name = ir.name,
        )
        registerNewElement(ir, bir)
        bir.setter = mapIrElement(ir.setter) as BirSimpleFunction?
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    private fun convertModuleFragment(ir: IrModuleFragment): BirModuleFragment {
        val bir = BirModuleFragmentImpl(
            descriptor = ir.descriptor,
            name = ir.name,
            irBuiltins = ir.irBuiltins,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        moveChildElementList(ir.files, bir.files)
        return bir
    }

    private fun convertProperty(ir: IrProperty): BirProperty {
        val bir = BirPropertyImpl(
            descriptor = ir.descriptor,
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
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
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

    private fun convertScript(ir: IrScript): BirScript {
        val bir = BirScriptImpl(
            thisReceiver = null,
            baseClass = ir.baseClass,
            providedProperties = ir.providedProperties.map { mapSymbol(ir, it) },
            resultProperty = ir.resultProperty?.let { mapSymbol(ir, it) },
            earlierScriptsParameter = null,
            earlierScripts = ir.earlierScripts?.map { mapSymbol(ir, it) },
            targetClass = ir.targetClass?.let { mapSymbol(ir, it) },
            constructor = null,
            descriptor = ir.descriptor as ScriptDescriptor,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
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
        return bir
    }

    private fun convertSimpleFunction(ir: IrSimpleFunction): BirSimpleFunction {
        val bir = BirSimpleFunctionImpl(
            isTailrec = ir.isTailrec,
            isSuspend = ir.isSuspend,
            isFakeOverride = ir.isFakeOverride,
            isOperator = ir.isOperator,
            isInfix = ir.isInfix,
            correspondingProperty = null,
            overriddenSymbols = emptyList(),
            descriptor = ir.descriptor,
            isInline = ir.isInline,
            isExpect = ir.isExpect,
            returnType = ir.returnType,
            dispatchReceiverParameter = null,
            extensionReceiverParameter = null,
            contextReceiverParametersCount = ir.contextReceiverParametersCount,
            body = null,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
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
        return bir
    }

    private fun convertTypeAlias(ir: IrTypeAlias): BirTypeAlias {
        val bir = BirTypeAliasImpl(
            descriptor = ir.descriptor,
            isActual = ir.isActual,
            expandedType = ir.expandedType,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            annotations = emptyList(),
            name = ir.name,
            visibility = ir.visibility,
        )
        registerNewElement(ir, bir)
        moveChildElementList(ir.typeParameters, bir.typeParameters)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    private fun convertVariable(ir: IrVariable): BirVariable {
        val bir = BirVariableImpl(
            descriptor = ir.descriptor,
            isVar = ir.isVar,
            isConst = ir.isConst,
            isLateinit = ir.isLateinit,
            initializer = null,
            origin = ir.origin,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            annotations = emptyList(),
            type = ir.type,
            isAssignable = ir.isAssignable,
            name = ir.name,
        )
        registerNewElement(ir, bir)
        bir.initializer = mapIrElement(ir.initializer) as BirExpression?
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    private fun convertExternalPackageFragment(ir: IrExternalPackageFragment):
            BirExternalPackageFragment {
        val bir = BirExternalPackageFragmentImpl(
            descriptor = ir.packageFragmentDescriptor,
            fqName = ir.fqName,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            containerSource = ir.containerSource,
        )
        registerNewElement(ir, bir)
        moveChildElementList(ir.declarations, bir.declarations)
        return bir
    }

    private fun convertFile(ir: IrFile): BirFile {
        val bir = BirFileImpl(
            module = mapIrElement(ir.module) as BirModuleFragment,
            fileEntry = ir.fileEntry,
            descriptor = ir.packageFragmentDescriptor,
            fqName = ir.fqName,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            annotations = emptyList(),
        )
        registerNewElement(ir, bir)
        moveChildElementList(ir.declarations, bir.declarations)
        bir.annotations = mapIrElementList<BirConstructorCall>(ir.annotations)
        return bir
    }

    private fun convertExpressionBody(ir: IrExpressionBody): BirExpressionBody {
        val bir = BirExpressionBodyImpl(
            expression = mapIrElement(ir.expression) as BirExpression,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        return bir
    }

    private fun convertBlockBody(ir: IrBlockBody): BirBlockBody {
        val bir = BirBlockBodyImpl(
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        moveChildElementList(ir.statements, bir.statements)
        return bir
    }

    private fun convertConstructorCall(ir: IrConstructorCall): BirConstructorCall {
        val bir = BirConstructorCallImpl(
            target = mapSymbol(ir, ir.symbol),
            source = ir.source,
            constructorTypeArgumentsCount = ir.constructorTypeArgumentsCount,
            contextReceiversCount = ir.contextReceiversCount,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = ir.typeArguments,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        return bir
    }

    private fun convertGetObjectValue(ir: IrGetObjectValue): BirGetObjectValue {
        val bir = BirGetObjectValueImpl(
            target = mapSymbol(ir, ir.symbol),
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertGetEnumValue(ir: IrGetEnumValue): BirGetEnumValue {
        val bir = BirGetEnumValueImpl(
            target = mapSymbol(ir, ir.symbol),
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertRawFunctionReference(ir: IrRawFunctionReference): BirRawFunctionReference {
        val bir = BirRawFunctionReferenceImpl(
            target = mapSymbol(ir, ir.symbol),
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertBlock(ir: IrBlock): BirBlock {
        val bir = BirBlockImpl(
            origin = ir.origin,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.statements, bir.statements)
        return bir
    }

    private fun convertComposite(ir: IrComposite): BirComposite {
        val bir = BirCompositeImpl(
            origin = ir.origin,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.statements, bir.statements)
        return bir
    }

    private fun convertReturnableBlock(ir: IrReturnableBlock): BirReturnableBlock {
        val bir = BirReturnableBlockImpl(
            origin = ir.origin,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
            descriptor = ir.descriptor,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.statements, bir.statements)
        return bir
    }

    private fun convertInlinedFunctionBlock(ir: IrInlinedFunctionBlock): BirInlinedFunctionBlock {
        val bir = BirInlinedFunctionBlockImpl(
            inlineCall = mapIrElement(ir.inlineCall) as BirFunctionAccessExpression,
            inlinedElement = mapIrElement(ir.inlinedElement),
            origin = ir.origin,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.statements, bir.statements)
        return bir
    }

    private fun convertSyntheticBody(ir: IrSyntheticBody): BirSyntheticBody {
        val bir = BirSyntheticBodyImpl(
            kind = ir.kind,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        return bir
    }

    private fun convertBreak(ir: IrBreak): BirBreak {
        val bir = BirBreakImpl(
            loop = mapIrElement(ir.loop) as BirLoop,
            label = ir.label,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertContinue(ir: IrContinue): BirContinue {
        val bir = BirContinueImpl(
            loop = mapIrElement(ir.loop) as BirLoop,
            label = ir.label,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertCall(ir: IrCall): BirCall {
        val bir = BirCallImpl(
            target = mapSymbol(ir, ir.symbol),
            superQualifier = null,
            contextReceiversCount = ir.contextReceiversCount,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = ir.typeArguments,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.superQualifier = ir.superQualifierSymbol?.let { mapSymbol(ir, it) }
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        return bir
    }

    private fun convertFunctionReference(ir: IrFunctionReference): BirFunctionReference {
        val bir = BirFunctionReferenceImpl(
            target = mapSymbol(ir, ir.symbol),
            reflectionTarget = null,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = ir.typeArguments,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.reflectionTarget = ir.reflectionTarget?.let { mapSymbol(ir, it) }
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        return bir
    }

    private fun convertPropertyReference(ir: IrPropertyReference): BirPropertyReference {
        val bir = BirPropertyReferenceImpl(
            target = mapSymbol(ir, ir.symbol),
            field = null,
            getter = null,
            setter = null,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = ir.typeArguments,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.field = ir.field?.let { mapSymbol(ir, it) }
        bir.getter = ir.getter?.let { mapSymbol(ir, it) }
        bir.setter = ir.setter?.let { mapSymbol(ir, it) }
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        return bir
    }

    private fun convertLocalDelegatedPropertyReference(ir: IrLocalDelegatedPropertyReference):
            BirLocalDelegatedPropertyReference {
        val bir = BirLocalDelegatedPropertyReferenceImpl(
            target = mapSymbol(ir, ir.symbol),
            delegate = mapIrElement(ir.delegate.owner) as BirVariable,
            getter = mapSymbol(ir, ir.getter),
            setter = null,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = ir.typeArguments,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.setter = ir.setter?.let { mapSymbol(ir, it) }
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        return bir
    }

    private fun convertClassReference(ir: IrClassReference): BirClassReference {
        val bir = BirClassReferenceImpl(
            target = mapSymbol(ir, ir.symbol),
            classType = ir.classType,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertConst(ir: IrConst<*>): BirConst<*> {
        val bir = BirConstImpl<Any?>(
            kind = ir.kind as IrConstKind<Any?>,
            value = ir.value,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertConstantPrimitive(ir: IrConstantPrimitive): BirConstantPrimitive {
        val bir = BirConstantPrimitiveImpl(
            value = mapIrElement(ir.value) as BirConst<*>,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertConstantObject(ir: IrConstantObject): BirConstantObject {
        val bir = BirConstantObjectImpl(
            constructor = mapSymbol(ir, ir.constructor),
            typeArguments = ir.typeArguments,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.valueArguments, bir.valueArguments)
        return bir
    }

    private fun convertConstantArray(ir: IrConstantArray): BirConstantArray {
        val bir = BirConstantArrayImpl(
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.elements, bir.elements)
        return bir
    }

    private fun convertDelegatingConstructorCall(ir: IrDelegatingConstructorCall):
            BirDelegatingConstructorCall {
        val bir = BirDelegatingConstructorCallImpl(
            target = mapSymbol(ir, ir.symbol),
            contextReceiversCount = ir.contextReceiversCount,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = ir.typeArguments,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        return bir
    }

    private fun convertDynamicOperatorExpression(ir: IrDynamicOperatorExpression):
            BirDynamicOperatorExpression {
        val bir = BirDynamicOperatorExpressionImpl(
            operator = ir.operator,
            receiver = mapIrElement(ir.receiver) as BirExpression,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.arguments, bir.arguments)
        return bir
    }

    private fun convertDynamicMemberExpression(ir: IrDynamicMemberExpression):
            BirDynamicMemberExpression {
        val bir = BirDynamicMemberExpressionImpl(
            memberName = ir.memberName,
            receiver = mapIrElement(ir.receiver) as BirExpression,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertEnumConstructorCall(ir: IrEnumConstructorCall): BirEnumConstructorCall {
        val bir = BirEnumConstructorCallImpl(
            target = mapSymbol(ir, ir.symbol),
            contextReceiversCount = ir.contextReceiversCount,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = ir.origin,
            typeArguments = ir.typeArguments,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.dispatchReceiver = mapIrElement(ir.dispatchReceiver) as BirExpression?
        bir.extensionReceiver = mapIrElement(ir.extensionReceiver) as BirExpression?
        moveIrMemberAccessExpressionValueArguments(ir, bir)
        return bir
    }

    private fun convertErrorCallExpression(ir: IrErrorCallExpression): BirErrorCallExpression {
        val bir = BirErrorCallExpressionImpl(
            explicitReceiver = null,
            description = ir.description,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.explicitReceiver = mapIrElement(ir.explicitReceiver) as BirExpression?
        moveChildElementList(ir.arguments, bir.arguments)
        return bir
    }

    private fun convertGetField(ir: IrGetField): BirGetField {
        val bir = BirGetFieldImpl(
            target = mapSymbol(ir, ir.symbol),
            superQualifier = null,
            receiver = null,
            origin = ir.origin,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.superQualifier = ir.superQualifierSymbol?.let { mapSymbol(ir, it) }
        bir.receiver = mapIrElement(ir.receiver) as BirExpression?
        return bir
    }

    private fun convertSetField(ir: IrSetField): BirSetField {
        val bir = BirSetFieldImpl(
            value = mapIrElement(ir.value) as BirExpression,
            target = mapSymbol(ir, ir.symbol),
            superQualifier = null,
            receiver = null,
            origin = ir.origin,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.superQualifier = ir.superQualifierSymbol?.let { mapSymbol(ir, it) }
        bir.receiver = mapIrElement(ir.receiver) as BirExpression?
        return bir
    }

    private fun convertFunctionExpression(ir: IrFunctionExpression): BirFunctionExpression {
        val bir = BirFunctionExpressionImpl(
            origin = ir.origin,
            function = mapIrElement(ir.function) as BirSimpleFunction,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertGetClass(ir: IrGetClass): BirGetClass {
        val bir = BirGetClassImpl(
            argument = mapIrElement(ir.argument) as BirExpression,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertInstanceInitializerCall(ir: IrInstanceInitializerCall):
            BirInstanceInitializerCall {
        val bir = BirInstanceInitializerCallImpl(
            `class` = mapSymbol(ir, ir.classSymbol),
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertWhileLoop(ir: IrWhileLoop): BirWhileLoop {
        val bir = BirWhileLoopImpl(
            origin = ir.origin,
            body = null,
            condition = mapIrElement(ir.condition) as BirExpression,
            label = ir.label,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.body = mapIrElement(ir.body) as BirExpression?
        return bir
    }

    private fun convertDoWhileLoop(ir: IrDoWhileLoop): BirDoWhileLoop {
        val bir = BirDoWhileLoopImpl(
            origin = ir.origin,
            body = null,
            condition = mapIrElement(ir.condition) as BirExpression,
            label = ir.label,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        bir.body = mapIrElement(ir.body) as BirExpression?
        return bir
    }

    private fun convertReturn(ir: IrReturn): BirReturn {
        val bir = BirReturnImpl(
            value = mapIrElement(ir.value) as BirExpression,
            returnTarget = mapSymbol(ir, ir.returnTargetSymbol),
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertStringConcatenation(ir: IrStringConcatenation): BirStringConcatenation {
        val bir = BirStringConcatenationImpl(
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.arguments, bir.arguments)
        return bir
    }

    private fun convertSuspensionPoint(ir: IrSuspensionPoint): BirSuspensionPoint {
        val bir = BirSuspensionPointImpl(
            suspensionPointIdParameter = mapIrElement(ir.suspensionPointIdParameter) as BirVariable,
            result = mapIrElement(ir.result) as BirExpression,
            resumeResult = mapIrElement(ir.resumeResult) as BirExpression,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertSuspendableExpression(ir: IrSuspendableExpression):
            BirSuspendableExpression {
        val bir = BirSuspendableExpressionImpl(
            suspensionPointId = mapIrElement(ir.suspensionPointId) as BirExpression,
            result = mapIrElement(ir.result) as BirExpression,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertThrow(ir: IrThrow): BirThrow {
        val bir = BirThrowImpl(
            value = mapIrElement(ir.value) as BirExpression,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertTry(ir: IrTry): BirTry {
        val bir = BirTryImpl(
            tryResult = mapIrElement(ir.tryResult) as BirExpression,
            finallyExpression = null,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.catches, bir.catches)
        bir.finallyExpression = mapIrElement(ir.finallyExpression) as BirExpression?
        return bir
    }

    private fun convertCatch(ir: IrCatch): BirCatch {
        val bir = BirCatchImpl(
            catchParameter = mapIrElement(ir.catchParameter) as BirVariable,
            result = mapIrElement(ir.result) as BirExpression,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        return bir
    }

    private fun convertTypeOperatorCall(ir: IrTypeOperatorCall): BirTypeOperatorCall {
        val bir = BirTypeOperatorCallImpl(
            operator = ir.operator,
            argument = mapIrElement(ir.argument) as BirExpression,
            typeOperand = ir.typeOperand,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertGetValue(ir: IrGetValue): BirGetValue {
        val bir = BirGetValueImpl(
            target = mapIrElement(ir.symbol.owner) as BirValueDeclaration,
            origin = ir.origin,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertSetValue(ir: IrSetValue): BirSetValue {
        val bir = BirSetValueImpl(
            target = mapIrElement(ir.symbol.owner) as BirValueDeclaration,
            value = mapIrElement(ir.value) as BirExpression,
            origin = ir.origin,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        return bir
    }

    private fun convertVararg(ir: IrVararg): BirVararg {
        val bir = BirVarargImpl(
            varargElementType = ir.varargElementType,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.elements, bir.elements)
        return bir
    }

    private fun convertSpreadElement(ir: IrSpreadElement): BirSpreadElement {
        val bir = BirSpreadElementImpl(
            expression = mapIrElement(ir.expression) as BirExpression,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        return bir
    }

    private fun convertWhen(ir: IrWhen): BirWhen {
        val bir = BirWhenImpl(
            origin = ir.origin,
            type = ir.type,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        bir.attributeOwnerId = mapIrElement(ir.attributeOwnerId) as BirAttributeContainer
        moveChildElementList(ir.branches, bir.branches)
        return bir
    }

    private fun convertBranch(ir: IrBranch): BirBranch {
        val bir = BirBranchImpl(
            condition = mapIrElement(ir.condition) as BirExpression,
            result = mapIrElement(ir.result) as BirExpression,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        return bir
    }

    private fun convertElseBranch(ir: IrElseBranch): BirElseBranch {
        val bir = BirElseBranchImpl(
            condition = mapIrElement(ir.condition) as BirExpression,
            result = mapIrElement(ir.result) as BirExpression,
            startOffset = ir.startOffset,
            endOffset = ir.endOffset,
        )
        registerNewElement(ir, bir)
        return bir
    }
}
