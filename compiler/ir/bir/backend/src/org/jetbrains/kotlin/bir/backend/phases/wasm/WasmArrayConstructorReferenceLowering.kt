/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases.wasm

import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.BirConstructor
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.expressions.BirFunctionReference
import org.jetbrains.kotlin.bir.utils.constructedClass

context(WasmBirContext)
class WasmArrayConstructorReferenceLowering : BirLoweringPhase() {
    private val possiblyArrayCreation = registerElementsWithFeatureCacheKey<BirFunctionReference>(false) { it.target is BirConstructor }

    override fun invoke(module: BirModuleFragment) {
        getElementsWithFeature(possiblyArrayCreation).forEach { functionRef ->
            val constructor = functionRef.target as BirConstructor

            // Array(size, init) -> create###Array(size, init)
            val creator = when (constructor.valueParameters.size) {
                2 -> wasmSymbols.primitiveTypeToCreateTypedArray[constructor.constructedClass]
                else -> null
            } ?: return@forEach

            functionRef.target = creator
            functionRef.reflectionTarget = creator
        }
    }
}