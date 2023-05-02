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
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.expressions.BirBranch
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept

class BirBranchImpl(
    override var sourceSpan: SourceSpan,
    condition: BirExpression,
    result: BirExpression,
) : BirBranch() {
    private var _condition: BirExpression = condition

    context(BirTreeContext)
    override var condition: BirExpression
        get() = _condition
        set(value) {
            setChildField(_condition, value, null)
            _condition = value
        }

    private var _result: BirExpression = result

    context(BirTreeContext)
    override var result: BirExpression
        get() = _result
        set(value) {
            setChildField(_result, value, this._condition)
            _result = value
        }
    init {
        initChildField(_condition, null)
        initChildField(_result, _condition)
    }

    override fun getFirstChild(): BirElement? = _condition

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._condition
        children[1] = this._result
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._condition.accept(visitor)
        this._result.accept(visitor)
    }
}
