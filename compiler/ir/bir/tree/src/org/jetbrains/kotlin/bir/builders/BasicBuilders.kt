/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.builders

import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.declarations.impl.BirSimpleFunctionImpl
import org.jetbrains.kotlin.bir.expressions.BirBody
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSimpleFunctionSymbol
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirSimpleFunction(
    sourceSpan: SourceSpan = SourceSpan.UNDEFINED,
    name: Name,
    descriptor: FunctionDescriptor?,
    origin: IrDeclarationOrigin = IrDeclarationOrigin.DEFINED,
    annotations: List<BirConstructorCall> = emptyList(),
    visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC,
    modality: Modality = Modality.FINAL,
    dispatchReceiverParameter: BirValueParameter? = null,
    extensionReceiverParameter: BirValueParameter? = null,
    contextReceiverParametersCount: Int = 0,
    body: BirBody? = null,
    returnType: IrType,
    isExternal: Boolean = false,
    isInline: Boolean = false,
    isExpect: Boolean = false,
    isFakeOverride: Boolean = false,
    isTailrec: Boolean = false,
    isSuspend: Boolean = false,
    isOperator: Boolean = false,
    isInfix: Boolean = false,
    overriddenSymbols: List<BirSimpleFunctionSymbol> = emptyList(),
    correspondingProperty: BirPropertySymbol? = null,
) = BirSimpleFunctionImpl(
    sourceSpan = sourceSpan,
    name = name,
    _descriptor = descriptor,
    origin = origin,
    annotations = annotations,
    visibility = visibility,
    modality = modality,
    dispatchReceiverParameter = dispatchReceiverParameter,
    extensionReceiverParameter = extensionReceiverParameter,
    contextReceiverParametersCount = contextReceiverParametersCount,
    body = body,
    returnType = returnType,
    isExternal = isExternal,
    isInline = isInline,
    isExpect = isExpect,
    isFakeOverride = isFakeOverride,
    isTailrec = isTailrec,
    isSuspend = isSuspend,
    isOperator = isOperator,
    isInfix = isInfix,
    overriddenSymbols = overriddenSymbols,
    correspondingProperty = correspondingProperty,
)