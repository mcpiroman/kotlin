/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSimpleFunctionSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbolElement

/**
 * A non-leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.simpleFunction]
 */
abstract class BirSimpleFunction : BirFunction(),
        BirOverridableDeclaration<BirSimpleFunctionSymbol>, BirAttributeContainer, BirSymbolElement,
        BirSimpleFunctionSymbol {
    abstract var isTailrec: Boolean

    abstract var isSuspend: Boolean

    abstract override var isFakeOverride: Boolean

    abstract var isOperator: Boolean

    abstract var isInfix: Boolean

    abstract var correspondingProperty: BirPropertySymbol?

    abstract override var overriddenSymbols: List<BirSimpleFunctionSymbol>
}
