/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.symbols.BirConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType

/**
 * A leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.constantObject]
 */
context(BirTreeContext)
abstract class BirConstantObject : BirConstantValue() {
    abstract var constructor: BirConstructorSymbol

    abstract val valueArguments: BirChildElementList<BirConstantValue>

    abstract val typeArguments: List<IrType>
}
