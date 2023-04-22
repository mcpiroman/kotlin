/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.symbols

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.BirReturnableBlock
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.IdSignature

abstract class LateBindBirSymbol<E : BirElement>(
    val irSymbol: IrSymbol
) : BirPossiblyElementSymbol<E> {
    @ObsoleteDescriptorBasedAPI
    final override val descriptor: DeclarationDescriptor
        get() = error("Not bound")

    @ObsoleteDescriptorBasedAPI
    final override val hasDescriptor: Boolean
        get() = error("Not bound")

    final override val isBound: Boolean
        get() = false

    final override val owner: IrSymbolOwner
        get() = TODO("IR fragment in BIR context")

    final override var privateSignature: IdSignature?
        get() = error("Not bound")
        set(value) = error("Not bound")

    final override val signature: IdSignature
        get() = error("Not bound")


    class FileSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirFile>(irSymbol), BirFileSymbol
    class ExternalPackageFragmentSymbol(irSymbol: IrSymbol) :
        LateBindBirSymbol<BirExternalPackageFragment>(irSymbol), BirExternalPackageFragmentSymbol

    class AnonymousInitializerSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirAnonymousInitializer>(irSymbol),
        BirAnonymousInitializerSymbol

    class EnumEntrySymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirEnumEntry>(irSymbol), BirEnumEntrySymbol
    class FieldSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirField>(irSymbol), BirFieldSymbol
    class ClassSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirClass>(irSymbol), BirClassSymbol
    class ScriptSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirScript>(irSymbol), BirScriptSymbol
    class TypeParameterSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirTypeParameter>(irSymbol),
        BirTypeParameterSymbol

    class ValueParameterSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirValueParameter>(irSymbol),
        BirValueParameterSymbol

    class VariableSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirVariable>(irSymbol), BirVariableSymbol
    class ConstructorSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirConstructor>(irSymbol),
        BirConstructorSymbol

    class SimpleFunctionSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirSimpleFunction>(irSymbol),
        BirSimpleFunctionSymbol

    class ReturnableBlockSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirReturnableBlock>(irSymbol),
        BirReturnableBlockSymbol

    class PropertySymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirProperty>(irSymbol), BirPropertySymbol
    class LocalDelegatedPropertySymbol(irSymbol: IrSymbol) :
        LateBindBirSymbol<BirLocalDelegatedProperty>(irSymbol), BirLocalDelegatedPropertySymbol

    class TypeAliasSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<BirTypeAlias>(irSymbol), BirTypeAliasSymbol
}

