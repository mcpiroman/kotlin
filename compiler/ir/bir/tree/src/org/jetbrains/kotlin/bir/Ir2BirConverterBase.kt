/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirMemberAccessExpression
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.types.IrType
import java.util.IdentityHashMap
import org.jetbrains.kotlin.ir.symbols.*
import java.util.*

@OptIn(ObsoleteDescriptorBasedAPI::class)
abstract class Ir2BirConverterBase() {
    private var ir2birElementMap = IdentityHashMap<IrElement, BirElement>()
    private var currentlyMappedElement: IrElement? = null

    fun setExpectedTreeSize(size: Int) {
        val old = ir2birElementMap
        ir2birElementMap = IdentityHashMap<IrElement, BirElement>(size)
        ir2birElementMap.putAll(old)
    }

    protected abstract fun convertIrElement(ir: IrElement): BirElement
    protected abstract fun elementRefMayAppearTwice(ir: IrElement): Boolean

    fun convertIrTree(irRootElements: List<IrElement>): List<BirElement> {
        return irRootElements.map { mapIrElement(it) }
    }

    protected fun mapIrElement(ir: IrElement): BirElement {
        if (ir === currentlyMappedElement) {
            return TmpBirAttributeContainerForThisSubstitution
        }

        val last = currentlyMappedElement
        currentlyMappedElement = ir

        val new = if (elementRefMayAppearTwice(ir)) {
            ir2birElementMap[ir] ?: doConvertElement(ir)
        } else {
            assert(ir !in ir2birElementMap)
            doConvertElement(ir)
        }

        currentlyMappedElement = last
        return new
    }

    private fun doConvertElement(ir: IrElement): BirElement {
        val bir = convertIrElement(ir)
        //(bir as BirElementBase).originalIrElement = ir
        return bir
    }

    protected fun registerNewElement(ir: IrElement, bir: BirElement) {
        if (elementRefMayAppearTwice(ir)) {
            ir2birElementMap[ir] = bir
        }
    }

    @JvmName("mapIrElementNullable")
    protected fun mapIrElement(ir: IrElement?): BirElement? = if (ir == null) null else mapIrElement(ir)

    protected fun <Bir : BirElement> mapIrElementList(list: List<IrElement>): MutableList<Bir> {
        @Suppress("UNCHECKED_CAST")
        return list.mapTo(ArrayList(list.size)) { mapIrElement(it) } as MutableList<Bir>
    }

    protected fun <Ir : IrElement, Bir : BirElement> moveChildElementList(from: List<Ir>, to: BirChildElementList<Bir>) {
        for (ir in from) {
            @Suppress("UNCHECKED_CAST")
            val bir = mapIrElement(ir) as Bir
            to += bir
        }
    }

    protected fun moveIrMemberAccessExpressionValueArguments(ir: IrMemberAccessExpression<*>, bir: BirMemberAccessExpression<*>) {
        for (i in 0 until ir.valueArgumentsCount) {
            val arg = ir.getValueArgument(i)
            if (arg != null) {
                bir.valueArguments += mapIrElement(arg) as BirExpression
            }
        }
    }

    protected val IrMemberAccessExpression<*>.typeArguments: Array<IrType?>
        get() = Array(typeArgumentsCount) { getTypeArgument(it) }

    protected fun <IrS : IrSymbol> mapSymbol(ir: IrElement, symbol: IrS): IrS = symbol


    companion object {
        fun IrElement.convertToBir(): BirElement {
            val converter = Ir2BirConverter()
            return converter.convertIrTree(listOf(this)).single()
        }
    }

    private object TmpBirAttributeContainerForThisSubstitution : BirAttributeContainer {
        override var attributeOwnerId: BirAttributeContainer
            get() = TODO("Not yet implemented")
            set(value) {}
        override var originalBeforeInline: BirAttributeContainer?
            get() = TODO("Not yet implemented")
            set(value) {}
        override val startOffset: Int
            get() = TODO("Not yet implemented")
        override val endOffset: Int
            get() = TODO("Not yet implemented")
        override val parent: BirElement?
            get() = TODO("Not yet implemented")

        override fun acceptChildren(visitor: BirElementVisitor) {
            TODO("Not yet implemented")
        }
    }
}