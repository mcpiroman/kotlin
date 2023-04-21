/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions

import org.jetbrains.kotlin.bir.BirElementTrackingBackReferences
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

/**
 * A non-leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.loop]
 */
abstract class BirLoop : BirExpression(), BirElementTrackingBackReferences {
    abstract var origin: IrStatementOrigin?

    context(BirTreeContext)
    abstract var body: BirExpression?

    context(BirTreeContext)
    abstract var condition: BirExpression

    abstract var label: String?
}
