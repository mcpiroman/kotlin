/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirPropertyReference
import org.jetbrains.kotlin.bir.symbols.BirFieldSymbol
import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSimpleFunctionSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

class BirPropertyReferenceImpl(
    sourceSpan: SourceSpan,
    type: BirType,
    target: BirPropertySymbol,
    dispatchReceiver: BirExpression?,
    extensionReceiver: BirExpression?,
    origin: IrStatementOrigin?,
    override var typeArguments: List<BirType?>,
    field: BirFieldSymbol?,
    getter: BirSimpleFunctionSymbol?,
    setter: BirSimpleFunctionSymbol?,
) : BirPropertyReference() {
    private var _sourceSpan: SourceSpan = sourceSpan

    override var sourceSpan: SourceSpan
        get() = _sourceSpan
        set(value) {
            if(_sourceSpan != value) {
               _sourceSpan = value
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

    private var _type: BirType = type

    override var type: BirType
        get() = _type
        set(value) {
            if(_type != value) {
               _type = value
               propertyChanged()
            }
        }

    private var _target: BirPropertySymbol = target

    override var target: BirPropertySymbol
        get() = _target
        set(value) {
            if(_target != value) {
               setTrackedElementReference(_target, value, 0)
               _target = value
               propertyChanged()
            }
        }

    private var _dispatchReceiver: BirExpression? = dispatchReceiver

    override var dispatchReceiver: BirExpression?
        get() = _dispatchReceiver
        set(value) {
            if(_dispatchReceiver != value) {
               setChildField(_dispatchReceiver, value, null)
               _dispatchReceiver = value
               propertyChanged()
            }
        }

    private var _extensionReceiver: BirExpression? = extensionReceiver

    override var extensionReceiver: BirExpression?
        get() = _extensionReceiver
        set(value) {
            if(_extensionReceiver != value) {
               setChildField(_extensionReceiver, value, this._dispatchReceiver)
               _extensionReceiver = value
               propertyChanged()
            }
        }

    private var _origin: IrStatementOrigin? = origin

    override var origin: IrStatementOrigin?
        get() = _origin
        set(value) {
            if(_origin != value) {
               _origin = value
               propertyChanged()
            }
        }

    override var valueArguments: BirChildElementList<BirExpression> =
            BirChildElementList(this, 1)

    private var _field: BirFieldSymbol? = field

    override var field: BirFieldSymbol?
        get() = _field
        set(value) {
            if(_field != value) {
               setTrackedElementReference(_field, value, 1)
               _field = value
               propertyChanged()
            }
        }

    private var _getter: BirSimpleFunctionSymbol? = getter

    override var getter: BirSimpleFunctionSymbol?
        get() = _getter
        set(value) {
            if(_getter != value) {
               _getter = value
               propertyChanged()
            }
        }

    private var _setter: BirSimpleFunctionSymbol? = setter

    override var setter: BirSimpleFunctionSymbol?
        get() = _setter
        set(value) {
            if(_setter != value) {
               _setter = value
               propertyChanged()
            }
        }
    init {
        initChildField(_dispatchReceiver, null)
        initChildField(_extensionReceiver, _dispatchReceiver)
    }

    override fun getFirstChild(): BirElement? = _dispatchReceiver ?: _extensionReceiver ?:
            valueArguments.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._dispatchReceiver
        children[1] = this._extensionReceiver
        children[2] = this.valueArguments
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._dispatchReceiver?.accept(visitor)
        this._extensionReceiver?.accept(visitor)
        this.valueArguments.acceptChildren(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._dispatchReceiver === old -> this.dispatchReceiver = new as BirExpression
           this._extensionReceiver === old -> this.extensionReceiver = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }

    override fun getChildrenListById(id: Int): BirChildElementList<*> = when {
       id == 1 -> this.valueArguments
       else -> throwChildrenListWithIdNotFound(id)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.target === old) this.target = new as BirPropertySymbol
        if(this.field === old) this.field = new as BirFieldSymbol
        if(this.getter === old) this.getter = new as BirSimpleFunctionSymbol
        if(this.setter === old) this.setter = new as BirSimpleFunctionSymbol
    }

    override fun registerTrackedBackReferences(unregisterFrom: BirElementBase?) {
        registerTrackedBackReferenceTo(target, 0, unregisterFrom)
        registerTrackedBackReferenceTo(field, 1, unregisterFrom)
    }
}
