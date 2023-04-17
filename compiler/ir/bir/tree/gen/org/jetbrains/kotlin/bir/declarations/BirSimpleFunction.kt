/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

/**
 * A non-leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.simpleFunction]
 */
abstract class BirSimpleFunction : BirFunction(),
        BirOverridableDeclaration<IrSimpleFunctionSymbol>, BirAttributeContainer {
    abstract override val symbol: IrSimpleFunctionSymbol

    abstract var isTailrec: Boolean

    abstract var isSuspend: Boolean

    abstract override var isFakeOverride: Boolean

    abstract var isOperator: Boolean

    abstract var isInfix: Boolean

    abstract var correspondingPropertySymbol: IrPropertySymbol?

    abstract override var overriddenSymbols: List<IrSimpleFunctionSymbol>
}
