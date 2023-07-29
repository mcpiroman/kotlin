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
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirCatch
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept

class BirCatchImpl(
    override var sourceSpan: SourceSpan,
    catchParameter: BirVariable,
    result: BirExpression,
) : BirCatch() {
    private var _catchParameter: BirVariable = catchParameter

    override var catchParameter: BirVariable
        get() = _catchParameter
        set(value) {
            setChildField(_catchParameter, value, null)
            _catchParameter = value
        }

    private var _result: BirExpression = result

    override var result: BirExpression
        get() = _result
        set(value) {
            setChildField(_result, value, this._catchParameter)
            _result = value
        }
    init {
        initChildField(_catchParameter, null)
        initChildField(_result, _catchParameter)
    }

    override fun getFirstChild(): BirElement? = _catchParameter

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._catchParameter
        children[1] = this._result
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._catchParameter.accept(visitor)
        this._result.accept(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._catchParameter === old -> this.catchParameter = new as BirVariable
           this._result === old -> this.result = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
