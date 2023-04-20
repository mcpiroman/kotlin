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
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirReturn
import org.jetbrains.kotlin.bir.symbols.BirReturnTargetSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.types.IrType

context(BirTreeContext)
class BirReturnImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    value: BirExpression,
    override var returnTarget: BirReturnTargetSymbol,
) : BirReturn() {
    override var attributeOwnerId: BirAttributeContainer = this

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

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.returnTarget === old) this.returnTarget = new as BirReturnTargetSymbol
    }
}
