/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElementTrackingBackReferences
import org.jetbrains.kotlin.bir.expressions.BirBody
import org.jetbrains.kotlin.bir.symbols.BirFunctionSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbolElement
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.types.IrType

/**
 * A non-leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.function]
 */
interface BirFunction : BirDeclaration, BirDeclarationWithVisibility,
        BirPossiblyExternalDeclaration, BirTypeParametersContainer, BirReturnTarget,
        BirMemberWithContainerSource, BirMetadataSourceOwner, BirSymbolElement, BirFunctionSymbol,
        BirElementTrackingBackReferences {
    @ObsoleteDescriptorBasedAPI
    override val descriptor: FunctionDescriptor

    var isInline: Boolean

    var isExpect: Boolean

    var returnType: IrType

    var dispatchReceiverParameter: BirValueParameter?

    var extensionReceiverParameter: BirValueParameter?

    var valueParameters: BirChildElementList<BirValueParameter>

    var contextReceiverParametersCount: Int

    var body: BirBody?
}
