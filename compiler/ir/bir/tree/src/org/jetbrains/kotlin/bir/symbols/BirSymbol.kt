/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.symbols

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.BirReturnableBlock
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.types.model.TypeConstructorMarker
import org.jetbrains.kotlin.types.model.TypeParameterMarker

interface BirSymbol {
    val signature: IdSignature?
}

class BirIrSymbolWrapper(
    val original: IrSymbol
) : BirSymbol, IrSymbol by original

interface BirSymbolWithTypedDescriptor<out D : DeclarationDescriptor> : BirSymbol {
    /*@ObsoleteDescriptorBasedAPI
    override val descriptor: D*/
}

interface BirLLPossiblyElementSymbol : BirSymbol
interface BirPossiblyElementSymbol<out E : BirElement> : BirLLPossiblyElementSymbol

inline val <reified E : BirElement> BirPossiblyElementSymbol<E>.maybeAsElement: E?
    get() = this as? E

inline val <reified E : BirElement> BirPossiblyElementSymbol<E>.asElement: E
    get() = this as E


interface BirPackageFragmentSymbol : BirSymbol {
    /*@ObsoleteDescriptorBasedAPI
    override val descriptor: PackageFragmentDescriptor*/
}

interface BirFileSymbol : BirPackageFragmentSymbol, BirSymbolWithTypedDescriptor<PackageFragmentDescriptor>,
    BirPossiblyElementSymbol<BirFile>

interface BirExternalPackageFragmentSymbol : BirPackageFragmentSymbol,
    BirSymbolWithTypedDescriptor<PackageFragmentDescriptor>, BirPossiblyElementSymbol<BirExternalPackageFragment>

interface BirAnonymousInitializerSymbol : BirSymbolWithTypedDescriptor<ClassDescriptor>, BirPossiblyElementSymbol<BirAnonymousInitializer>

interface BirEnumEntrySymbol : BirSymbolWithTypedDescriptor<ClassDescriptor>, BirPossiblyElementSymbol<BirEnumEntry>

interface BirFieldSymbol : BirSymbolWithTypedDescriptor<PropertyDescriptor>, BirPossiblyElementSymbol<BirField>

interface BirClassifierSymbol : BirSymbol, TypeConstructorMarker {
    /*@ObsoleteDescriptorBasedAPI
    override val descriptor: ClassifierDescriptor*/
}

interface BirClassSymbol : BirClassifierSymbol, BirSymbolWithTypedDescriptor<ClassDescriptor>, BirPossiblyElementSymbol<BirClass>

interface BirScriptSymbol : BirClassifierSymbol, BirSymbolWithTypedDescriptor<ScriptDescriptor>, BirPossiblyElementSymbol<BirScript>

interface BirTypeParameterSymbol : BirClassifierSymbol, BirSymbolWithTypedDescriptor<TypeParameterDescriptor>,
    BirPossiblyElementSymbol<BirTypeParameter>,
    TypeParameterMarker

interface BirValueSymbol : BirSymbol {
    /*@ObsoleteDescriptorBasedAPI
    override val descriptor: ValueDescriptor*/
}

interface BirValueParameterSymbol : BirValueSymbol, BirSymbolWithTypedDescriptor<ParameterDescriptor>,
    BirPossiblyElementSymbol<BirValueParameter>

interface BirVariableSymbol : BirValueSymbol, BirSymbolWithTypedDescriptor<VariableDescriptor>, BirPossiblyElementSymbol<BirVariable>

interface BirReturnTargetSymbol : BirSymbol {
    /*@ObsoleteDescriptorBasedAPI
    override val descriptor: FunctionDescriptor*/
}

interface BirFunctionSymbol : BirReturnTargetSymbol //todo: , BirPossiblyElementSymbol<BirFunction>

interface BirConstructorSymbol : BirFunctionSymbol, BirSymbolWithTypedDescriptor<ClassConstructorDescriptor>,
    BirPossiblyElementSymbol<BirConstructor>

interface BirSimpleFunctionSymbol : BirFunctionSymbol, BirSymbolWithTypedDescriptor<FunctionDescriptor>,
    BirPossiblyElementSymbol<BirSimpleFunction>

interface BirReturnableBlockSymbol : BirReturnTargetSymbol, BirSymbolWithTypedDescriptor<FunctionDescriptor>,
    BirPossiblyElementSymbol<BirReturnableBlock>

interface BirPropertySymbol : BirSymbolWithTypedDescriptor<PropertyDescriptor>, BirPossiblyElementSymbol<BirProperty>

interface BirLocalDelegatedPropertySymbol : BirSymbolWithTypedDescriptor<VariableDescriptorWithAccessors>,
    BirPossiblyElementSymbol<BirLocalDelegatedProperty>

interface BirTypeAliasSymbol : BirSymbolWithTypedDescriptor<TypeAliasDescriptor>, BirPossiblyElementSymbol<BirTypeAlias>
