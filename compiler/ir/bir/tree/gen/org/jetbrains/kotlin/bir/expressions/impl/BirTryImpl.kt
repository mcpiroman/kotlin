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
import org.jetbrains.kotlin.bir.expressions.BirCatch
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirTry
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.types.IrType

class BirTryImpl(
    tryResult: BirExpression,
    finallyExpression: BirExpression?,
    override var type: IrType,
    override val startOffset: Int,
    override val endOffset: Int,
    override var originalBeforeInline: BirAttributeContainer?,
) : BirTry() {
    override var tryResult: BirExpression = tryResult
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override val catches: BirChildElementList<BirCatch> = BirChildElementList(this)

    override var finallyExpression: BirExpression? = finallyExpression
        set(value) {
            setChildField(field, value, this.catches)
            field = value
        }

    override var attributeOwnerId: BirAttributeContainer = this
    init {
        initChildField(tryResult, null)
        initChildField(finallyExpression, catches)
    }

    override fun getFirstChild(): BirElement? = tryResult

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.tryResult
        children[1] = this.catches
        children[2] = this.finallyExpression
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.tryResult.accept(visitor)
        this.catches.acceptChildren(visitor)
        this.finallyExpression?.accept(visitor)
    }
}
