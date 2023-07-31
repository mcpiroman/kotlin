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
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirSuspendableExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType

class BirSuspendableExpressionImpl(
    sourceSpan: SourceSpan,
    type: BirType,
    suspensionPointId: BirExpression,
    result: BirExpression,
) : BirSuspendableExpression() {
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

    private var _suspensionPointId: BirExpression = suspensionPointId

    override var suspensionPointId: BirExpression
        get() = _suspensionPointId
        set(value) {
            if(_suspensionPointId != value) {
               setChildField(_suspensionPointId, value, null)
               _suspensionPointId = value
               propertyChanged()
            }
        }

    private var _result: BirExpression = result

    override var result: BirExpression
        get() = _result
        set(value) {
            if(_result != value) {
               setChildField(_result, value, this._suspensionPointId)
               _result = value
               propertyChanged()
            }
        }
    init {
        initChildField(_suspensionPointId, null)
        initChildField(_result, _suspensionPointId)
    }

    override fun getFirstChild(): BirElement? = _suspensionPointId

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._suspensionPointId
        children[1] = this._result
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._suspensionPointId.accept(visitor)
        this._result.accept(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._suspensionPointId === old -> this.suspensionPointId = new as BirExpression
           this._result === old -> this.result = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
