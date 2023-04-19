/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirBackReferenceCollectionArrayStyle
import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.declarations.BirDeclaration
import org.jetbrains.kotlin.bir.declarations.BirTypeParameter
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
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
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: ClassDescriptor,
    override var origin: IrDeclarationOrigin,
    override var visibility: DescriptorVisibility,
    override var name: Name,
    override var isExternal: Boolean,
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
) : BirClass() {
    override var referencedBy: BirBackReferenceCollectionArrayStyle =
            BirBackReferenceCollectionArrayStyle()

    override var typeParameters: BirChildElementList<BirTypeParameter> =
            BirChildElementList(this)

    override val declarations: BirChildElementList<BirDeclaration> =
            BirChildElementList(this)

    override var attributeOwnerId: BirAttributeContainer = this

    override var thisReceiver: BirValueParameter? = thisReceiver
        set(value) {
            setChildField(field, value, this.declarations)
            field = value
        }
    init {
        initChildField(thisReceiver, declarations)
    }

    override fun getFirstChild(): BirElement? = typeParameters.firstOrNull() ?:
            declarations.firstOrNull() ?: thisReceiver

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.typeParameters
        children[1] = this.declarations
        children[2] = this.thisReceiver
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.typeParameters.acceptChildren(visitor)
        this.declarations.acceptChildren(visitor)
        this.thisReceiver?.accept(visitor)
    }
}
