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
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.symbols.BirConstructorSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

class BirConstructorCallImpl(
    override var sourceSpan: SourceSpan,
    override var type: BirType,
    target: BirConstructorSymbol,
    dispatchReceiver: BirExpression?,
    extensionReceiver: BirExpression?,
    override var origin: IrStatementOrigin?,
    override var typeArguments: List<BirType?>,
    override var contextReceiversCount: Int,
    override var source: SourceElement,
    override var constructorTypeArgumentsCount: Int,
) : BirConstructorCall() {
    override var attributeOwnerId: BirAttributeContainer = this

    override var target: BirConstructorSymbol = target
        set(value) {
            setTrackedElementReference(field, value, 0)
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

    override var valueArguments: BirChildElementList<BirExpression> =
            BirChildElementList(this, 1)
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

    context(BirTreeContext)
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
        if(this.target === old) this.target = new as BirConstructorSymbol
    }

    override fun registerTrackedBackReferences(unregisterFrom: BirElementBase?) {
        registerTrackedBackReferenceTo(target, 0, unregisterFrom)
    }
}
