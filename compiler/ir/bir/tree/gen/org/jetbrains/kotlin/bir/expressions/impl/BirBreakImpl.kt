/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirBreak
import org.jetbrains.kotlin.bir.expressions.BirLoop
import org.jetbrains.kotlin.ir.types.IrType

class BirBreakImpl(
    override val sourceSpan: SourceSpan,
    override var type: IrType,
    loop: BirLoop,
    override var label: String?,
) : BirBreak() {
    override var attributeOwnerId: BirAttributeContainer = this

    override var loop: BirLoop = loop
        set(value) {
            setTrackedElementReference(field, value, 0)
            field = value
        }

    override fun registerTrackedBackReferences(unregisterFrom: BirElementBase?) {
        registerTrackedBackReferenceTo(loop, 0, unregisterFrom)
    }
}
