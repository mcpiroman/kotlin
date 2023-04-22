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
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirConstantArray
import org.jetbrains.kotlin.bir.expressions.BirConstantValue
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.ir.types.IrType

class BirConstantArrayImpl(
    override val sourceSpan: SourceSpan,
    override var type: IrType,
) : BirConstantArray() {
    override var attributeOwnerId: BirAttributeContainer = this

    override val elements: BirChildElementList<BirConstantValue> = BirChildElementList(this)

    override fun getFirstChild(): BirElement? = elements.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.elements
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.elements.acceptChildren(visitor)
    }
}
