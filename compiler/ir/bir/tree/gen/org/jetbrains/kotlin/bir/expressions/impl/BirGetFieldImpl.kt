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
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirGetField
import org.jetbrains.kotlin.bir.symbols.BirClassSymbol
import org.jetbrains.kotlin.bir.symbols.BirFieldSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

class BirGetFieldImpl(
    sourceSpan: SourceSpan,
    type: BirType,
    target: BirFieldSymbol,
    superQualifier: BirClassSymbol?,
    receiver: BirExpression?,
    origin: IrStatementOrigin?,
) : BirGetField() {
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

    private var _target: BirFieldSymbol = target

    override var target: BirFieldSymbol
        get() = _target
        set(value) {
            if(_target != value) {
               setTrackedElementReference(_target, value, 0)
               _target = value
               propertyChanged()
            }
        }

    private var _superQualifier: BirClassSymbol? = superQualifier

    override var superQualifier: BirClassSymbol?
        get() = _superQualifier
        set(value) {
            if(_superQualifier != value) {
               _superQualifier = value
               propertyChanged()
            }
        }

    private var _receiver: BirExpression? = receiver

    override var receiver: BirExpression?
        get() = _receiver
        set(value) {
            if(_receiver != value) {
               setChildField(_receiver, value, null)
               _receiver = value
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
    init {
        initChildField(_receiver, null)
    }

    override fun getFirstChild(): BirElement? = _receiver

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._receiver
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._receiver?.accept(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._receiver === old -> this.receiver = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.target === old) this.target = new as BirFieldSymbol
        if(this.superQualifier === old) this.superQualifier = new as BirClassSymbol
    }

    override fun registerTrackedBackReferences(unregisterFrom: BirElementBase?) {
        registerTrackedBackReferenceTo(target, 0, unregisterFrom)
    }
}
