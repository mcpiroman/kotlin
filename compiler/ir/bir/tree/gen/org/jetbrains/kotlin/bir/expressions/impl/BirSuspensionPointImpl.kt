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
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirSuspensionPoint
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.types.IrType

class BirSuspensionPointImpl(
    suspensionPointIdParameter: BirVariable,
    result: BirExpression,
    resumeResult: BirExpression,
    override var type: IrType,
    override val startOffset: Int,
    override val endOffset: Int,
    override var originalBeforeInline: BirAttributeContainer?,
) : BirSuspensionPoint() {
    override var suspensionPointIdParameter: BirVariable = suspensionPointIdParameter
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override var result: BirExpression = result
        set(value) {
            setChildField(field, value, this.suspensionPointIdParameter)
            field = value
        }

    override var resumeResult: BirExpression = resumeResult
        set(value) {
            setChildField(field, value, this.result)
            field = value
        }

    override var attributeOwnerId: BirAttributeContainer = this
    init {
        initChildField(suspensionPointIdParameter, null)
        initChildField(result, suspensionPointIdParameter)
        initChildField(resumeResult, result)
    }

    override fun getFirstChild(): BirElement? = suspensionPointIdParameter

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.suspensionPointIdParameter
        children[1] = this.result
        children[2] = this.resumeResult
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.suspensionPointIdParameter.accept(visitor)
        this.result.accept(visitor)
        this.resumeResult.accept(visitor)
    }
}
