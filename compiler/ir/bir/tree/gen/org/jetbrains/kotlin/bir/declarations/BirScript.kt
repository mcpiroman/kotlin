/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.expressions.BirStatementContainer
import org.jetbrains.kotlin.bir.symbols.BirClassSymbol
import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirScriptSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbolElement
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI

/**
 * A leafB IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.script]
 */
abstract class BirScript : BirElementBase(), BirDeclaration, BirDeclarationWithName,
        BirStatementContainer, BirMetadataSourceOwner, BirSymbolElement, BirScriptSymbol {
    @ObsoleteDescriptorBasedAPI
    abstract override val _descriptor: ScriptDescriptor?

    context(BirTreeContext)
    abstract var thisReceiver: BirValueParameter?

    abstract var baseClass: BirType?

    abstract var explicitCallParameters: BirChildElementList<BirVariable>

    abstract var implicitReceiversParameters: BirChildElementList<BirValueParameter>

    abstract var providedProperties: List<BirPropertySymbol>

    abstract var providedPropertiesParameters: BirChildElementList<BirValueParameter>

    abstract var resultProperty: BirPropertySymbol?

    context(BirTreeContext)
    abstract var earlierScriptsParameter: BirValueParameter?

    abstract var earlierScripts: List<BirScriptSymbol>?

    abstract var targetClass: BirClassSymbol?

    abstract var constructor: BirConstructor?

    companion object
}
