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
import org.jetbrains.kotlin.bir.expressions.BirDynamicMemberExpression
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType

class BirDynamicMemberExpressionImpl(
    sourceSpan: SourceSpan,
    type: BirType,
    memberName: String,
    receiver: BirExpression,
) : BirDynamicMemberExpression() {
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

    private var _memberName: String = memberName

    override var memberName: String
        get() = _memberName
        set(value) {
            if(_memberName != value) {
               _memberName = value
               propertyChanged()
            }
        }

    private var _receiver: BirExpression = receiver

    override var receiver: BirExpression
        get() = _receiver
        set(value) {
            if(_receiver != value) {
               setChildField(_receiver, value, null)
               _receiver = value
               propertyChanged()
            }
        }
    init {
        initChildField(_receiver, null)
    }

    override fun getFirstChild(): BirElement? = _receiver

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._receiver
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._receiver.accept(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._receiver === old -> this.receiver = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
