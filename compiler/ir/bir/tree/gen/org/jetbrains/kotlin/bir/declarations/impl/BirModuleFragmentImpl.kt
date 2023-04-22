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
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirFile
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.name.Name

class BirModuleFragmentImpl(
    override val sourceSpan: SourceSpan,
    override val _descriptor: ModuleDescriptor?,
    override val name: Name,
    override val irBuiltins: IrBuiltIns,
) : BirModuleFragment() {
    override val files: BirChildElementList<BirFile> = BirChildElementList(this)

    override fun getFirstChild(): BirElement? = files.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.files
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.files.acceptChildren(visitor)
    }
}
