/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.types

import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.hasEqualFqName
import org.jetbrains.kotlin.bir.symbols.BirClassSymbol
import org.jetbrains.kotlin.bir.symbols.BirClassifierSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isMarkedNullable

// The contents of irTypePredicates.kt is to be replaced by some de-duplicated code.


fun BirClassifierSymbol.isClassWithFqName(fqName: FqNameUnsafe): Boolean =
    this is BirClassSymbol && classFqNameEquals(this, fqName)

private fun classFqNameEquals(symbol: BirClassSymbol, fqName: FqNameUnsafe): Boolean {
    return classFqNameEquals(symbol as BirClass, fqName)
}

private fun classFqNameEquals(declaration: BirClass, fqName: FqNameUnsafe): Boolean =
    declaration.hasEqualFqName(fqName.toSafe())


fun BirType.isNotNullClassType(signature: IdSignature.CommonSignature) = isClassType(signature, nullable = false)
fun BirType.isNullableClassType(signature: IdSignature.CommonSignature) = isClassType(signature, nullable = true)

fun BirType.isClassType(signature: IdSignature.CommonSignature, nullable: Boolean? = null): Boolean {
    if (this !is BirSimpleType) return false
    if (nullable != null && this.isMarkedNullable() != nullable) return false
    return signature == classifier.signature ||
            classifier.let { it is BirClass && it.hasFqNameEqualToSignature(signature) }
}

private fun BirClass.hasFqNameEqualToSignature(signature: IdSignature.CommonSignature): Boolean =
    name.asString() == signature.shortName &&
            hasEqualFqName(FqName("${signature.packageFqName}.${signature.declarationFqName}"))