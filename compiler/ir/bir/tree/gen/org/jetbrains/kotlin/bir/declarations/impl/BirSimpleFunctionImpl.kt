/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirBackReferenceCollectionArrayStyleImpl
import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.SourceSpan
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
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

class BirSimpleFunctionImpl @ObsoleteDescriptorBasedAPI constructor(
    override var sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: FunctionDescriptor?,
    override var origin: IrDeclarationOrigin,
    override var visibility: DescriptorVisibility,
    override var name: Name,
    override var isExternal: Boolean,
    override var isInline: Boolean,
    override var isExpect: Boolean,
    override var returnType: BirType,
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
) : BirSimpleFunction() {
    override var _referencedBy: BirBackReferenceCollectionArrayStyleImpl =
            BirBackReferenceCollectionArrayStyleImpl()

    override var typeParameters: BirChildElementList<BirTypeParameter> =
            BirChildElementList(this)

    private var _dispatchReceiverParameter: BirValueParameter? = dispatchReceiverParameter

    context(BirTreeContext)
    override var dispatchReceiverParameter: BirValueParameter?
        get() = _dispatchReceiverParameter
        set(value) {
            setChildField(_dispatchReceiverParameter, value, this.typeParameters)
            _dispatchReceiverParameter = value
        }

    private var _extensionReceiverParameter: BirValueParameter? = extensionReceiverParameter

    context(BirTreeContext)
    override var extensionReceiverParameter: BirValueParameter?
        get() = _extensionReceiverParameter
        set(value) {
            setChildField(_extensionReceiverParameter, value, this._dispatchReceiverParameter ?:
                    this.typeParameters)
            _extensionReceiverParameter = value
        }

    override var valueParameters: BirChildElementList<BirValueParameter> =
            BirChildElementList(this)

    private var _body: BirBody? = body

    context(BirTreeContext)
    override var body: BirBody?
        get() = _body
        set(value) {
            setChildField(_body, value, this.valueParameters)
            _body = value
        }

    override var attributeOwnerId: BirAttributeContainer = this
    init {
        initChildField(_dispatchReceiverParameter, typeParameters)
        initChildField(_extensionReceiverParameter, _dispatchReceiverParameter ?: typeParameters)
        initChildField(_body, valueParameters)
    }

    override fun getFirstChild(): BirElement? = typeParameters.firstOrNull() ?:
            _dispatchReceiverParameter ?: _extensionReceiverParameter ?:
            valueParameters.firstOrNull() ?: _body

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.typeParameters
        children[1] = this._dispatchReceiverParameter
        children[2] = this._extensionReceiverParameter
        children[3] = this.valueParameters
        children[4] = this._body
        return 5
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.typeParameters.acceptChildren(visitor)
        this._dispatchReceiverParameter?.accept(visitor)
        this._extensionReceiverParameter?.accept(visitor)
        this.valueParameters.acceptChildren(visitor)
        this._body?.accept(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        this.overriddenSymbols = this.overriddenSymbols.map { if(it === old) new as
                BirSimpleFunctionSymbol else it }
        if(this.correspondingProperty === old) this.correspondingProperty = new as BirPropertySymbol
    }
}
