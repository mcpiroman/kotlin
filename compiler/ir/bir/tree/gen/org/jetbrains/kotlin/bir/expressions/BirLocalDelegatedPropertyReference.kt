/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions

import org.jetbrains.kotlin.bir.symbols.BirLocalDelegatedPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSimpleFunctionSymbol
import org.jetbrains.kotlin.bir.symbols.BirVariableSymbol

/**
 * A leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.localDelegatedPropertyReference]
 */
abstract class BirLocalDelegatedPropertyReference :
        BirCallableReference<BirLocalDelegatedPropertySymbol>() {
    abstract override var target: BirLocalDelegatedPropertySymbol

    abstract var delegate: BirVariableSymbol

    abstract var getter: BirSimpleFunctionSymbol

    abstract var setter: BirSimpleFunctionSymbol?
}
