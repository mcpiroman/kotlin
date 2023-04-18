/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
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
    override var delegate: BirVariable = delegate
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override var getter: BirSimpleFunction = getter
        set(value) {
            setChildField(field, value, this.delegate)
            field = value
        }

    override var setter: BirSimpleFunction? = setter
        set(value) {
            setChildField(field, value, this.getter)
            field = value
        }
    init {
        initChildField(delegate, null)
        initChildField(getter, delegate)
        initChildField(setter, getter)
    }

    override fun getFirstChild(): BirElement? = delegate

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.delegate
        children[1] = this.getter
        children[2] = this.setter
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.delegate.accept(visitor)
        this.getter.accept(visitor)
        this.setter?.accept(visitor)
    }
}
