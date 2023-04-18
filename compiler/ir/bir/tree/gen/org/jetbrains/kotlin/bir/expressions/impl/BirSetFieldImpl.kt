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
import org.jetbrains.kotlin.bir.expressions.BirSetField
import org.jetbrains.kotlin.bir.symbols.BirClassSymbol
import org.jetbrains.kotlin.bir.symbols.BirFieldSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.IrType

class BirSetFieldImpl(
    value: BirExpression,
    override var symbol: BirFieldSymbol,
    override var superQualifierSymbol: BirClassSymbol?,
    receiver: BirExpression?,
    override var origin: IrStatementOrigin?,
    override var type: IrType,
    override val startOffset: Int,
    override val endOffset: Int,
    override var originalBeforeInline: BirAttributeContainer?,
) : BirSetField() {
    override var value: BirExpression = value
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override var receiver: BirExpression? = receiver
        set(value) {
            setChildField(field, value, this.value)
            field = value
        }

    override var attributeOwnerId: BirAttributeContainer = this
    init {
        initChildField(value, null)
        initChildField(receiver, value)
    }

    override fun getFirstChild(): BirElement? = value

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.value
        children[1] = this.receiver
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.value.accept(visitor)
        this.receiver?.accept(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.symbol === old) this.symbol = new as BirFieldSymbol
        if(this.superQualifierSymbol === old) this.superQualifierSymbol = new as BirClassSymbol
    }
}
