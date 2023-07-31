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
    sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: ClassDescriptor?,
    origin: IrDeclarationOrigin,
    visibility: DescriptorVisibility,
    name: Name,
    isExternal: Boolean,
    kind: ClassKind,
    modality: Modality,
    isCompanion: Boolean,
    isInner: Boolean,
    isData: Boolean,
    isValue: Boolean,
    isExpect: Boolean,
    isFun: Boolean,
    override val source: SourceElement,
    override var superTypes: List<BirType>,
    thisReceiver: BirValueParameter?,
    valueClassRepresentation: ValueClassRepresentation<BirSimpleType>?,
) : BirClass() {
    override var _referencedBy: BirBackReferenceCollectionArrayStyleImpl =
            BirBackReferenceCollectionArrayStyleImpl()

    private var _sourceSpan: SourceSpan = sourceSpan

    override var sourceSpan: SourceSpan
        get() = _sourceSpan
        set(value) {
            if(_sourceSpan != value) {
               _sourceSpan = value
               propertyChanged()
            }
        }

    private var _origin: IrDeclarationOrigin = origin

    override var origin: IrDeclarationOrigin
        get() = _origin
        set(value) {
            if(_origin != value) {
               _origin = value
               propertyChanged()
            }
        }

    private var _visibility: DescriptorVisibility = visibility

    override var visibility: DescriptorVisibility
        get() = _visibility
        set(value) {
            if(_visibility != value) {
               _visibility = value
               propertyChanged()
            }
        }

    private var _name: Name = name

    override var name: Name
        get() = _name
        set(value) {
            if(_name != value) {
               _name = value
               propertyChanged()
            }
        }

    private var _isExternal: Boolean = isExternal

    override var isExternal: Boolean
        get() = _isExternal
        set(value) {
            if(_isExternal != value) {
               _isExternal = value
               propertyChanged()
            }
        }

    override var typeParameters: BirChildElementList<BirTypeParameter> =
            BirChildElementList(this, 1)

    override val declarations: BirChildElementList<BirDeclaration> =
            BirChildElementList(this, 2)

    private var _attributeOwnerId: BirAttributeContainer = this

    override var attributeOwnerId: BirAttributeContainer
        get() = _attributeOwnerId
        set(value) {
            if(_attributeOwnerId != value) {
               _attributeOwnerId = value
               propertyChanged()
            }
        }

    private var _kind: ClassKind = kind

    override var kind: ClassKind
        get() = _kind
        set(value) {
            if(_kind != value) {
               _kind = value
               propertyChanged()
            }
        }

    private var _modality: Modality = modality

    override var modality: Modality
        get() = _modality
        set(value) {
            if(_modality != value) {
               _modality = value
               propertyChanged()
            }
        }

    private var _isCompanion: Boolean = isCompanion

    override var isCompanion: Boolean
        get() = _isCompanion
        set(value) {
            if(_isCompanion != value) {
               _isCompanion = value
               propertyChanged()
            }
        }

    private var _isInner: Boolean = isInner

    override var isInner: Boolean
        get() = _isInner
        set(value) {
            if(_isInner != value) {
               _isInner = value
               propertyChanged()
            }
        }

    private var _isData: Boolean = isData

    override var isData: Boolean
        get() = _isData
        set(value) {
            if(_isData != value) {
               _isData = value
               propertyChanged()
            }
        }

    private var _isValue: Boolean = isValue

    override var isValue: Boolean
        get() = _isValue
        set(value) {
            if(_isValue != value) {
               _isValue = value
               propertyChanged()
            }
        }

    private var _isExpect: Boolean = isExpect

    override var isExpect: Boolean
        get() = _isExpect
        set(value) {
            if(_isExpect != value) {
               _isExpect = value
               propertyChanged()
            }
        }

    private var _isFun: Boolean = isFun

    override var isFun: Boolean
        get() = _isFun
        set(value) {
            if(_isFun != value) {
               _isFun = value
               propertyChanged()
            }
        }

    private var _thisReceiver: BirValueParameter? = thisReceiver

    override var thisReceiver: BirValueParameter?
        get() = _thisReceiver
        set(value) {
            if(_thisReceiver != value) {
               setChildField(_thisReceiver, value, this.declarations)
               _thisReceiver = value
               propertyChanged()
            }
        }

    private var _valueClassRepresentation: ValueClassRepresentation<BirSimpleType>? =
            valueClassRepresentation

    override var valueClassRepresentation: ValueClassRepresentation<BirSimpleType>?
        get() = _valueClassRepresentation
        set(value) {
            if(_valueClassRepresentation != value) {
               _valueClassRepresentation = value
               propertyChanged()
            }
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

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._thisReceiver === old -> this.thisReceiver = new as BirValueParameter
           else -> throwChildForReplacementNotFound(old)
        }
    }

    override fun getChildrenListById(id: Int): BirChildElementList<*> = when {
       id == 1 -> this.typeParameters
       id == 2 -> this.declarations
       else -> throwChildrenListWithIdNotFound(id)
    }
}
