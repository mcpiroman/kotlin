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
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.expressions.BirFunctionExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.IrType

class BirFunctionExpressionImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    override var origin: IrStatementOrigin,
    function: BirSimpleFunction,
) : BirFunctionExpression() {
    override var attributeOwnerId: BirAttributeContainer = this

    private var _function: BirSimpleFunction = function

    context(BirTreeContext)
    override var function: BirSimpleFunction
        get() = _function
        set(value) {
            setChildField(_function, value, null)
            _function = value
        }
    init {
        initChildField(_function, null)
    }

    override fun getFirstChild(): BirElement? = _function

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._function
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._function.accept(visitor)
    }
}
