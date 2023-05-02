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
import org.jetbrains.kotlin.bir.expressions.BirDynamicMemberExpression
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType

class BirDynamicMemberExpressionImpl(
    override var sourceSpan: SourceSpan,
    override var type: BirType,
    override var memberName: String,
    receiver: BirExpression,
) : BirDynamicMemberExpression() {
    override var attributeOwnerId: BirAttributeContainer = this

    private var _receiver: BirExpression = receiver

    context(BirTreeContext)
    override var receiver: BirExpression
        get() = _receiver
        set(value) {
            setChildField(_receiver, value, null)
            _receiver = value
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
}
