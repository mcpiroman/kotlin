/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.declarations.BirTypeParameter
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.expressions.BirBody
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSimpleFunctionSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.MetadataSource
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

class BirSimpleFunctionImpl @ObsoleteDescriptorBasedAPI constructor(
    override var isTailrec: Boolean,
    override var isSuspend: Boolean,
    override var isFakeOverride: Boolean,
    override var isOperator: Boolean,
    override var isInfix: Boolean,
    override var correspondingPropertySymbol: BirPropertySymbol?,
    override var overriddenSymbols: List<BirSimpleFunctionSymbol>,
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: FunctionDescriptor,
    override var isInline: Boolean,
    override var isExpect: Boolean,
    override var returnType: IrType,
    dispatchReceiverParameter: BirValueParameter?,
    extensionReceiverParameter: BirValueParameter?,
    override var contextReceiverParametersCount: Int,
    body: BirBody?,
    override var origin: IrDeclarationOrigin,
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    override var isExternal: Boolean,
    override var name: Name,
    override var visibility: DescriptorVisibility,
    override val containerSource: DeserializedContainerSource?,
    override var metadata: MetadataSource?,
    override var modality: Modality,
    override var originalBeforeInline: BirAttributeContainer?,
) : BirSimpleFunction() {
    override var dispatchReceiverParameter: BirValueParameter? = dispatchReceiverParameter
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override var extensionReceiverParameter: BirValueParameter? = extensionReceiverParameter
        set(value) {
            setChildField(field, value, this.dispatchReceiverParameter)
            field = value
        }

    override var valueParameters: BirChildElementList<BirValueParameter> =
            BirChildElementList(this)

    override var body: BirBody? = body
        set(value) {
            setChildField(field, value, this.valueParameters)
            field = value
        }

    override var typeParameters: BirChildElementList<BirTypeParameter> =
            BirChildElementList(this)

    override var attributeOwnerId: BirAttributeContainer = this
    init {
        initChildField(dispatchReceiverParameter, null)
        initChildField(extensionReceiverParameter, dispatchReceiverParameter)
        initChildField(body, valueParameters)
    }

    override fun getFirstChild(): BirElement? = dispatchReceiverParameter ?:
            extensionReceiverParameter ?: valueParameters.firstOrNull() ?: body ?:
            typeParameters.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.dispatchReceiverParameter
        children[1] = this.extensionReceiverParameter
        children[2] = this.valueParameters
        children[3] = this.body
        children[4] = this.typeParameters
        return 5
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.dispatchReceiverParameter?.accept(visitor)
        this.extensionReceiverParameter?.accept(visitor)
        this.valueParameters.acceptChildren(visitor)
        this.body?.accept(visitor)
        this.typeParameters.acceptChildren(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.correspondingPropertySymbol === old) this.correspondingPropertySymbol = new as
                BirPropertySymbol
        this.overriddenSymbols = this.overriddenSymbols.map { if(it === old) new as
                BirSimpleFunctionSymbol else it }
    }
}
