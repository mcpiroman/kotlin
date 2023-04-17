/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.symbols.IrPackageFragmentSymbol
import org.jetbrains.kotlin.name.FqName

/**
 * A non-leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.packageFragment]
 */
abstract class BirPackageFragment : BirElementBase(), BirDeclarationContainer, BirSymbolOwner
        {
    abstract override val symbol: IrPackageFragmentSymbol

    @ObsoleteDescriptorBasedAPI
    abstract val descriptor: PackageFragmentDescriptor

    abstract var fqName: FqName
}
