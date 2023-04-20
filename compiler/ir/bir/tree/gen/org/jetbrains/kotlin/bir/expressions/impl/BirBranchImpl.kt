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
import org.jetbrains.kotlin.bir.expressions.BirBranch
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept

context(BirTreeContext)
class BirBranchImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    condition: BirExpression,
    result: BirExpression,
) : BirBranch() {
    override var condition: BirExpression = condition
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override var result: BirExpression = result
        set(value) {
            setChildField(field, value, this.condition)
            field = value
        }
    init {
        initChildField(condition, null)
        initChildField(result, condition)
    }

    override fun getFirstChild(): BirElement? = condition

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.condition
        children[1] = this.result
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.condition.accept(visitor)
        this.result.accept(visitor)
    }
}
