/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirTypeAlias
import org.jetbrains.kotlin.bir.declarations.BirTypeParameter
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

class BirTypeAliasImpl @ObsoleteDescriptorBasedAPI constructor(
    override var sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: TypeAliasDescriptor?,
    override var origin: IrDeclarationOrigin,
    override var name: Name,
    override var visibility: DescriptorVisibility,
    override var isActual: Boolean,
    override var expandedType: BirType,
) : BirTypeAlias() {
    override var typeParameters: BirChildElementList<BirTypeParameter> =
            BirChildElementList(this, 1)

    override fun getFirstChild(): BirElement? = typeParameters.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.typeParameters
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.typeParameters.acceptChildren(visitor)
    }

    context(BirTreeContext)
    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           else -> throwChildForReplacementNotFound(old)
        }
    }

    override fun getChildrenListById(id: Int): BirChildElementList<*> = when {
       id == 1 -> this.typeParameters
       else -> throwChildrenListWithIdNotFound(id)
    }
}
