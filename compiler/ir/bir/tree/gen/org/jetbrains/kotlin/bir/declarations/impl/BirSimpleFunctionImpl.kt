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
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.declarations.BirTypeParameter
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.expressions.BirBody
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSimpleFunctionSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

class BirSimpleFunctionImpl @ObsoleteDescriptorBasedAPI constructor(
    sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: FunctionDescriptor?,
    origin: IrDeclarationOrigin,
    visibility: DescriptorVisibility,
    name: Name,
    isExternal: Boolean,
    isInline: Boolean,
    isExpect: Boolean,
    returnType: BirType,
    dispatchReceiverParameter: BirValueParameter?,
    extensionReceiverParameter: BirValueParameter?,
    contextReceiverParametersCount: Int,
    body: BirBody?,
    modality: Modality,
    isFakeOverride: Boolean,
    override var overriddenSymbols: List<BirSimpleFunctionSymbol>,
    isTailrec: Boolean,
    isSuspend: Boolean,
    isOperator: Boolean,
    isInfix: Boolean,
    correspondingProperty: BirPropertySymbol?,
) : BirSimpleFunction() {
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

    private var _isInline: Boolean = isInline

    override var isInline: Boolean
        get() = _isInline
        set(value) {
            if(_isInline != value) {
               _isInline = value
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

    private var _returnType: BirType = returnType

    override var returnType: BirType
        get() = _returnType
        set(value) {
            if(_returnType != value) {
               _returnType = value
               propertyChanged()
            }
        }

    private var _dispatchReceiverParameter: BirValueParameter? = dispatchReceiverParameter

    override var dispatchReceiverParameter: BirValueParameter?
        get() = _dispatchReceiverParameter
        set(value) {
            if(_dispatchReceiverParameter != value) {
               setChildField(_dispatchReceiverParameter, value, this.typeParameters)
               _dispatchReceiverParameter = value
               propertyChanged()
            }
        }

    private var _extensionReceiverParameter: BirValueParameter? = extensionReceiverParameter

    override var extensionReceiverParameter: BirValueParameter?
        get() = _extensionReceiverParameter
        set(value) {
            if(_extensionReceiverParameter != value) {
               setChildField(_extensionReceiverParameter, value, this._dispatchReceiverParameter ?:
                    this.typeParameters)
               _extensionReceiverParameter = value
               propertyChanged()
            }
        }

    override var valueParameters: BirChildElementList<BirValueParameter> =
            BirChildElementList(this, 2)

    private var _contextReceiverParametersCount: Int = contextReceiverParametersCount

    override var contextReceiverParametersCount: Int
        get() = _contextReceiverParametersCount
        set(value) {
            if(_contextReceiverParametersCount != value) {
               _contextReceiverParametersCount = value
               propertyChanged()
            }
        }

    private var _body: BirBody? = body

    override var body: BirBody?
        get() = _body
        set(value) {
            if(_body != value) {
               setChildField(_body, value, this.valueParameters)
               _body = value
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

    private var _isFakeOverride: Boolean = isFakeOverride

    override var isFakeOverride: Boolean
        get() = _isFakeOverride
        set(value) {
            if(_isFakeOverride != value) {
               _isFakeOverride = value
               propertyChanged()
            }
        }

    private var _attributeOwnerId: BirAttributeContainer = this

    override var attributeOwnerId: BirAttributeContainer
        get() = _attributeOwnerId
        set(value) {
            if(_attributeOwnerId != value) {
               _attributeOwnerId = value
               propertyChanged()
            }
        }

    private var _isTailrec: Boolean = isTailrec

    override var isTailrec: Boolean
        get() = _isTailrec
        set(value) {
            if(_isTailrec != value) {
               _isTailrec = value
               propertyChanged()
            }
        }

    private var _isSuspend: Boolean = isSuspend

    override var isSuspend: Boolean
        get() = _isSuspend
        set(value) {
            if(_isSuspend != value) {
               _isSuspend = value
               propertyChanged()
            }
        }

    private var _isOperator: Boolean = isOperator

    override var isOperator: Boolean
        get() = _isOperator
        set(value) {
            if(_isOperator != value) {
               _isOperator = value
               propertyChanged()
            }
        }

    private var _isInfix: Boolean = isInfix

    override var isInfix: Boolean
        get() = _isInfix
        set(value) {
            if(_isInfix != value) {
               _isInfix = value
               propertyChanged()
            }
        }

    private var _correspondingProperty: BirPropertySymbol? = correspondingProperty

    override var correspondingProperty: BirPropertySymbol?
        get() = _correspondingProperty
        set(value) {
            if(_correspondingProperty != value) {
               _correspondingProperty = value
               propertyChanged()
            }
        }
    init {
        initChildField(_dispatchReceiverParameter, typeParameters)
        initChildField(_extensionReceiverParameter, _dispatchReceiverParameter ?: typeParameters)
        initChildField(_body, valueParameters)
    }

    override fun getFirstChild(): BirElement? = typeParameters.firstOrNull() ?:
            _dispatchReceiverParameter ?: _extensionReceiverParameter ?:
            valueParameters.firstOrNull() ?: _body

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.typeParameters
        children[1] = this._dispatchReceiverParameter
        children[2] = this._extensionReceiverParameter
        children[3] = this.valueParameters
        children[4] = this._body
        return 5
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.typeParameters.acceptChildren(visitor)
        this._dispatchReceiverParameter?.accept(visitor)
        this._extensionReceiverParameter?.accept(visitor)
        this.valueParameters.acceptChildren(visitor)
        this._body?.accept(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._dispatchReceiverParameter === old -> this.dispatchReceiverParameter = new as
                BirValueParameter
           this._extensionReceiverParameter === old -> this.extensionReceiverParameter = new as
                BirValueParameter
           this._body === old -> this.body = new as BirBody
           else -> throwChildForReplacementNotFound(old)
        }
    }

    override fun getChildrenListById(id: Int): BirChildElementList<*> = when {
       id == 1 -> this.typeParameters
       id == 2 -> this.valueParameters
       else -> throwChildrenListWithIdNotFound(id)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        this.overriddenSymbols = this.overriddenSymbols.map { if(it === old) new as
                BirSimpleFunctionSymbol else it }
        if(this.correspondingProperty === old) this.correspondingProperty = new as BirPropertySymbol
    }
}
