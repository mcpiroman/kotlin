/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.BirChildElementList
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementOrList
import org.jetbrains.kotlin.bir.BirStatement
import org.jetbrains.kotlin.bir.declarations.BirConstructor
import org.jetbrains.kotlin.bir.declarations.BirScript
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.symbols.BirClassSymbol
import org.jetbrains.kotlin.bir.symbols.BirPropertySymbol
import org.jetbrains.kotlin.bir.symbols.BirScriptSymbol
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

class BirScriptImpl @ObsoleteDescriptorBasedAPI constructor(
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: ScriptDescriptor,
    override var origin: IrDeclarationOrigin,
    override var name: Name,
    thisReceiver: BirValueParameter?,
    override var baseClass: IrType?,
    override var providedProperties: List<BirPropertySymbol>,
    override var resultProperty: BirPropertySymbol?,
    earlierScriptsParameter: BirValueParameter?,
    override var earlierScripts: List<BirScriptSymbol>?,
    override var targetClass: BirClassSymbol?,
    override var constructor: BirConstructor?,
) : BirScript() {
    override val statements: BirChildElementList<BirStatement> = BirChildElementList(this)

    override var thisReceiver: BirValueParameter? = thisReceiver
        set(value) {
            setChildField(field, value, this.statements)
            field = value
        }

    override var explicitCallParameters: BirChildElementList<BirVariable> =
            BirChildElementList(this)

    override var implicitReceiversParameters: BirChildElementList<BirValueParameter> =
            BirChildElementList(this)

    override var providedPropertiesParameters: BirChildElementList<BirValueParameter> =
            BirChildElementList(this)

    override var earlierScriptsParameter: BirValueParameter? = earlierScriptsParameter
        set(value) {
            setChildField(field, value, this.providedPropertiesParameters)
            field = value
        }
    init {
        initChildField(thisReceiver, statements)
        initChildField(earlierScriptsParameter, providedPropertiesParameters)
    }

    override fun getFirstChild(): BirElement? = statements.firstOrNull() ?: thisReceiver ?:
            explicitCallParameters.firstOrNull() ?: implicitReceiversParameters.firstOrNull() ?:
            providedPropertiesParameters.firstOrNull() ?: earlierScriptsParameter

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.statements
        children[1] = this.thisReceiver
        children[2] = this.explicitCallParameters
        children[3] = this.implicitReceiversParameters
        children[4] = this.providedPropertiesParameters
        children[5] = this.earlierScriptsParameter
        return 6
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.statements.acceptChildren(visitor)
        this.thisReceiver?.accept(visitor)
        this.explicitCallParameters.acceptChildren(visitor)
        this.implicitReceiversParameters.acceptChildren(visitor)
        this.providedPropertiesParameters.acceptChildren(visitor)
        this.earlierScriptsParameter?.accept(visitor)
    }

    override fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) {
        this.providedProperties = this.providedProperties.map { if(it === old) new as
                BirPropertySymbol else it }
        if(this.resultProperty === old) this.resultProperty = new as BirPropertySymbol
        this.earlierScripts = this.earlierScripts?.map { if(it === old) new as BirScriptSymbol else
                it }
        if(this.targetClass === old) this.targetClass = new as BirClassSymbol
    }
}
