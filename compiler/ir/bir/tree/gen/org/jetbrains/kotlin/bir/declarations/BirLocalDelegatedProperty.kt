/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.BirElementTrackingBackReferences
import org.jetbrains.kotlin.bir.symbols.BirLocalDelegatedPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbolElement
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.VariableDescriptorWithAccessors
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI

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

    abstract var type: BirType

    abstract var isVar: Boolean

    abstract var delegate: BirVariable

    abstract var getter: BirSimpleFunction

    abstract var setter: BirSimpleFunction?

    companion object
}
