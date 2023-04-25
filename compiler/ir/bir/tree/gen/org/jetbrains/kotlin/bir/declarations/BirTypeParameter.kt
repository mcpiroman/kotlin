/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.symbols.BirSymbolElement
import org.jetbrains.kotlin.bir.symbols.BirTypeParameterSymbol
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.types.Variance

/**
 * A leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.typeParameter]
 */
abstract class BirTypeParameter : BirElementBase(), BirDeclaration, BirDeclarationWithName,
        BirSymbolElement, BirTypeParameterSymbol {
    @ObsoleteDescriptorBasedAPI
    abstract override val _descriptor: TypeParameterDescriptor?

    abstract var variance: Variance

    abstract var isReified: Boolean

    abstract var superTypes: List<BirType>
}
