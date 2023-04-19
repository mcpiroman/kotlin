/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirGetEnumValue
import org.jetbrains.kotlin.bir.symbols.BirEnumEntrySymbol
import org.jetbrains.kotlin.ir.types.IrType

class BirGetEnumValueImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    target: BirEnumEntrySymbol,
) : BirGetEnumValue() {
    override var attributeOwnerId: BirAttributeContainer = this

    override var target: BirEnumEntrySymbol = target
        set(value) {
            setTrackedElementReferenceArrayStyle(field, value)
            field = value
        }
    init {
        initTrackedElementReferenceArrayStyle(target)
    }
}
