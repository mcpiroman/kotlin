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
    sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: PropertyDescriptor?,
    origin: IrDeclarationOrigin,
    visibility: DescriptorVisibility,
    name: Name,
    isExternal: Boolean,
    type: BirType,
    isFinal: Boolean,
    isStatic: Boolean,
    initializer: BirExpressionBody?,
    correspondingProperty: BirPropertySymbol?,
) : BirField() {
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

    private var _type: BirType = type

    override var type: BirType
        get() = _type
        set(value) {
            if(_type != value) {
               _type = value
               propertyChanged()
            }
        }

    private var _isFinal: Boolean = isFinal

    override var isFinal: Boolean
        get() = _isFinal
        set(value) {
            if(_isFinal != value) {
               _isFinal = value
               propertyChanged()
            }
        }

    private var _isStatic: Boolean = isStatic

    override var isStatic: Boolean
        get() = _isStatic
        set(value) {
            if(_isStatic != value) {
               _isStatic = value
               propertyChanged()
            }
        }

    private var _initializer: BirExpressionBody? = initializer

    override var initializer: BirExpressionBody?
        get() = _initializer
        set(value) {
            if(_initializer != value) {
               setChildField(_initializer, value, null)
               _initializer = value
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

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._initializer === old -> this.initializer = new as BirExpressionBody
           else -> throwChildForReplacementNotFound(old)
        }
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.correspondingProperty === old) this.correspondingProperty = new as BirPropertySymbol
    }
}
