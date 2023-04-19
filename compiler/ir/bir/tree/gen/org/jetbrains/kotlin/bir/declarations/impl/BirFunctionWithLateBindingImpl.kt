/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirBackReferenceCollectionArrayStyle
import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirFunctionWithLateBinding
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
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

class BirFunctionWithLateBindingImpl @ObsoleteDescriptorBasedAPI constructor(
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: FunctionDescriptor,
    override var origin: IrDeclarationOrigin,
    override var visibility: DescriptorVisibility,
    override var name: Name,
    override var isExternal: Boolean,
    override var isInline: Boolean,
    override var isExpect: Boolean,
    override var returnType: IrType,
    dispatchReceiverParameter: BirValueParameter?,
    extensionReceiverParameter: BirValueParameter?,
    override var contextReceiverParametersCount: Int,
    body: BirBody?,
    override var modality: Modality,
    override var isFakeOverride: Boolean,
    override var overriddenSymbols: List<BirSimpleFunctionSymbol>,
    override var isTailrec: Boolean,
    override var isSuspend: Boolean,
    override var isOperator: Boolean,
    override var isInfix: Boolean,
    override var correspondingProperty: BirPropertySymbol?,
    override val isElementBound: Boolean,
) : BirFunctionWithLateBinding() {
    override var referencedBy: BirBackReferenceCollectionArrayStyle =
            BirBackReferenceCollectionArrayStyle()

    override var typeParameters: BirChildElementList<BirTypeParameter> =
            BirChildElementList(this)

    override var dispatchReceiverParameter: BirValueParameter? = dispatchReceiverParameter
        set(value) {
            setChildField(field, value, this.typeParameters)
            field = value
        }

    override var extensionReceiverParameter: BirValueParameter? = extensionReceiverParameter
        set(value) {
            setChildField(field, value, this.dispatchReceiverParameter ?: this.typeParameters)
            field = value
        }

    override var valueParameters: BirChildElementList<BirValueParameter> =
            BirChildElementList(this)

    override var body: BirBody? = body
        set(value) {
            setChildField(field, value, this.valueParameters)
            field = value
        }

    override var attributeOwnerId: BirAttributeContainer = this
    init {
        initChildField(dispatchReceiverParameter, typeParameters)
        initChildField(extensionReceiverParameter, dispatchReceiverParameter ?: typeParameters)
        initChildField(body, valueParameters)
    }

    override fun getFirstChild(): BirElement? = typeParameters.firstOrNull() ?:
            dispatchReceiverParameter ?: extensionReceiverParameter ?: valueParameters.firstOrNull()
            ?: body

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.typeParameters
        children[1] = this.dispatchReceiverParameter
        children[2] = this.extensionReceiverParameter
        children[3] = this.valueParameters
        children[4] = this.body
        return 5
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.typeParameters.acceptChildren(visitor)
        this.dispatchReceiverParameter?.accept(visitor)
        this.extensionReceiverParameter?.accept(visitor)
        this.valueParameters.acceptChildren(visitor)
        this.body?.accept(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        this.overriddenSymbols = this.overriddenSymbols.map { if(it === old) new as
                BirSimpleFunctionSymbol else it }
        if(this.correspondingProperty === old) this.correspondingProperty = new as BirPropertySymbol
    }
}
