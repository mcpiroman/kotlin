/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpressionBody
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

class BirValueParameterImpl @ObsoleteDescriptorBasedAPI constructor(
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: ParameterDescriptor,
    override var index: Int,
    override var varargElementType: IrType?,
    override var isCrossinline: Boolean,
    override var isNoinline: Boolean,
    override var isHidden: Boolean,
    defaultValue: BirExpressionBody?,
    override var origin: IrDeclarationOrigin,
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    override var type: IrType,
    override val isAssignable: Boolean,
    override var name: Name,
) : BirValueParameter() {
    override var defaultValue: BirExpressionBody? = defaultValue
        set(value) {
            setChildField(field, value, null)
            field = value
        }
    init {
        initChildField(defaultValue, null)
    }

    override fun getFirstChild(): BirElement? = defaultValue

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.defaultValue
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.defaultValue?.accept(visitor)
    }
}
