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
import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.declarations.BirEnumEntry
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpressionBody
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

context(BirTreeContext)
class BirEnumEntryImpl @ObsoleteDescriptorBasedAPI constructor(
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: ClassDescriptor,
    override var origin: IrDeclarationOrigin,
    override var name: Name,
    initializerExpression: BirExpressionBody?,
    correspondingClass: BirClass?,
) : BirEnumEntry() {
    override var referencedBy: BirBackReferenceCollectionArrayStyle =
            BirBackReferenceCollectionArrayStyle()

    override var initializerExpression: BirExpressionBody? = initializerExpression
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override var correspondingClass: BirClass? = correspondingClass
        set(value) {
            setChildField(field, value, this.initializerExpression)
            field = value
        }
    init {
        initChildField(initializerExpression, null)
        initChildField(correspondingClass, initializerExpression)
    }

    override fun getFirstChild(): BirElement? = initializerExpression ?: correspondingClass

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.initializerExpression
        children[1] = this.correspondingClass
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.initializerExpression?.accept(visitor)
        this.correspondingClass?.accept(visitor)
    }
}
