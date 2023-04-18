/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.symbols

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.BirReturnableBlock
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrFileSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.IdSignature

abstract class LateBindBirSymbol<out D : DeclarationDescriptor, E : BirElement>(
    val irSymbol: IrSymbol
) : BirPossiblyElementSymbol<D, E> {
    @ObsoleteDescriptorBasedAPI
    final override val descriptor: D
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


    class FileSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<PackageFragmentDescriptor, BirFile>(irSymbol), BirFileSymbol
    class ExternalPackageFragmentSymbol(irSymbol: IrSymbol) :
        LateBindBirSymbol<PackageFragmentDescriptor, BirExternalPackageFragment>(irSymbol), BirExternalPackageFragmentSymbol

    class AnonymousInitializerSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<ClassDescriptor, BirAnonymousInitializer>(irSymbol),
        BirAnonymousInitializerSymbol

    class EnumEntrySymbol(irSymbol: IrSymbol) : LateBindBirSymbol<ClassDescriptor, BirEnumEntry>(irSymbol), BirEnumEntrySymbol
    class FieldSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<PropertyDescriptor, BirField>(irSymbol), BirFieldSymbol
    class ClassSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<ClassDescriptor, BirClass>(irSymbol), BirClassSymbol
    class ScriptSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<ScriptDescriptor, BirScript>(irSymbol), BirScriptSymbol
    class TypeParameterSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<TypeParameterDescriptor, BirTypeParameter>(irSymbol),
        BirTypeParameterSymbol

    class ValueParameterSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<ParameterDescriptor, BirValueParameter>(irSymbol),
        BirValueParameterSymbol

    class VariableSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<VariableDescriptor, BirVariable>(irSymbol), BirVariableSymbol
    class ConstructorSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<ClassConstructorDescriptor, BirConstructor>(irSymbol),
        BirConstructorSymbol

    class SimpleFunctionSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<FunctionDescriptor, BirSimpleFunction>(irSymbol),
        BirSimpleFunctionSymbol

    class ReturnableBlockSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<FunctionDescriptor, BirReturnableBlock>(irSymbol),
        BirReturnableBlockSymbol

    class PropertySymbol(irSymbol: IrSymbol) : LateBindBirSymbol<PropertyDescriptor, BirProperty>(irSymbol), BirPropertySymbol
    class LocalDelegatedPropertySymbol(irSymbol: IrSymbol) :
        LateBindBirSymbol<VariableDescriptorWithAccessors, BirLocalDelegatedProperty>(irSymbol), BirLocalDelegatedPropertySymbol

    class TypeAliasSymbol(irSymbol: IrSymbol) : LateBindBirSymbol<TypeAliasDescriptor, BirTypeAlias>(irSymbol), BirTypeAliasSymbol
}

