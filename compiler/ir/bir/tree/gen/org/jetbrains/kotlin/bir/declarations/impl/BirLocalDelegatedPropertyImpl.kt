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
import org.jetbrains.kotlin.bir.declarations.BirLocalDelegatedProperty
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.VariableDescriptorWithAccessors
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

class BirLocalDelegatedPropertyImpl @ObsoleteDescriptorBasedAPI constructor(
    sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: VariableDescriptorWithAccessors?,
    origin: IrDeclarationOrigin,
    name: Name,
    type: BirType,
    isVar: Boolean,
    delegate: BirVariable,
    getter: BirSimpleFunction,
    setter: BirSimpleFunction?,
) : BirLocalDelegatedProperty() {
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

    private var _isVar: Boolean = isVar

    override var isVar: Boolean
        get() = _isVar
        set(value) {
            if(_isVar != value) {
               _isVar = value
               propertyChanged()
            }
        }

    private var _delegate: BirVariable = delegate

    override var delegate: BirVariable
        get() = _delegate
        set(value) {
            if(_delegate != value) {
               setChildField(_delegate, value, null)
               _delegate = value
               propertyChanged()
            }
        }

    private var _getter: BirSimpleFunction = getter

    override var getter: BirSimpleFunction
        get() = _getter
        set(value) {
            if(_getter != value) {
               setChildField(_getter, value, this._delegate)
               _getter = value
               propertyChanged()
            }
        }

    private var _setter: BirSimpleFunction? = setter

    override var setter: BirSimpleFunction?
        get() = _setter
        set(value) {
            if(_setter != value) {
               setChildField(_setter, value, this._getter)
               _setter = value
               propertyChanged()
            }
        }
    init {
        initChildField(_delegate, null)
        initChildField(_getter, _delegate)
        initChildField(_setter, _getter)
    }

    override fun getFirstChild(): BirElement? = _delegate

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._delegate
        children[1] = this._getter
        children[2] = this._setter
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._delegate.accept(visitor)
        this._getter.accept(visitor)
        this._setter?.accept(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._delegate === old -> this.delegate = new as BirVariable
           this._getter === old -> this.getter = new as BirSimpleFunction
           this._setter === old -> this.setter = new as BirSimpleFunction
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
