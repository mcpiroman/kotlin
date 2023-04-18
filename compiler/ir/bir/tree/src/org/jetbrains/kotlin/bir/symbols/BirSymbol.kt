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
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.types.model.TypeConstructorMarker
import org.jetbrains.kotlin.types.model.TypeParameterMarker

interface BirSymbol : IrSymbol

class BirIrSymbolWrapper(
    val original: IrSymbol
) : BirSymbol, IrSymbol by original


interface BirPossiblyElementSymbol<out D : DeclarationDescriptor, E : BirElement> : BirSymbol {
    @ObsoleteDescriptorBasedAPI
    override val descriptor: D
}

inline val <reified E : BirElement> BirPossiblyElementSymbol<*, E>.asElement: E?
    get() = this as? E


interface BirPackageFragmentSymbol : BirSymbol {
    @ObsoleteDescriptorBasedAPI
    override val descriptor: PackageFragmentDescriptor
}

interface BirFileSymbol : BirPackageFragmentSymbol, BirPossiblyElementSymbol<PackageFragmentDescriptor, BirFile>

interface BirExternalPackageFragmentSymbol : BirPackageFragmentSymbol,
    BirPossiblyElementSymbol<PackageFragmentDescriptor, BirExternalPackageFragment>

interface BirAnonymousInitializerSymbol : BirPossiblyElementSymbol<ClassDescriptor, BirAnonymousInitializer>

interface BirEnumEntrySymbol : BirPossiblyElementSymbol<ClassDescriptor, BirEnumEntry>

interface BirFieldSymbol : BirPossiblyElementSymbol<PropertyDescriptor, BirField>

interface BirClassifierSymbol : BirSymbol, TypeConstructorMarker {
    @ObsoleteDescriptorBasedAPI
    override val descriptor: ClassifierDescriptor
}

interface BirClassSymbol : BirClassifierSymbol, BirPossiblyElementSymbol<ClassDescriptor, BirClass>

interface BirScriptSymbol : BirClassifierSymbol, BirPossiblyElementSymbol<ScriptDescriptor, BirScript>

interface BirTypeParameterSymbol : BirClassifierSymbol, BirPossiblyElementSymbol<TypeParameterDescriptor, BirTypeParameter>,
    TypeParameterMarker

interface BirValueSymbol : BirSymbol {
    @ObsoleteDescriptorBasedAPI
    override val descriptor: ValueDescriptor
}

interface BirValueParameterSymbol : BirValueSymbol, BirPossiblyElementSymbol<ParameterDescriptor, BirValueParameter>

interface BirVariableSymbol : BirValueSymbol, BirPossiblyElementSymbol<VariableDescriptor, BirVariable>

interface BirReturnTargetSymbol : BirSymbol {
    @ObsoleteDescriptorBasedAPI
    override val descriptor: FunctionDescriptor
}

interface BirFunctionSymbol : BirReturnTargetSymbol

interface BirConstructorSymbol : BirFunctionSymbol, BirPossiblyElementSymbol<ClassConstructorDescriptor, BirConstructor>

interface BirSimpleFunctionSymbol : BirFunctionSymbol, BirPossiblyElementSymbol<FunctionDescriptor, BirSimpleFunction>

interface BirReturnableBlockSymbol : BirReturnTargetSymbol, BirPossiblyElementSymbol<FunctionDescriptor, BirReturnableBlock>

interface BirPropertySymbol : BirPossiblyElementSymbol<PropertyDescriptor, BirProperty>

interface BirLocalDelegatedPropertySymbol : BirPossiblyElementSymbol<VariableDescriptorWithAccessors, BirLocalDelegatedProperty>

interface BirTypeAliasSymbol : BirPossiblyElementSymbol<TypeAliasDescriptor, BirTypeAlias>
