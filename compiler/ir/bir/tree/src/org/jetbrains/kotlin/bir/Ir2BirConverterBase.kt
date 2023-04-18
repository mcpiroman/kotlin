/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirMemberAccessExpression
import org.jetbrains.kotlin.bir.symbols.BirIrSymbolWrapper
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.symbols.LateBindBirSymbol
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
    private val elementsWithSymbolsToLateBind = mutableListOf<Pair<IrElement, LateBindBirSymbol<*, *>>>()
    private var currentlyConvertedElement: IrElement? = null
    private var lastNewRegisteredElement: BirElement? = null
    private var lastNewRegisteredElementSource: IrElement? = null

    fun setExpectedTreeSize(size: Int) {
        val old = ir2birElementMap
        ir2birElementMap = IdentityHashMap<IrElement, BirElement>(size)
        ir2birElementMap.putAll(old)
    }

    protected abstract fun convertIrElement(ir: IrElement): BirElement
    protected abstract fun elementRefMayAppearTwice(ir: IrElement): Boolean

    fun convertIrTree(irRootElements: List<IrElement>): List<BirElement> {
        val birRootElements = irRootElements.map { mapIrElement(it) }
        lateBindSymbols()
        return birRootElements
    }

    protected fun mapIrElement(ir: IrElement): BirElement {
        // Optimization for converting reference to self if it comes directly after [registerNewElement]
        if (ir === lastNewRegisteredElementSource) {
            return lastNewRegisteredElement!!
        }

        val new = if (elementRefMayAppearTwice(ir)) {
            ir2birElementMap[ir] ?: doConvertElement(ir)
        } else {
            assert(ir !in ir2birElementMap)
            doConvertElement(ir)
        }

        return new
    }

    private fun doConvertElement(ir: IrElement): BirElement {
        val last = currentlyConvertedElement
        currentlyConvertedElement = ir

        val bir = convertIrElement(ir)
        //(bir as BirElementBase).originalIrElement = ir

        currentlyConvertedElement = last
        return bir
    }

    protected fun registerNewElement(ir: IrElement, bir: BirElement) {
        lastNewRegisteredElement = bir
        lastNewRegisteredElementSource = ir

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


    protected fun <IrS : IrSymbol, BirS : BirSymbol> mapSymbol(ir: IrElement, symbol: IrS): BirS {
        return if (symbol.isBound) {
            ir2birElementMap[symbol.owner] as BirS? ?: run {
                val birSymbol = when (symbol) {
                    is IrFileSymbol -> LateBindBirSymbol.FileSymbol(symbol)
                    is IrExternalPackageFragmentSymbol -> LateBindBirSymbol.ExternalPackageFragmentSymbol(symbol)
                    is IrAnonymousInitializerSymbol -> LateBindBirSymbol.AnonymousInitializerSymbol(symbol)
                    is IrEnumEntrySymbol -> LateBindBirSymbol.EnumEntrySymbol(symbol)
                    is IrFieldSymbol -> LateBindBirSymbol.FieldSymbol(symbol)
                    is IrClassSymbol -> LateBindBirSymbol.ClassSymbol(symbol)
                    is IrScriptSymbol -> LateBindBirSymbol.ScriptSymbol(symbol)
                    is IrTypeParameterSymbol -> LateBindBirSymbol.TypeParameterSymbol(symbol)
                    is IrValueParameterSymbol -> LateBindBirSymbol.ValueParameterSymbol(symbol)
                    is IrVariableSymbol -> LateBindBirSymbol.VariableSymbol(symbol)
                    is IrConstructorSymbol -> LateBindBirSymbol.ConstructorSymbol(symbol)
                    is IrSimpleFunctionSymbol -> LateBindBirSymbol.SimpleFunctionSymbol(symbol)
                    is IrReturnableBlockSymbol -> LateBindBirSymbol.ReturnableBlockSymbol(symbol)
                    is IrPropertySymbol -> LateBindBirSymbol.PropertySymbol(symbol)
                    is IrLocalDelegatedPropertySymbol -> LateBindBirSymbol.LocalDelegatedPropertySymbol(symbol)
                    is IrTypeAliasSymbol -> LateBindBirSymbol.TypeAliasSymbol(symbol)
                    else -> error(symbol)
                }
                elementsWithSymbolsToLateBind += ir to birSymbol
                birSymbol as BirS
            }
        } else {
            BirIrSymbolWrapper(symbol) as BirS
        }
    }

    private fun lateBindSymbols() {
        while (true) {
            // new elements may appear in [mapIrElement] call
            val (irElement, lateBindBirSymbol) = elementsWithSymbolsToLateBind.removeLastOrNull()
                ?: break

            val containerElement = ir2birElementMap.getValue(irElement) as BirElementBase
            // Apparently the symbol owner element may be in IrExternalPackageFragment, and such
            // fragments are not directly linked to the tree (thus not visited).
            val birElementBehindSymbol = mapIrElement(lateBindBirSymbol.irSymbol.owner)
            containerElement.replaceSymbolProperty(lateBindBirSymbol, birElementBehindSymbol as BirSymbol)
        }
    }


    companion object {
        fun IrElement.convertToBir(): BirElement {
            val converter = Ir2BirConverter()
            return converter.convertIrTree(listOf(this)).single()
        }
    }
}