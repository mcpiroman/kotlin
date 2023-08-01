/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.expressions.BirElseBranch
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept

class BirElseBranchImpl(
    sourceSpan: SourceSpan,
    condition: BirExpression,
    result: BirExpression,
) : BirElseBranch() {
    private var _sourceSpan: SourceSpan = sourceSpan

    override var sourceSpan: SourceSpan
        get() = _sourceSpan
        set(value) {
            if(_sourceSpan != value) {
               _sourceSpan = value
               propertyChanged()
            }
        }

    private var _condition: BirExpression = condition

    override var condition: BirExpression
        get() = _condition
        set(value) {
            if(_condition != value) {
               setChildField(_condition, value, null)
               _condition = value
               propertyChanged()
            }
        }

    private var _result: BirExpression = result

    override var result: BirExpression
        get() = _result
        set(value) {
            if(_result != value) {
               setChildField(_result, value, this._condition)
               _result = value
               propertyChanged()
            }
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

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._condition === old -> this.condition = new as BirExpression
           this._result === old -> this.result = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
