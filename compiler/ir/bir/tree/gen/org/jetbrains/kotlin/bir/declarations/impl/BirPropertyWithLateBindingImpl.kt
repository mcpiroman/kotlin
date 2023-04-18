/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
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
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

class BirPropertyWithLateBindingImpl @ObsoleteDescriptorBasedAPI constructor(
    override val isElementBound: Boolean,
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: PropertyDescriptor,
    override var isVar: Boolean,
    override var isConst: Boolean,
    override var isLateinit: Boolean,
    override var isDelegated: Boolean,
    override var isExpect: Boolean,
    override var isFakeOverride: Boolean,
    backingField: BirField?,
    getter: BirSimpleFunction?,
    setter: BirSimpleFunction?,
    override var overriddenSymbols: List<BirPropertySymbol>,
    override var origin: IrDeclarationOrigin,
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    override var isExternal: Boolean,
    override var name: Name,
    override var modality: Modality,
    override var visibility: DescriptorVisibility,
    override var originalBeforeInline: BirAttributeContainer?,
    override val containerSource: DeserializedContainerSource?,
) : BirPropertyWithLateBinding() {
    override var backingField: BirField? = backingField
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override var getter: BirSimpleFunction? = getter
        set(value) {
            setChildField(field, value, this.backingField)
            field = value
        }

    override var setter: BirSimpleFunction? = setter
        set(value) {
            setChildField(field, value, this.getter ?: this.backingField)
            field = value
        }

    override var attributeOwnerId: BirAttributeContainer = this
    init {
        initChildField(backingField, null)
        initChildField(getter, backingField)
        initChildField(setter, getter ?: backingField)
    }

    override fun getFirstChild(): BirElement? = backingField ?: getter ?: setter

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.backingField
        children[1] = this.getter
        children[2] = this.setter
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.backingField?.accept(visitor)
        this.getter?.accept(visitor)
        this.setter?.accept(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        this.overriddenSymbols = this.overriddenSymbols.map { if(it === old) new as
                BirPropertySymbol else it }
    }
}
