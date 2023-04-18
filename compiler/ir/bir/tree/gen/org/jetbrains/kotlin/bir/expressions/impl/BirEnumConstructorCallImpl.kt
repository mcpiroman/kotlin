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
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirEnumConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType

class BirEnumConstructorCallImpl(
    override val symbol: IrConstructorSymbol,
    override var contextReceiversCount: Int,
    dispatchReceiver: BirExpression?,
    extensionReceiver: BirExpression?,
    override var origin: IrStatementOrigin?,
    override val typeArguments: Array<IrType?>,
    override var type: IrType,
    override val startOffset: Int,
    override val endOffset: Int,
    override var originalBeforeInline: BirAttributeContainer?,
) : BirEnumConstructorCall() {
    override var dispatchReceiver: BirExpression? = dispatchReceiver
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override var extensionReceiver: BirExpression? = extensionReceiver
        set(value) {
            setChildField(field, value, this.dispatchReceiver)
            field = value
        }

    override val valueArguments: BirChildElementList<BirExpression> =
            BirChildElementList(this)

    override var attributeOwnerId: BirAttributeContainer = this
    init {
        initChildField(dispatchReceiver, null)
        initChildField(extensionReceiver, dispatchReceiver)
    }

    override fun getFirstChild(): BirElement? = dispatchReceiver ?: extensionReceiver ?:
            valueArguments.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.dispatchReceiver
        children[1] = this.extensionReceiver
        children[2] = this.valueArguments
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.dispatchReceiver?.accept(visitor)
        this.extensionReceiver?.accept(visitor)
        this.valueArguments.acceptChildren(visitor)
    }
}
