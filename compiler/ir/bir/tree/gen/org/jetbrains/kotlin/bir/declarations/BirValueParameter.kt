/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.expressions.BirExpressionBody
import org.jetbrains.kotlin.bir.symbols.BirSymbolElement
import org.jetbrains.kotlin.bir.symbols.BirValueParameterSymbol
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI

/**
 * A leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.valueParameter]
 */
abstract class BirValueParameter : BirValueDeclaration(), BirDeclaration, BirSymbolElement,
        BirValueParameterSymbol {
    @ObsoleteDescriptorBasedAPI
    abstract override val _descriptor: ParameterDescriptor?

    abstract var index: Int

    abstract var varargElementType: BirType?

    abstract var isCrossinline: Boolean

    abstract var isNoinline: Boolean

    abstract var isHidden: Boolean

    context(BirTreeContext)
    abstract var defaultValue: BirExpressionBody?
}
