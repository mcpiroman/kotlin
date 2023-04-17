/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.expressions.BirStatementContainer
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrScriptSymbol
import org.jetbrains.kotlin.ir.types.IrType

/**
 * A leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.script]
 */
abstract class BirScript : BirDeclarationBase(), BirDeclarationWithName,
        BirDeclarationParent, BirStatementContainer, BirMetadataSourceOwner {
    abstract override val symbol: IrScriptSymbol

    abstract var thisReceiver: BirValueParameter?

    abstract var baseClass: IrType?

    abstract var explicitCallParameters: BirChildElementList<BirVariable>

    abstract var implicitReceiversParameters: BirChildElementList<BirValueParameter>

    abstract var providedProperties: List<IrPropertySymbol>

    abstract var providedPropertiesParameters: BirChildElementList<BirValueParameter>

    abstract var resultProperty: IrPropertySymbol?

    abstract var earlierScriptsParameter: BirValueParameter?

    abstract var earlierScripts: List<IrScriptSymbol>?

    abstract var targetClass: IrClassSymbol?

    abstract var constructor: BirConstructor?
}
