/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirBackReferenceCollectionArrayStyleImpl
import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.declarations.BirDeclaration
import org.jetbrains.kotlin.bir.declarations.BirTypeParameter
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirSimpleType
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.ValueClassRepresentation
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

class BirClassImpl @ObsoleteDescriptorBasedAPI constructor(
    override var sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: ClassDescriptor?,
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
    override var superTypes: List<BirType>,
    thisReceiver: BirValueParameter?,
    override var valueClassRepresentation: ValueClassRepresentation<BirSimpleType>?,
) : BirClass() {
    override var _referencedBy: BirBackReferenceCollectionArrayStyleImpl =
            BirBackReferenceCollectionArrayStyleImpl()

    override var typeParameters: BirChildElementList<BirTypeParameter> =
            BirChildElementList(this)

    override val declarations: BirChildElementList<BirDeclaration> =
            BirChildElementList(this)

    override var attributeOwnerId: BirAttributeContainer = this

    private var _thisReceiver: BirValueParameter? = thisReceiver

    context(BirTreeContext)
    override var thisReceiver: BirValueParameter?
        get() = _thisReceiver
        set(value) {
            setChildField(_thisReceiver, value, this.declarations)
            _thisReceiver = value
        }
    init {
        initChildField(_thisReceiver, declarations)
    }

    override fun getFirstChild(): BirElement? = typeParameters.firstOrNull() ?:
            declarations.firstOrNull() ?: _thisReceiver

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.typeParameters
        children[1] = this.declarations
        children[2] = this._thisReceiver
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.typeParameters.acceptChildren(visitor)
        this.declarations.acceptChildren(visitor)
        this._thisReceiver?.accept(visitor)
    }

    context(BirTreeContext)
    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._thisReceiver === old -> this.thisReceiver = new as BirValueParameter
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
