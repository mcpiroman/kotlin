/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.BirElementTrackingBackReferences
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.symbols.BirLocalDelegatedPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbolElement
import org.jetbrains.kotlin.descriptors.VariableDescriptorWithAccessors
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.types.IrType

/**
 * A leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.localDelegatedProperty]
 */
abstract class BirLocalDelegatedProperty : BirElementBase(), BirDeclaration,
        BirDeclarationWithName, BirMetadataSourceOwner, BirSymbolElement,
        BirLocalDelegatedPropertySymbol, BirElementTrackingBackReferences {
    @ObsoleteDescriptorBasedAPI
    abstract override val _descriptor: VariableDescriptorWithAccessors?

    abstract var type: IrType

    abstract var isVar: Boolean

    context(BirTreeContext)
    abstract var delegate: BirVariable

    context(BirTreeContext)
    abstract var getter: BirSimpleFunction

    context(BirTreeContext)
    abstract var setter: BirSimpleFunction?
}
