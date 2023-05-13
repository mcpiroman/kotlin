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
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.SourceSpan
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
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

class BirScriptImpl @ObsoleteDescriptorBasedAPI constructor(
    override var sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: ScriptDescriptor?,
    override var origin: IrDeclarationOrigin,
    override var name: Name,
    thisReceiver: BirValueParameter?,
    override var baseClass: BirType?,
    override var providedProperties: List<BirPropertySymbol>,
    override var resultProperty: BirPropertySymbol?,
    earlierScriptsParameter: BirValueParameter?,
    override var earlierScripts: List<BirScriptSymbol>?,
    override var targetClass: BirClassSymbol?,
    override var constructor: BirConstructor?,
) : BirScript() {
    override val statements: BirChildElementList<BirStatement> = BirChildElementList(this, 1)

    private var _thisReceiver: BirValueParameter? = thisReceiver

    context(BirTreeContext)
    override var thisReceiver: BirValueParameter?
        get() = _thisReceiver
        set(value) {
            setChildField(_thisReceiver, value, this.statements)
            _thisReceiver = value
        }

    override var explicitCallParameters: BirChildElementList<BirVariable> =
            BirChildElementList(this, 2)

    override var implicitReceiversParameters: BirChildElementList<BirValueParameter> =
            BirChildElementList(this, 3)

    override var providedPropertiesParameters: BirChildElementList<BirValueParameter> =
            BirChildElementList(this, 4)

    private var _earlierScriptsParameter: BirValueParameter? = earlierScriptsParameter

    context(BirTreeContext)
    override var earlierScriptsParameter: BirValueParameter?
        get() = _earlierScriptsParameter
        set(value) {
            setChildField(_earlierScriptsParameter, value, this.providedPropertiesParameters)
            _earlierScriptsParameter = value
        }
    init {
        initChildField(_thisReceiver, statements)
        initChildField(_earlierScriptsParameter, providedPropertiesParameters)
    }

    override fun getFirstChild(): BirElement? = statements.firstOrNull() ?: _thisReceiver ?:
            explicitCallParameters.firstOrNull() ?: implicitReceiversParameters.firstOrNull() ?:
            providedPropertiesParameters.firstOrNull() ?: _earlierScriptsParameter

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.statements
        children[1] = this._thisReceiver
        children[2] = this.explicitCallParameters
        children[3] = this.implicitReceiversParameters
        children[4] = this.providedPropertiesParameters
        children[5] = this._earlierScriptsParameter
        return 6
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.statements.acceptChildren(visitor)
        this._thisReceiver?.accept(visitor)
        this.explicitCallParameters.acceptChildren(visitor)
        this.implicitReceiversParameters.acceptChildren(visitor)
        this.providedPropertiesParameters.acceptChildren(visitor)
        this._earlierScriptsParameter?.accept(visitor)
    }

    context(BirTreeContext)
    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           this._thisReceiver === old -> this.thisReceiver = new as BirValueParameter
           this._earlierScriptsParameter === old -> this.earlierScriptsParameter = new as
                BirValueParameter
           else -> throwChildForReplacementNotFound(old)
        }
    }

    override fun getChildrenListById(id: Int): BirChildElementList<*> = when {
       id == 1 -> this.statements
       id == 2 -> this.explicitCallParameters
       id == 3 -> this.implicitReceiversParameters
       id == 4 -> this.providedPropertiesParameters
       else -> throwChildrenListWithIdNotFound(id)
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
