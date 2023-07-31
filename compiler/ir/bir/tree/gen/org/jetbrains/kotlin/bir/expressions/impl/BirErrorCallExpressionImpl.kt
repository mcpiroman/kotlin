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
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirErrorCallExpression
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType

class BirErrorCallExpressionImpl(
    sourceSpan: SourceSpan,
    type: BirType,
    description: String,
    explicitReceiver: BirExpression?,
) : BirErrorCallExpression() {
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

    private var _description: String = description

    override var description: String
        get() = _description
        set(value) {
            if(_description != value) {
               _description = value
               propertyChanged()
            }
        }

    private var _explicitReceiver: BirExpression? = explicitReceiver

    override var explicitReceiver: BirExpression?
        get() = _explicitReceiver
        set(value) {
            if(_explicitReceiver != value) {
               setChildField(_explicitReceiver, value, null)
               _explicitReceiver = value
               propertyChanged()
            }
        }

    override val arguments: BirChildElementList<BirExpression> = BirChildElementList(this, 1)
    init {
        initChildField(_explicitReceiver, null)
    }

    override fun getFirstChild(): BirElement? = _explicitReceiver ?: arguments.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._explicitReceiver
        children[1] = this.arguments
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._explicitReceiver?.accept(visitor)
        this.arguments.acceptChildren(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._explicitReceiver === old -> this.explicitReceiver = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }

    override fun getChildrenListById(id: Int): BirChildElementList<*> = when {
       id == 1 -> this.arguments
       else -> throwChildrenListWithIdNotFound(id)
    }
}
