/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirDynamicOperatorExpression
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.ir.expressions.IrDynamicOperator

class BirDynamicOperatorExpressionImpl(
    override var sourceSpan: SourceSpan,
    override var type: BirType,
    override var operator: IrDynamicOperator,
    receiver: BirExpression,
) : BirDynamicOperatorExpression() {
    override var attributeOwnerId: BirAttributeContainer = this

    private var _receiver: BirExpression = receiver

    override var receiver: BirExpression
        get() = _receiver
        set(value) {
            setChildField(_receiver, value, null)
            _receiver = value
        }

    override val arguments: BirChildElementList<BirExpression> = BirChildElementList(this, 1)
    init {
        initChildField(_receiver, null)
    }

    override fun getFirstChild(): BirElement? = _receiver

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._receiver
        children[1] = this.arguments
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._receiver.accept(visitor)
        this.arguments.acceptChildren(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._receiver === old -> this.receiver = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }

    override fun getChildrenListById(id: Int): BirChildElementList<*> = when {
       id == 1 -> this.arguments
       else -> throwChildrenListWithIdNotFound(id)
    }
}
