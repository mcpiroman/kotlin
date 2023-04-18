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
import org.jetbrains.kotlin.bir.expressions.BirBranch
import org.jetbrains.kotlin.bir.expressions.BirWhen
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.IrType

class BirWhenImpl(
    override var origin: IrStatementOrigin?,
    override var type: IrType,
    override val startOffset: Int,
    override val endOffset: Int,
    override var originalBeforeInline: BirAttributeContainer?,
) : BirWhen() {
    override val branches: BirChildElementList<BirBranch> = BirChildElementList(this)

    override var attributeOwnerId: BirAttributeContainer = this

    override fun getFirstChild(): BirElement? = branches.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.branches
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.branches.acceptChildren(visitor)
    }
}
