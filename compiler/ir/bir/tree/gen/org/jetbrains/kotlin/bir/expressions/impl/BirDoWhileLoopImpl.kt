/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.BirBackReferenceCollectionArrayStyleImpl
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirDoWhileLoop
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

class BirDoWhileLoopImpl(
    override var sourceSpan: SourceSpan,
    override var type: BirType,
    override var origin: IrStatementOrigin?,
    body: BirExpression?,
    condition: BirExpression,
    override var label: String?,
) : BirDoWhileLoop() {
    override var _referencedBy: BirBackReferenceCollectionArrayStyleImpl =
            BirBackReferenceCollectionArrayStyleImpl()

    override var attributeOwnerId: BirAttributeContainer = this

    private var _body: BirExpression? = body

    context(BirTreeContext)
    override var body: BirExpression?
        get() = _body
        set(value) {
            setChildField(_body, value, null)
            _body = value
        }

    private var _condition: BirExpression = condition

    context(BirTreeContext)
    override var condition: BirExpression
        get() = _condition
        set(value) {
            setChildField(_condition, value, this._body)
            _condition = value
        }
    init {
        initChildField(_body, null)
        initChildField(_condition, _body)
    }

    override fun getFirstChild(): BirElement? = _body ?: _condition

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._body
        children[1] = this._condition
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._body?.accept(visitor)
        this._condition.accept(visitor)
    }
}
