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
import org.jetbrains.kotlin.bir.expressions.BirContinue
import org.jetbrains.kotlin.bir.expressions.BirLoop
import org.jetbrains.kotlin.bir.types.BirType

class BirContinueImpl(
    sourceSpan: SourceSpan,
    type: BirType,
    loop: BirLoop,
    label: String?,
) : BirContinue() {
    private var _sourceSpan: SourceSpan = sourceSpan

    override var sourceSpan: SourceSpan
        get() = _sourceSpan
        set(value) {
            if(_sourceSpan != value) {
               _sourceSpan = value
               propertyChanged()
            }
        }

    private var _attributeOwnerId: BirAttributeContainer = this

    override var attributeOwnerId: BirAttributeContainer
        get() = _attributeOwnerId
        set(value) {
            if(_attributeOwnerId != value) {
               _attributeOwnerId = value
               propertyChanged()
            }
        }

    private var _type: BirType = type

    override var type: BirType
        get() = _type
        set(value) {
            if(_type != value) {
               _type = value
               propertyChanged()
            }
        }

    private var _loop: BirLoop = loop

    override var loop: BirLoop
        get() = _loop
        set(value) {
            if(_loop != value) {
               setTrackedElementReference(_loop, value, 0)
               _loop = value
               propertyChanged()
            }
        }

    private var _label: String? = label

    override var label: String?
        get() = _label
        set(value) {
            if(_label != value) {
               _label = value
               propertyChanged()
            }
        }

    override fun registerTrackedBackReferences(unregisterFrom: BirElementBase?) {
        registerTrackedBackReferenceTo(loop, 0, unregisterFrom)
    }
}
