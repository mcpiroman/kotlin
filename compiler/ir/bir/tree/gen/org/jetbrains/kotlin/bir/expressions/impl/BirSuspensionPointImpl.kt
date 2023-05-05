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
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirSuspensionPoint
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType

class BirSuspensionPointImpl(
    override var sourceSpan: SourceSpan,
    override var type: BirType,
    suspensionPointIdParameter: BirVariable,
    result: BirExpression,
    resumeResult: BirExpression,
) : BirSuspensionPoint() {
    override var attributeOwnerId: BirAttributeContainer = this

    private var _suspensionPointIdParameter: BirVariable = suspensionPointIdParameter

    context(BirTreeContext)
    override var suspensionPointIdParameter: BirVariable
        get() = _suspensionPointIdParameter
        set(value) {
            setChildField(_suspensionPointIdParameter, value, null)
            _suspensionPointIdParameter = value
        }

    private var _result: BirExpression = result

    context(BirTreeContext)
    override var result: BirExpression
        get() = _result
        set(value) {
            setChildField(_result, value, this._suspensionPointIdParameter)
            _result = value
        }

    private var _resumeResult: BirExpression = resumeResult

    context(BirTreeContext)
    override var resumeResult: BirExpression
        get() = _resumeResult
        set(value) {
            setChildField(_resumeResult, value, this._result)
            _resumeResult = value
        }
    init {
        initChildField(_suspensionPointIdParameter, null)
        initChildField(_result, _suspensionPointIdParameter)
        initChildField(_resumeResult, _result)
    }

    override fun getFirstChild(): BirElement? = _suspensionPointIdParameter

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._suspensionPointIdParameter
        children[1] = this._result
        children[2] = this._resumeResult
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._suspensionPointIdParameter.accept(visitor)
        this._result.accept(visitor)
        this._resumeResult.accept(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._suspensionPointIdParameter === old -> this._suspensionPointIdParameter = new as
                BirVariable
           this._result === old -> this._result = new as BirExpression
           this._resumeResult === old -> this._resumeResult = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
