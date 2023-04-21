/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirLocalDelegatedProperty
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirLocalDelegatedPropertyReference
import org.jetbrains.kotlin.bir.symbols.BirSimpleFunctionSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.IrType

class BirLocalDelegatedPropertyReferenceImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    target: BirLocalDelegatedProperty,
    dispatchReceiver: BirExpression?,
    extensionReceiver: BirExpression?,
    override var origin: IrStatementOrigin?,
    override val typeArguments: Array<IrType?>,
    delegate: BirVariable,
    override var getter: BirSimpleFunctionSymbol,
    override var setter: BirSimpleFunctionSymbol?,
) : BirLocalDelegatedPropertyReference() {
    override var attributeOwnerId: BirAttributeContainer = this

    override var target: BirLocalDelegatedProperty = target
        set(value) {
            setTrackedElementReferenceArrayStyle(field, value)
            field = value
        }

    private var _dispatchReceiver: BirExpression? = dispatchReceiver

    context(BirTreeContext)
    override var dispatchReceiver: BirExpression?
        get() = _dispatchReceiver
        set(value) {
            setChildField(_dispatchReceiver, value, null)
            _dispatchReceiver = value
        }

    private var _extensionReceiver: BirExpression? = extensionReceiver

    context(BirTreeContext)
    override var extensionReceiver: BirExpression?
        get() = _extensionReceiver
        set(value) {
            setChildField(_extensionReceiver, value, this._dispatchReceiver)
            _extensionReceiver = value
        }

    override val valueArguments: BirChildElementList<BirExpression> =
            BirChildElementList(this)

    override var delegate: BirVariable = delegate
        set(value) {
            setTrackedElementReferenceArrayStyle(field, value)
            field = value
        }
    init {
        initChildField(_dispatchReceiver, null)
        initChildField(_extensionReceiver, _dispatchReceiver)
        initTrackedElementReferenceArrayStyle(target)
        initTrackedElementReferenceArrayStyle(delegate)
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

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.getter === old) this.getter = new as BirSimpleFunctionSymbol
        if(this.setter === old) this.setter = new as BirSimpleFunctionSymbol
    }
}
