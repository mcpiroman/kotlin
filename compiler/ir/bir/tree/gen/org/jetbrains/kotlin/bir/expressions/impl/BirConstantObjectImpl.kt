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
import org.jetbrains.kotlin.bir.expressions.BirConstantObject
import org.jetbrains.kotlin.bir.expressions.BirConstantValue
import org.jetbrains.kotlin.bir.symbols.BirConstructorSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.ir.types.IrType

context(BirTreeContext)
class BirConstantObjectImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    constructor: BirConstructorSymbol,
    override val typeArguments: List<IrType>,
) : BirConstantObject() {
    override var attributeOwnerId: BirAttributeContainer = this

    override var constructor: BirConstructorSymbol = constructor
        set(value) {
            setTrackedElementReferenceArrayStyle(field, value)
            field = value
        }

    override val valueArguments: BirChildElementList<BirConstantValue> =
            BirChildElementList(this)
    init {
        initTrackedElementReferenceArrayStyle(constructor)
    }

    override fun getFirstChild(): BirElement? = valueArguments.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.valueArguments
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.valueArguments.acceptChildren(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        if(this.constructor === old) this.constructor = new as BirConstructorSymbol
    }
}
