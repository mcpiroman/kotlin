/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAnonymousInitializer
import org.jetbrains.kotlin.bir.expressions.BirBlockBody
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin

class BirAnonymousInitializerImpl @ObsoleteDescriptorBasedAPI constructor(
    override var sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: ClassDescriptor?,
    override var origin: IrDeclarationOrigin,
    override var isStatic: Boolean,
    body: BirBlockBody,
) : BirAnonymousInitializer() {
    private var _body: BirBlockBody = body

    context(BirTreeContext)
    override var body: BirBlockBody
        get() = _body
        set(value) {
            setChildField(_body, value, null)
            _body = value
        }
    init {
        initChildField(_body, null)
    }

    override fun getFirstChild(): BirElement? = _body

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this._body
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this._body.accept(visitor)
    }
}
