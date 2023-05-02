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
import org.jetbrains.kotlin.bir.declarations.BirField
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpressionBody
import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

class BirFieldImpl @ObsoleteDescriptorBasedAPI constructor(
    override var sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: PropertyDescriptor?,
    override var origin: IrDeclarationOrigin,
    override var visibility: DescriptorVisibility,
    override var name: Name,
    override var isExternal: Boolean,
    override var type: BirType,
    override var isFinal: Boolean,
    override var isStatic: Boolean,
    initializer: BirExpressionBody?,
    override var correspondingProperty: BirPropertySymbol?,
) : BirField() {
    override var _referencedBy: BirBackReferenceCollectionArrayStyleImpl =
            BirBackReferenceCollectionArrayStyleImpl()

    private var _initializer: BirExpressionBody? = initializer

    context(BirTreeContext)
    override var initializer: BirExpressionBody?
        get() = _initializer
        set(value) {
            setChildField(_initializer, value, null)
            _initializer = value
        }
    init {
        initChildField(_initializer, null)
    }

    override fun getFirstChild(): BirElement? = _initializer

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._initializer
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._initializer?.accept(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.correspondingProperty === old) this.correspondingProperty = new as BirPropertySymbol
    }
}
