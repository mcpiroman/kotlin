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
import org.jetbrains.kotlin.bir.expressions.BirConst
import org.jetbrains.kotlin.bir.expressions.BirConstantPrimitive
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.types.IrType

class BirConstantPrimitiveImpl(
    value: BirConst<*>,
    override var type: IrType,
    override val startOffset: Int,
    override val endOffset: Int,
    override var originalBeforeInline: BirAttributeContainer?,
) : BirConstantPrimitive() {
    override var value: BirConst<*> = value
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override var attributeOwnerId: BirAttributeContainer = this
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
