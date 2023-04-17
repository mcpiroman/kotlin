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
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.MetadataSource
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrScriptSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

class BirScriptImpl @ObsoleteDescriptorBasedAPI constructor(
    override val symbol: IrScriptSymbol,
    thisReceiver: BirValueParameter?,
    override var baseClass: IrType?,
    override var providedProperties: List<IrPropertySymbol>,
    override var resultProperty: IrPropertySymbol?,
    earlierScriptsParameter: BirValueParameter?,
    override var earlierScripts: List<IrScriptSymbol>?,
    override var targetClass: IrClassSymbol?,
    override var constructor: BirConstructor?,
    @property:ObsoleteDescriptorBasedAPI
    override val descriptor: DeclarationDescriptor,
    override var origin: IrDeclarationOrigin,
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<BirConstructorCall>,
    override var name: Name,
    override var metadata: MetadataSource?,
) : BirScript() {
    override var thisReceiver: BirValueParameter? = thisReceiver
        set(value) {
            setChildField(field, value, null)
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

    override val statements: BirChildElementList<BirStatement> = BirChildElementList(this)
    init {
        initChildField(thisReceiver, null)
        initChildField(earlierScriptsParameter, providedPropertiesParameters)
    }

    override fun getFirstChild(): BirElement? = thisReceiver ?:
            explicitCallParameters.firstOrNull() ?: implicitReceiversParameters.firstOrNull() ?:
            providedPropertiesParameters.firstOrNull() ?: earlierScriptsParameter ?:
            statements.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.thisReceiver
        children[1] = this.explicitCallParameters
        children[2] = this.implicitReceiversParameters
        children[3] = this.providedPropertiesParameters
        children[4] = this.earlierScriptsParameter
        children[5] = this.statements
        return 6
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.thisReceiver?.accept(visitor)
        this.explicitCallParameters.acceptChildren(visitor)
        this.implicitReceiversParameters.acceptChildren(visitor)
        this.providedPropertiesParameters.acceptChildren(visitor)
        this.earlierScriptsParameter?.accept(visitor)
        this.statements.acceptChildren(visitor)
    }
}
