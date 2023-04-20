/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.declarations.BirMemberWithContainerSource
import org.jetbrains.kotlin.bir.declarations.BirMetadataSourceOwner
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirMemberAccessExpression
import org.jetbrains.kotlin.bir.expressions.impl.BirNoExpressionImpl
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.symbols.LateBindBirSymbol
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrMemberWithContainerSource
import org.jetbrains.kotlin.ir.declarations.IrMetadataSourceOwner
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrUninitializedType
import java.util.*

@OptIn(ObsoleteDescriptorBasedAPI::class)
abstract class Ir2BirConverterBase {
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

    context(BirTreeContext)
    protected abstract fun convertIrElement(ir: IrElement): BirElement

    protected abstract fun elementRefMayAppearTwice(ir: IrElement): Boolean

    fun convertIrTree(treeContext: BirTreeContext, irRootElements: List<IrElement>): List<BirElement> {
        with(treeContext) {
            val birRootElements = irRootElements.map { mapIrElement(it) }
            lateBindSymbols()
            registerAuxStorage()
            return birRootElements
        }
    }

    context(BirTreeContext)
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

    context(BirTreeContext)
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

    context(BirTreeContext)
    @JvmName("mapIrElementNullable")
    protected fun mapIrElement(ir: IrElement?): BirElement? = if (ir == null) null else mapIrElement(ir)

    context(BirTreeContext)
    protected fun <Bir : BirElement> mapIrElementList(list: List<IrElement>): MutableList<Bir> {
        @Suppress("UNCHECKED_CAST")
        return list.mapTo(ArrayList(list.size)) { mapIrElement(it) } as MutableList<Bir>
    }

    context(BirTreeContext)
    protected fun <Ir : IrElement, Bir : BirElement> moveChildElementList(from: List<Ir>, to: BirChildElementList<Bir>) {
        for (ir in from) {
            @Suppress("UNCHECKED_CAST")
            val bir = mapIrElement(ir) as Bir
            to += bir
        }
    }

    context(BirTreeContext)
    protected fun moveIrMemberAccessExpressionValueArguments(ir: IrMemberAccessExpression<*>, bir: BirMemberAccessExpression<*>) {
        for (i in 0 until ir.valueArgumentsCount) {
            val arg = ir.getValueArgument(i)
            if (arg != null) {
                bir.valueArguments += mapIrElement(arg) as BirExpression
            } else {
                bir.valueArguments += BirNoExpressionImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, IrUninitializedType)
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
            symbol as BirS
        }
    }

    context(BirTreeContext)
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

    context(BirTreeContext)
    private fun registerAuxStorage() {
        ir2birElementMap.entries.forEach { (ir, bir) ->
            bir as BirElementBase

            if (ir is IrMetadataSourceOwner) {
                (bir as BirMetadataSourceOwner)[GlobalBirElementAuxStorageTokens.Metadata] = ir.metadata
            }

            if (ir is IrMemberWithContainerSource) {
                (bir as BirMemberWithContainerSource)[GlobalBirElementAuxStorageTokens.ContainerSource] = ir.containerSource
            }

            if (ir is IrAttributeContainer) {
                (bir as BirAttributeContainer)[GlobalBirElementAuxStorageTokens.OriginalBeforeInline] =
                    mapIrElement(ir.originalBeforeInline) as BirAttributeContainer?
            }

            if (ir is IrClass) {
                (bir as BirClass)[GlobalBirElementAuxStorageTokens.SealedSubclasses] = ir.sealedSubclasses
            }
        }
    }


    companion object {
        fun IrElement.convertToBir(treeContext: BirTreeContext): BirElement {
            val converter = Ir2BirConverter()
            return converter.convertIrTree(treeContext, listOf(this)).single()
        }
    }
}