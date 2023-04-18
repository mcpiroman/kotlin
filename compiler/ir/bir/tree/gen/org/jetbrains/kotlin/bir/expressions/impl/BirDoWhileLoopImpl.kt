/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirDoWhileLoop
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.IrType

class BirDoWhileLoopImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var originalBeforeInline: BirAttributeContainer?,
    override var type: IrType,
    override var origin: IrStatementOrigin?,
    body: BirExpression?,
    condition: BirExpression,
    override var label: String?,
) : BirDoWhileLoop() {
    override var attributeOwnerId: BirAttributeContainer = this

    override var body: BirExpression? = body
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override var condition: BirExpression = condition
        set(value) {
            setChildField(field, value, this.body)
            field = value
        }
    init {
        initChildField(body, null)
        initChildField(condition, body)
    }

    override fun getFirstChild(): BirElement? = body ?: condition

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.body
        children[1] = this.condition
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.body?.accept(visitor)
        this.condition.accept(visitor)
    }
}
