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
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirField
import org.jetbrains.kotlin.bir.declarations.BirPropertyWithLateBinding
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

class BirPropertyWithLateBindingImpl @ObsoleteDescriptorBasedAPI constructor(
    override var sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: PropertyDescriptor?,
    override var origin: IrDeclarationOrigin,
    override var name: Name,
    override var isExternal: Boolean,
    override var visibility: DescriptorVisibility,
    override var modality: Modality,
    override var isFakeOverride: Boolean,
    override var overriddenSymbols: List<BirPropertySymbol>,
    override var isVar: Boolean,
    override var isConst: Boolean,
    override var isLateinit: Boolean,
    override var isDelegated: Boolean,
    override var isExpect: Boolean,
    backingField: BirField?,
    getter: BirSimpleFunction?,
    setter: BirSimpleFunction?,
    override val isElementBound: Boolean,
) : BirPropertyWithLateBinding() {
    override var _referencedBy: BirBackReferenceCollectionArrayStyleImpl =
            BirBackReferenceCollectionArrayStyleImpl()

    override var attributeOwnerId: BirAttributeContainer = this

    private var _backingField: BirField? = backingField

    context(BirTreeContext)
    override var backingField: BirField?
        get() = _backingField
        set(value) {
            setChildField(_backingField, value, null)
            _backingField = value
        }

    private var _getter: BirSimpleFunction? = getter

    context(BirTreeContext)
    override var getter: BirSimpleFunction?
        get() = _getter
        set(value) {
            setChildField(_getter, value, this._backingField)
            _getter = value
        }

    private var _setter: BirSimpleFunction? = setter

    context(BirTreeContext)
    override var setter: BirSimpleFunction?
        get() = _setter
        set(value) {
            setChildField(_setter, value, this._getter ?: this._backingField)
            _setter = value
        }
    init {
        initChildField(_backingField, null)
        initChildField(_getter, _backingField)
        initChildField(_setter, _getter ?: _backingField)
    }

    override fun getFirstChild(): BirElement? = _backingField ?: _getter ?: _setter

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._backingField
        children[1] = this._getter
        children[2] = this._setter
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._backingField?.accept(visitor)
        this._getter?.accept(visitor)
        this._setter?.accept(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        this.overriddenSymbols = this.overriddenSymbols.map { if(it === old) new as
                BirPropertySymbol else it }
    }
}
