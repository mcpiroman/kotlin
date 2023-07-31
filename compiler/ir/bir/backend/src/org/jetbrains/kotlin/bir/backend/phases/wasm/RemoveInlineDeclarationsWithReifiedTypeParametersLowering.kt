/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases.wasm

import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.BirFunction
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.remove

context(WasmBirContext)
class RemoveInlineDeclarationsWithReifiedTypeParametersLowering : BirLoweringPhase() {
    private val inlineFunctionsKey = registerElementsWithFeatureCacheKey<BirFunction>(false) { it.isInline }

    override fun invoke(module: BirModuleFragment) {
        getElementsWithFeature(inlineFunctionsKey).forEach { function ->
            if (function.typeParameters.any { it.isReified }) {
                function.remove()
            }
        }
    }
}