/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.ValueClassRepresentation
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType

/**
 * A leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.class]
 */
abstract class BirClass : BirDeclarationBase(), BirPossiblyExternalDeclaration,
        BirDeclarationWithVisibility, BirTypeParametersContainer, BirDeclarationContainer,
        BirAttributeContainer, BirMetadataSourceOwner {
    @ObsoleteDescriptorBasedAPI
    abstract override val descriptor: ClassDescriptor

    abstract override val symbol: IrClassSymbol

    abstract var kind: ClassKind

    abstract var modality: Modality

    abstract var isCompanion: Boolean

    abstract var isInner: Boolean

    abstract var isData: Boolean

    abstract var isValue: Boolean

    abstract var isExpect: Boolean

    abstract var isFun: Boolean

    abstract val source: SourceElement

    abstract var superTypes: List<IrType>

    abstract var thisReceiver: BirValueParameter?

    abstract var valueClassRepresentation: ValueClassRepresentation<IrSimpleType>?

    abstract var sealedSubclasses: List<IrClassSymbol>
}
