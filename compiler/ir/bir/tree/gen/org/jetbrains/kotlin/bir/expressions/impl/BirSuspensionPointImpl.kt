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
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirSuspensionPoint
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType

class BirSuspensionPointImpl(
    sourceSpan: SourceSpan,
    type: BirType,
    suspensionPointIdParameter: BirVariable,
    result: BirExpression,
    resumeResult: BirExpression,
) : BirSuspensionPoint() {
    private var _sourceSpan: SourceSpan = sourceSpan

    override var sourceSpan: SourceSpan
        get() = _sourceSpan
        set(value) {
            if(_sourceSpan != value) {
               _sourceSpan = value
               propertyChanged()
            }
        }

    private var _attributeOwnerId: BirAttributeContainer = this

    override var attributeOwnerId: BirAttributeContainer
        get() = _attributeOwnerId
        set(value) {
            if(_attributeOwnerId != value) {
               _attributeOwnerId = value
               propertyChanged()
            }
        }

    private var _type: BirType = type

    override var type: BirType
        get() = _type
        set(value) {
            if(_type != value) {
               _type = value
               propertyChanged()
            }
        }

    private var _suspensionPointIdParameter: BirVariable = suspensionPointIdParameter

    override var suspensionPointIdParameter: BirVariable
        get() = _suspensionPointIdParameter
        set(value) {
            if(_suspensionPointIdParameter != value) {
               setChildField(_suspensionPointIdParameter, value, null)
               _suspensionPointIdParameter = value
               propertyChanged()
            }
        }

    private var _result: BirExpression = result

    override var result: BirExpression
        get() = _result
        set(value) {
            if(_result != value) {
               setChildField(_result, value, this._suspensionPointIdParameter)
               _result = value
               propertyChanged()
            }
        }

    private var _resumeResult: BirExpression = resumeResult

    override var resumeResult: BirExpression
        get() = _resumeResult
        set(value) {
            if(_resumeResult != value) {
               setChildField(_resumeResult, value, this._result)
               _resumeResult = value
               propertyChanged()
            }
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
           this._suspensionPointIdParameter === old -> this.suspensionPointIdParameter = new as
                BirVariable
           this._result === old -> this.result = new as BirExpression
           this._resumeResult === old -> this.resumeResult = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
