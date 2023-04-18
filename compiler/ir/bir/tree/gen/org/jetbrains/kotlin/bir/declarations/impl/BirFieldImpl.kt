/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.declarations.BirField
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpressionBody
import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.MetadataSource
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

class BirFieldImpl @ObsoleteDescriptorBasedAPI constructor(
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: PropertyDescriptor,
    override var type: IrType,
    override var isFinal: Boolean,
    override var isStatic: Boolean,
    initializer: BirExpressionBody?,
    override var correspondingPropertySymbol: BirPropertySymbol?,
    override var origin: IrDeclarationOrigin,
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    override var isExternal: Boolean,
    override var name: Name,
    override var visibility: DescriptorVisibility,
    override var metadata: MetadataSource?,
) : BirField() {
    override var initializer: BirExpressionBody? = initializer
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

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.correspondingPropertySymbol === old) this.correspondingPropertySymbol = new as
                BirPropertySymbol
    }
}
