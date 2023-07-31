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
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.declarations.BirTypeAlias
import org.jetbrains.kotlin.bir.declarations.BirTypeParameter
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

class BirTypeAliasImpl @ObsoleteDescriptorBasedAPI constructor(
    sourceSpan: SourceSpan,
    override var annotations: List<BirConstructorCall>,
    @property:ObsoleteDescriptorBasedAPI
    override val _descriptor: TypeAliasDescriptor?,
    origin: IrDeclarationOrigin,
    name: Name,
    visibility: DescriptorVisibility,
    isActual: Boolean,
    expandedType: BirType,
) : BirTypeAlias() {
    private var _sourceSpan: SourceSpan = sourceSpan

    override var sourceSpan: SourceSpan
        get() = _sourceSpan
        set(value) {
            if(_sourceSpan != value) {
               _sourceSpan = value
               propertyChanged()
            }
        }

    private var _origin: IrDeclarationOrigin = origin

    override var origin: IrDeclarationOrigin
        get() = _origin
        set(value) {
            if(_origin != value) {
               _origin = value
               propertyChanged()
            }
        }

    private var _name: Name = name

    override var name: Name
        get() = _name
        set(value) {
            if(_name != value) {
               _name = value
               propertyChanged()
            }
        }

    private var _visibility: DescriptorVisibility = visibility

    override var visibility: DescriptorVisibility
        get() = _visibility
        set(value) {
            if(_visibility != value) {
               _visibility = value
               propertyChanged()
            }
        }

    override var typeParameters: BirChildElementList<BirTypeParameter> =
            BirChildElementList(this, 1)

    private var _isActual: Boolean = isActual

    override var isActual: Boolean
        get() = _isActual
        set(value) {
            if(_isActual != value) {
               _isActual = value
               propertyChanged()
            }
        }

    private var _expandedType: BirType = expandedType

    override var expandedType: BirType
        get() = _expandedType
        set(value) {
            if(_expandedType != value) {
               _expandedType = value
               propertyChanged()
            }
        }

    override fun getFirstChild(): BirElement? = typeParameters.firstOrNull()

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = this.typeParameters
        return 1
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        this.typeParameters.acceptChildren(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
           else -> throwChildForReplacementNotFound(old)
        }
    }

    override fun getChildrenListById(id: Int): BirChildElementList<*> = when {
       id == 1 -> this.typeParameters
       else -> throwChildrenListWithIdNotFound(id)
    }
}
