/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.generator

import org.jetbrains.kotlin.bir.generator.Packages.declarations
import org.jetbrains.kotlin.bir.generator.Packages.exprs
import org.jetbrains.kotlin.bir.generator.Packages.symbols
import org.jetbrains.kotlin.bir.generator.Packages.tree
import org.jetbrains.kotlin.bir.generator.util.TypeKind
import org.jetbrains.kotlin.bir.generator.util.type

object Packages {
    const val tree = "org.jetbrains.kotlin.bir"
    const val exprs = "org.jetbrains.kotlin.bir.expressions"
    const val symbols = "org.jetbrains.kotlin.ir.symbols"
    const val declarations = "org.jetbrains.kotlin.bir.declarations"
    const val descriptors = "org.jetbrains.kotlin.descriptors"
}

val elementBaseType = type(tree, "BirElementBase", kind = TypeKind.Class)
val rootElement = type(tree, "BirElement")
val elementList = type(tree, "BirChildElementList")
val elementOrList = type(tree, "BirElementOrList")
val elementVisitor = type(tree + ".traversal", "BirElementVisitor")
val irTypeType = type("org.jetbrains.kotlin.ir.types", "IrType")
val statementOriginType = type("org.jetbrains.kotlin.ir.expressions", "IrStatementOrigin")
val symbolType = type(symbols, "IrSymbol")

object SymbolTypes {
    val packageFragment = type(symbols, "IrPackageFragmentSymbol")
    val file = type(symbols, "IrFileSymbol")
    val externalPackageFragment = type(symbols, "IrExternalPackageFragmentSymbol")
    val anonymousInitializer = type(symbols, "IrAnonymousInitializerSymbol")
    val enumEntry = type(symbols, "IrEnumEntrySymbol")
    val field = type(symbols, "IrFieldSymbol")
    val classifier = type(symbols, "IrClassifierSymbol")
    val `class` = type(symbols, "IrClassSymbol")
    val script = type(symbols, "IrScriptSymbol")
    val typeParameter = type(symbols, "IrTypeParameterSymbol")
    val value = type(symbols, "IrValueSymbol")
    val valueParameter = type(symbols, "IrValueParameterSymbol")
    val variable = type(symbols, "IrVariableSymbol")
    val returnTarget = type(symbols, "IrReturnTargetSymbol")
    val function = type(symbols, "IrFunctionSymbol")
    val constructor = type(symbols, "IrConstructorSymbol")
    val simpleFunction = type(symbols, "IrSimpleFunctionSymbol")
    val returnableBlock = type(symbols, "IrReturnableBlockSymbol")
    val property = type(symbols, "IrPropertySymbol")
    val localDelegatedProperty = type(symbols, "IrLocalDelegatedPropertySymbol")
    val typeAlias = type(symbols, "IrTypeAliasSymbol")
}