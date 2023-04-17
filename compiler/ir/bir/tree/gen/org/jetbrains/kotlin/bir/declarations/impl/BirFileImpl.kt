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
import org.jetbrains.kotlin.bir.declarations.BirDeclaration
import org.jetbrains.kotlin.bir.declarations.BirFile
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.MetadataSource
import org.jetbrains.kotlin.ir.symbols.IrFileSymbol
import org.jetbrains.kotlin.name.FqName

class BirFileImpl @ObsoleteDescriptorBasedAPI constructor(
    override val symbol: IrFileSymbol,
    override var module: BirModuleFragment,
    override var fileEntry: IrFileEntry,
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: PackageFragmentDescriptor,
    override var fqName: FqName,
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    override var metadata: MetadataSource?,
) : BirFile() {
    override val declarations: BirChildElementList<BirDeclaration> =
            BirChildElementList(this)

    override fun getFirstChild(): BirElement? = declarations.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.declarations
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.declarations.acceptChildren(visitor)
    }
}
