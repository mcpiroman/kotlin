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
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpressionBody
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

class BirValueParameterImpl @ObsoleteDescriptorBasedAPI constructor(
    sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: ParameterDescriptor?,
    origin: IrDeclarationOrigin,
    name: Name,
    type: BirType,
    override val isAssignable: Boolean,
    varargElementType: BirType?,
    isCrossinline: Boolean,
    isNoinline: Boolean,
    isHidden: Boolean,
    defaultValue: BirExpressionBody?,
) : BirValueParameter() {
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

    private var _name: Name = name

    override var name: Name
        get() = _name
        set(value) {
            if(_name != value) {
               _name = value
               propertyChanged()
            }
        }

    private var _type: BirType = type

    override var type: BirType
        get() = _type
        set(value) {
            if(_type != value) {
               _type = value
               propertyChanged()
            }
        }

    private var _varargElementType: BirType? = varargElementType

    override var varargElementType: BirType?
        get() = _varargElementType
        set(value) {
            if(_varargElementType != value) {
               _varargElementType = value
               propertyChanged()
            }
        }

    private var _isCrossinline: Boolean = isCrossinline

    override var isCrossinline: Boolean
        get() = _isCrossinline
        set(value) {
            if(_isCrossinline != value) {
               _isCrossinline = value
               propertyChanged()
            }
        }

    private var _isNoinline: Boolean = isNoinline

    override var isNoinline: Boolean
        get() = _isNoinline
        set(value) {
            if(_isNoinline != value) {
               _isNoinline = value
               propertyChanged()
            }
        }

    private var _isHidden: Boolean = isHidden

    override var isHidden: Boolean
        get() = _isHidden
        set(value) {
            if(_isHidden != value) {
               _isHidden = value
               propertyChanged()
            }
        }

    private var _defaultValue: BirExpressionBody? = defaultValue

    override var defaultValue: BirExpressionBody?
        get() = _defaultValue
        set(value) {
            if(_defaultValue != value) {
               setChildField(_defaultValue, value, null)
               _defaultValue = value
               propertyChanged()
            }
        }
    init {
        initChildField(_defaultValue, null)
    }

    override fun getFirstChild(): BirElement? = _defaultValue

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._defaultValue
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._defaultValue?.accept(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._defaultValue === old -> this.defaultValue = new as BirExpressionBody
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
