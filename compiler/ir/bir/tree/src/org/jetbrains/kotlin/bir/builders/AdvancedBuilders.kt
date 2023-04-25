/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.builders

import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.declarations.impl.BirSimpleFunctionImpl
import org.jetbrains.kotlin.bir.expressions.BirBody
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSimpleFunctionSymbol
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

interface BirSimpleFunctionBuilder {
    interface NameBuilder {
        fun withName(name: Name): Final
    }

    interface ParametersBuilder {
        fun withoutValueParameters()
        fun withValueParameters(
            dispatchReceiverParameter: BirValueParameter? = null,
            extensionReceiverParameter: BirValueParameter? = null,
            valueParameters: List<BirValueParameter> = emptyList(),
            contextReceiverParametersCount: Int = 0,
        )
    }

    interface Final {
        fun build(): BirSimpleFunction
    }
}

private class BirSimpleFunctionBuilderImpl() :
    BirSimpleFunctionBuilder.NameBuilder, BirSimpleFunctionBuilder.Final,
    BirSimpleFunctionBuilder.ParametersBuilder {
    var sourceSpan: SourceSpan = SourceSpan.UNDEFINED
    lateinit var name: Name
    lateinit var origin: IrDeclarationOrigin
    var modality: Modality = Modality.FINAL
    var visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC
    var annotations: List<BirConstructorCall> = emptyList()
    var dispatchReceiverParameter: BirValueParameter? = null
    var extensionReceiverParameter: BirValueParameter? = null
    lateinit var valueParameters: List<BirValueParameter>
    var contextReceiverParametersCount: Int = 0
    var body: BirBody? = null
    lateinit var returnType: BirType
    var isExternal: Boolean = false
    var isExpect: Boolean = false
    var isInline: Boolean = false
    var isTailrec: Boolean = false
    var isSuspend: Boolean = false
    var isOperator: Boolean = false
    var isInfix: Boolean = false
    var isFakeOverride: Boolean = false
    lateinit var overriddenSymbols: List<BirSimpleFunctionSymbol>
    var correspondingProperty: BirPropertySymbol? = null
    var descriptor: FunctionDescriptor? = null

    override fun withName(name: Name): BirSimpleFunctionBuilder.Final {
        this.name = name
        return this
    }

    override fun withoutValueParameters() {
        TODO("Not yet implemented")
    }

    override fun withValueParameters(
        dispatchReceiverParameter: BirValueParameter?,
        extensionReceiverParameter: BirValueParameter?,
        valueParameters: List<BirValueParameter>,
        contextReceiverParametersCount: Int
    ) {
        this.dispatchReceiverParameter = dispatchReceiverParameter
        this.extensionReceiverParameter = extensionReceiverParameter
        this.valueParameters = valueParameters
        this.contextReceiverParametersCount = contextReceiverParametersCount
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun build(): BirSimpleFunction {
        return BirSimpleFunctionImpl(
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
    }

}