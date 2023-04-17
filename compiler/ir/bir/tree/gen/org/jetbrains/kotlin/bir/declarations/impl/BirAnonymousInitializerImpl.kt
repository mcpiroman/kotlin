/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.declarations.BirAnonymousInitializer
import org.jetbrains.kotlin.bir.expressions.BirBlockBody
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.symbols.IrAnonymousInitializerSymbol

class BirAnonymousInitializerImpl @ObsoleteDescriptorBasedAPI constructor(
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: ClassDescriptor,
    override val symbol: IrAnonymousInitializerSymbol,
    override var isStatic: Boolean,
    body: BirBlockBody,
    override var origin: IrDeclarationOrigin,
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
) : BirAnonymousInitializer() {
    override var body: BirBlockBody = body
        set(value) {
            setChildField(field, value, null)
            field = value
        }
    init {
        initChildField(body, null)
    }

    override fun getFirstChild(): BirElement? = body

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.body
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.body.accept(visitor)
    }
}
