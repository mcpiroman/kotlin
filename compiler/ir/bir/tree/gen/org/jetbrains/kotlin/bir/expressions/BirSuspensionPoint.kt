/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions

import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.declarations.BirVariable

/**
 * A leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.suspensionPoint]
 */
context(BirTreeContext)
abstract class BirSuspensionPoint : BirExpression() {
    abstract var suspensionPointIdParameter: BirVariable

    abstract var result: BirExpression

    abstract var resumeResult: BirExpression
}
