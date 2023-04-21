/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirValueDeclaration
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirSetValue
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.IrType

class BirSetValueImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    target: BirValueDeclaration,
    override var origin: IrStatementOrigin?,
    value: BirExpression,
) : BirSetValue() {
    override var attributeOwnerId: BirAttributeContainer = this

    override var target: BirValueDeclaration = target
        set(value) {
            setTrackedElementReferenceArrayStyle(field, value)
            field = value
        }

    private var _value: BirExpression = value

    context(BirTreeContext)
    override var value: BirExpression
        get() = _value
        set(value) {
            setChildField(_value, value, null)
            _value = value
        }
    init {
        initChildField(_value, null)
        initTrackedElementReferenceArrayStyle(target)
    }

    override fun getFirstChild(): BirElement? = _value

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._value
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._value.accept(visitor)
    }
}
