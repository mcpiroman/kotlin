/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirSetField
import org.jetbrains.kotlin.bir.symbols.BirClassSymbol
import org.jetbrains.kotlin.bir.symbols.BirFieldSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

class BirSetFieldImpl(
    override var sourceSpan: SourceSpan,
    override var type: BirType,
    target: BirFieldSymbol,
    override var superQualifier: BirClassSymbol?,
    receiver: BirExpression?,
    override var origin: IrStatementOrigin?,
    value: BirExpression,
) : BirSetField() {
    override var attributeOwnerId: BirAttributeContainer = this

    override var target: BirFieldSymbol = target
        set(value) {
            setTrackedElementReference(field, value, 0)
            field = value
        }

    private var _receiver: BirExpression? = receiver

    context(BirTreeContext)
    override var receiver: BirExpression?
        get() = _receiver
        set(value) {
            setChildField(_receiver, value, null)
            _receiver = value
        }

    private var _value: BirExpression = value

    context(BirTreeContext)
    override var value: BirExpression
        get() = _value
        set(value) {
            setChildField(_value, value, this._receiver)
            _value = value
        }
    init {
        initChildField(_receiver, null)
        initChildField(_value, _receiver)
    }

    override fun getFirstChild(): BirElement? = _receiver ?: _value

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._receiver
        children[1] = this._value
        return 2
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._receiver?.accept(visitor)
        this._value.accept(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.target === old) this.target = new as BirFieldSymbol
        if(this.superQualifier === old) this.superQualifier = new as BirClassSymbol
    }

    override fun registerTrackedBackReferences(unregisterFrom: BirElementBase?) {
        registerTrackedBackReferenceTo(target, 0, unregisterFrom)
    }
}
