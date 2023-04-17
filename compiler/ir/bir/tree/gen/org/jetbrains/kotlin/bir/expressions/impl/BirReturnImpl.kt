/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirReturn
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.symbols.IrReturnTargetSymbol
import org.jetbrains.kotlin.ir.types.IrType

class BirReturnImpl(
    value: BirExpression,
    override var returnTargetSymbol: IrReturnTargetSymbol,
    override var attributeOwnerId: BirAttributeContainer,
    override var originalBeforeInline: BirAttributeContainer?,
    override var type: IrType,
    override val startOffset: Int,
    override val endOffset: Int,
) : BirReturn() {
    override var value: BirExpression = value
        set(value) {
            setChildField(field, value, null)
            field = value
        }
    init {
        initChildField(value, null)
    }

    override fun getFirstChild(): BirElement? = value

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.value
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.value.accept(visitor)
    }
}
