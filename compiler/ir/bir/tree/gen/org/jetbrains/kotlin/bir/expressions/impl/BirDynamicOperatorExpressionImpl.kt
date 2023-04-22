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
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirDynamicOperatorExpression
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.expressions.IrDynamicOperator
import org.jetbrains.kotlin.ir.types.IrType

class BirDynamicOperatorExpressionImpl(
    override val sourceSpan: SourceSpan,
    override var type: IrType,
    override var operator: IrDynamicOperator,
    receiver: BirExpression,
) : BirDynamicOperatorExpression() {
    override var attributeOwnerId: BirAttributeContainer = this

    private var _receiver: BirExpression = receiver

    context(BirTreeContext)
    override var receiver: BirExpression
        get() = _receiver
        set(value) {
            setChildField(_receiver, value, null)
            _receiver = value
        }

    override val arguments: BirChildElementList<BirExpression> = BirChildElementList(this)
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
}
