/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.utils

import org.jetbrains.kotlin.bir.symbols.BirClassifierSymbol
import org.jetbrains.kotlin.bir.types.BirSimpleType
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.ir.util.IdSignature
import java.util.*

object BirTypeStatistics {
    private val typesByClassifierInstance = IdentityHashMap<BirClassifierSymbol, MutableList<BirSimpleType>>()
    private val typesByClassifierSignature = IdentityHashMap<IdSignature, MutableList<BirSimpleType>>()
    private var dynamicTypes = 0
    private var errorTypes = 0

    fun register(type: BirType) {

    }
}