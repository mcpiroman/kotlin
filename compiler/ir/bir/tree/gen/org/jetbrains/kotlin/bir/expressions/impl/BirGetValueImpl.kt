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
import org.jetbrains.kotlin.bir.declarations.BirValueDeclaration
import org.jetbrains.kotlin.bir.expressions.BirGetValue
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

class BirGetValueImpl(
    sourceSpan: SourceSpan,
    type: BirType,
    target: BirValueDeclaration,
    origin: IrStatementOrigin?,
) : BirGetValue() {
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

    private var _target: BirValueDeclaration = target

    override var target: BirValueDeclaration
        get() = _target
        set(value) {
            if(_target != value) {
               setTrackedElementReference(_target, value, 0)
               _target = value
               propertyChanged()
            }
        }

    private var _origin: IrStatementOrigin? = origin

    override var origin: IrStatementOrigin?
        get() = _origin
        set(value) {
            if(_origin != value) {
               _origin = value
               propertyChanged()
            }
        }

    override fun registerTrackedBackReferences(unregisterFrom: BirElementBase?) {
        registerTrackedBackReferenceTo(target, 0, unregisterFrom)
    }
}
