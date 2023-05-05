/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirBackReferenceCollectionArrayStyleImpl
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.SourceSpan
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

class BirEnumEntryImpl @ObsoleteDescriptorBasedAPI constructor(
    override var sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: ClassDescriptor?,
    override var origin: IrDeclarationOrigin,
    override var name: Name,
    initializerExpression: BirExpressionBody?,
    correspondingClass: BirClass?,
) : BirEnumEntry() {
    override var _referencedBy: BirBackReferenceCollectionArrayStyleImpl =
            BirBackReferenceCollectionArrayStyleImpl()

    private var _initializerExpression: BirExpressionBody? = initializerExpression

    context(BirTreeContext)
    override var initializerExpression: BirExpressionBody?
        get() = _initializerExpression
        set(value) {
            setChildField(_initializerExpression, value, null)
            _initializerExpression = value
        }

    private var _correspondingClass: BirClass? = correspondingClass

    context(BirTreeContext)
    override var correspondingClass: BirClass?
        get() = _correspondingClass
        set(value) {
            setChildField(_correspondingClass, value, this._initializerExpression)
            _correspondingClass = value
        }
    init {
        initChildField(_initializerExpression, null)
        initChildField(_correspondingClass, _initializerExpression)
    }

    override fun getFirstChild(): BirElement? = _initializerExpression ?: _correspondingClass

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._initializerExpression
        children[1] = this._correspondingClass
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._initializerExpression?.accept(visitor)
        this._correspondingClass?.accept(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._initializerExpression === old -> this._initializerExpression = new as
                BirExpressionBody
           this._correspondingClass === old -> this._correspondingClass = new as BirClass
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
