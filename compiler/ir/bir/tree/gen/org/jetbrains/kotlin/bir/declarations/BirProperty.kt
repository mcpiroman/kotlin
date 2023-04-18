/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbolElement
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI

/**
 * A non-leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.property]
 */
abstract class BirProperty : BirDeclarationBase(), BirPossiblyExternalDeclaration,
        BirOverridableDeclaration<BirPropertySymbol>, BirAttributeContainer,
        BirMemberWithContainerSource, BirSymbolElement, BirPropertySymbol {
    @ObsoleteDescriptorBasedAPI
    abstract override val descriptor: PropertyDescriptor

    abstract var isVar: Boolean

    abstract var isConst: Boolean

    abstract var isLateinit: Boolean

    abstract var isDelegated: Boolean

    abstract var isExpect: Boolean

    abstract override var isFakeOverride: Boolean

    abstract var backingField: BirField?

    abstract var getter: BirSimpleFunction?

    abstract var setter: BirSimpleFunction?

    abstract override var overriddenSymbols: List<BirPropertySymbol>
}
