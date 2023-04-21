/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirBackReferenceCollectionArrayStyle
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.declarations.BirLocalDelegatedProperty
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.VariableDescriptorWithAccessors
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

class BirLocalDelegatedPropertyImpl @ObsoleteDescriptorBasedAPI constructor(
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: VariableDescriptorWithAccessors,
    override var origin: IrDeclarationOrigin,
    override var name: Name,
    override var type: IrType,
    override var isVar: Boolean,
    delegate: BirVariable,
    getter: BirSimpleFunction,
    setter: BirSimpleFunction?,
) : BirLocalDelegatedProperty() {
    override var referencedBy: BirBackReferenceCollectionArrayStyle =
            BirBackReferenceCollectionArrayStyle()

    private var _delegate: BirVariable = delegate

    context(BirTreeContext)
    override var delegate: BirVariable
        get() = _delegate
        set(value) {
            setChildField(_delegate, value, null)
            _delegate = value
        }

    private var _getter: BirSimpleFunction = getter

    context(BirTreeContext)
    override var getter: BirSimpleFunction
        get() = _getter
        set(value) {
            setChildField(_getter, value, this._delegate)
            _getter = value
        }

    private var _setter: BirSimpleFunction? = setter

    context(BirTreeContext)
    override var setter: BirSimpleFunction?
        get() = _setter
        set(value) {
            setChildField(_setter, value, this._getter)
            _setter = value
        }
    init {
        initChildField(_delegate, null)
        initChildField(_getter, _delegate)
        initChildField(_setter, _getter)
    }

    override fun getFirstChild(): BirElement? = _delegate

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._delegate
        children[1] = this._getter
        children[2] = this._setter
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._delegate.accept(visitor)
        this._getter.accept(visitor)
        this._setter?.accept(visitor)
    }
}
