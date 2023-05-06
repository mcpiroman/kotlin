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
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirTypeOperatorCall
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator

class BirTypeOperatorCallImpl(
    override var sourceSpan: SourceSpan,
    override var type: BirType,
    override var operator: IrTypeOperator,
    argument: BirExpression,
    override var typeOperand: BirType,
) : BirTypeOperatorCall() {
    override var attributeOwnerId: BirAttributeContainer = this

    private var _argument: BirExpression = argument

    context(BirTreeContext)
    override var argument: BirExpression
        get() = _argument
        set(value) {
            setChildField(_argument, value, null)
            _argument = value
        }
    init {
        initChildField(_argument, null)
    }

    override fun getFirstChild(): BirElement? = _argument

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._argument
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._argument.accept(visitor)
    }

    context(BirTreeContext)
    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._argument === old -> this.argument = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
