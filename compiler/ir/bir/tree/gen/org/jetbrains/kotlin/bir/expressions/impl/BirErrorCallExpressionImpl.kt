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
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirErrorCallExpression
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.types.IrType

class BirErrorCallExpressionImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var originalBeforeInline: BirAttributeContainer?,
    override var type: IrType,
    override var description: String,
    explicitReceiver: BirExpression?,
) : BirErrorCallExpression() {
    override var attributeOwnerId: BirAttributeContainer = this

    override var explicitReceiver: BirExpression? = explicitReceiver
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override val arguments: BirChildElementList<BirExpression> = BirChildElementList(this)
    init {
        initChildField(explicitReceiver, null)
    }

    override fun getFirstChild(): BirElement? = explicitReceiver ?: arguments.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.explicitReceiver
        children[1] = this.arguments
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.explicitReceiver?.accept(visitor)
        this.arguments.acceptChildren(visitor)
    }
}
