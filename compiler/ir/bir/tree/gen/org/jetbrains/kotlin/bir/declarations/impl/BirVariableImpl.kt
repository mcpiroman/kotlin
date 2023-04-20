/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirBackReferenceCollectionArrayStyle
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

context(BirTreeContext)
class BirVariableImpl @ObsoleteDescriptorBasedAPI constructor(
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: VariableDescriptor,
    override var origin: IrDeclarationOrigin,
    override var name: Name,
    override var type: IrType,
    override val isAssignable: Boolean,
    override var isVar: Boolean,
    override var isConst: Boolean,
    override var isLateinit: Boolean,
    initializer: BirExpression?,
) : BirVariable() {
    override var referencedBy: BirBackReferenceCollectionArrayStyle =
            BirBackReferenceCollectionArrayStyle()

    override var initializer: BirExpression? = initializer
        set(value) {
            setChildField(field, value, null)
            field = value
        }
    init {
        initChildField(initializer, null)
    }

    override fun getFirstChild(): BirElement? = initializer

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.initializer
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.initializer?.accept(visitor)
    }
}
