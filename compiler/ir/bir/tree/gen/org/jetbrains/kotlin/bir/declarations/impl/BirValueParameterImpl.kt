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
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpressionBody
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

class BirValueParameterImpl @ObsoleteDescriptorBasedAPI constructor(
    override var sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: ParameterDescriptor?,
    override var origin: IrDeclarationOrigin,
    override var name: Name,
    override var type: BirType,
    override val isAssignable: Boolean,
    override var varargElementType: BirType?,
    override var isCrossinline: Boolean,
    override var isNoinline: Boolean,
    override var isHidden: Boolean,
    defaultValue: BirExpressionBody?,
) : BirValueParameter() {
    override var _referencedBy: BirBackReferenceCollectionArrayStyleImpl =
            BirBackReferenceCollectionArrayStyleImpl()

    private var _defaultValue: BirExpressionBody? = defaultValue

    context(BirTreeContext)
    override var defaultValue: BirExpressionBody?
        get() = _defaultValue
        set(value) {
            setChildField(_defaultValue, value, null)
            _defaultValue = value
        }
    init {
        initChildField(_defaultValue, null)
    }

    override fun getFirstChild(): BirElement? = _defaultValue

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._defaultValue
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._defaultValue?.accept(visitor)
    }
}
