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
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirCatch
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirTry
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.bir.types.BirType

class BirTryImpl(
    override var sourceSpan: SourceSpan,
    override var type: BirType,
    tryResult: BirExpression,
    finallyExpression: BirExpression?,
) : BirTry() {
    override var attributeOwnerId: BirAttributeContainer = this

    private var _tryResult: BirExpression = tryResult

    context(BirTreeContext)
    override var tryResult: BirExpression
        get() = _tryResult
        set(value) {
            setChildField(_tryResult, value, null)
            _tryResult = value
        }

    override val catches: BirChildElementList<BirCatch> = BirChildElementList(this)

    private var _finallyExpression: BirExpression? = finallyExpression

    context(BirTreeContext)
    override var finallyExpression: BirExpression?
        get() = _finallyExpression
        set(value) {
            setChildField(_finallyExpression, value, this.catches)
            _finallyExpression = value
        }
    init {
        initChildField(_tryResult, null)
        initChildField(_finallyExpression, catches)
    }

    override fun getFirstChild(): BirElement? = _tryResult

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._tryResult
        children[1] = this.catches
        children[2] = this._finallyExpression
        return 3
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._tryResult.accept(visitor)
        this.catches.acceptChildren(visitor)
        this._finallyExpression?.accept(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._tryResult === old -> this._tryResult = new as BirExpression
           this._finallyExpression === old -> this._finallyExpression = new as BirExpression
           else -> throwChildForReplacementNotFound(old)
        }
    }
}
