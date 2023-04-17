/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.expressions.BirBody
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType

/**
 * A non-leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.function]
 */
abstract class BirFunction : BirDeclarationBase(), BirPossiblyExternalDeclaration,
        BirDeclarationWithVisibility, BirTypeParametersContainer, BirSymbolOwner,
        BirDeclarationParent, BirReturnTarget, BirMemberWithContainerSource, BirMetadataSourceOwner
        {
    @ObsoleteDescriptorBasedAPI
    abstract override val descriptor: FunctionDescriptor

    abstract override val symbol: IrFunctionSymbol

    abstract var isInline: Boolean

    abstract var isExpect: Boolean

    abstract var returnType: IrType

    abstract var dispatchReceiverParameter: BirValueParameter?

    abstract var extensionReceiverParameter: BirValueParameter?

    abstract var valueParameters: BirChildElementList<BirValueParameter>

    abstract var contextReceiverParametersCount: Int

    abstract var body: BirBody?
}
