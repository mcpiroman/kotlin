/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions

import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.symbols.BirFunctionSymbol

/**
 * A leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.functionReference]
 */
context(BirTreeContext)
abstract class BirFunctionReference : BirCallableReference<BirFunctionSymbol>() {
    abstract override var target: BirFunctionSymbol

    abstract var reflectionTarget: BirFunctionSymbol?
}
