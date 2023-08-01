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
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirExpressionBody
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept

class BirExpressionBodyImpl(
    sourceSpan: SourceSpan,
    expression: BirExpression,
) : BirExpressionBody() {
    private var _sourceSpan: SourceSpan = sourceSpan

    override var sourceSpan: SourceSpan
        get() = _sourceSpan
        set(value) {
            if(_sourceSpan != value) {
               _sourceSpan = value
               propertyChanged()
            }
        }

    private var _expression: BirExpression = expression

    override var expression: BirExpression
        get() = _expression
        set(value) {
            if(_expression != value) {
               setChildField(_expression, value, null)
               _expression = value
               propertyChanged()
            }
        }
    init {
        initChildField(_expression, null)
    }

    override fun getFirstChild(): BirElement? = _expression

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._expression
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._expression.accept(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._expression === old -> this.expression = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
