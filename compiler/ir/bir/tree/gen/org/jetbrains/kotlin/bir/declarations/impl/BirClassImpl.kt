/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.declarations.BirDeclaration
import org.jetbrains.kotlin.bir.declarations.BirTypeParameter
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.symbols.BirClassSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.ValueClassRepresentation
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

class BirClassImpl @ObsoleteDescriptorBasedAPI constructor(
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: ClassDescriptor,
    override var kind: ClassKind,
    override var modality: Modality,
    override var isCompanion: Boolean,
    override var isInner: Boolean,
    override var isData: Boolean,
    override var isValue: Boolean,
    override var isExpect: Boolean,
    override var isFun: Boolean,
    override val source: SourceElement,
    override var superTypes: List<IrType>,
    thisReceiver: BirValueParameter?,
    override var valueClassRepresentation: ValueClassRepresentation<IrSimpleType>?,
    override var sealedSubclasses: List<BirClassSymbol>,
    override var origin: IrDeclarationOrigin,
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    override var isExternal: Boolean,
    override var name: Name,
    override var visibility: DescriptorVisibility,
    override var originalBeforeInline: BirAttributeContainer?,
) : BirClass() {
    override var thisReceiver: BirValueParameter? = thisReceiver
        set(value) {
            setChildField(field, value, null)
            field = value
        }

    override var typeParameters: BirChildElementList<BirTypeParameter> =
            BirChildElementList(this)

    override val declarations: BirChildElementList<BirDeclaration> =
            BirChildElementList(this)

    override var attributeOwnerId: BirAttributeContainer = this
    init {
        initChildField(thisReceiver, null)
    }

    override fun getFirstChild(): BirElement? = thisReceiver ?: typeParameters.firstOrNull()
            ?: declarations.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.thisReceiver
        children[1] = this.typeParameters
        children[2] = this.declarations
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.thisReceiver?.accept(visitor)
        this.typeParameters.acceptChildren(visitor)
        this.declarations.acceptChildren(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        this.sealedSubclasses = this.sealedSubclasses.map { if(it === old) new as BirClassSymbol
                else it }
    }
}
