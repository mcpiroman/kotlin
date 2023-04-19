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
import org.jetbrains.kotlin.bir.expressions.BirGetField
import org.jetbrains.kotlin.bir.symbols.BirClassSymbol
import org.jetbrains.kotlin.bir.symbols.BirFieldSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.IrType

class BirGetFieldImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var originalBeforeInline: BirAttributeContainer?,
    override var type: IrType,
    target: BirFieldSymbol,
    override var superQualifier: BirClassSymbol?,
    receiver: BirExpression?,
    override var origin: IrStatementOrigin?,
) : BirGetField() {
    override var attributeOwnerId: BirAttributeContainer = this

    override var target: BirFieldSymbol = target
        set(value) {
            setTrackedElementReferenceArrayStyle(field, value)
            field = value
        }

    override var receiver: BirExpression? = receiver
        set(value) {
            setChildField(field, value, null)
            field = value
        }
    init {
        initChildField(receiver, null)
        initTrackedElementReferenceArrayStyle(target)
    }

    override fun getFirstChild(): BirElement? = receiver

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.receiver
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.receiver?.accept(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.target === old) this.target = new as BirFieldSymbol
        if(this.superQualifier === old) this.superQualifier = new as BirClassSymbol
    }
}
