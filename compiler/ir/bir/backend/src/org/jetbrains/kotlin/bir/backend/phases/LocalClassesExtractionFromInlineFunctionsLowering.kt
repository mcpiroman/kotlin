/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.declarations.BirFunction
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.expressions.BirGetValue
import org.jetbrains.kotlin.bir.traversal.traverseStackBased

context (WasmBirContext)
class LocalClassesExtractionFromInlineFunctionsLowering : BirLoweringPhase() {
    private val inlineFunctionsKey = registerElementsWithFeatureCacheKey<BirFunction>(false) { it.isInline }

    override fun invoke(module: BirModuleFragment) {
        getElementsWithFeature(inlineFunctionsKey).forEach { function ->
            if (
                // Conservatively assume that functions with reified type parameters must be copied.
                function.typeParameters.none { it.isReified }
            ) {
                inlineClassesInFunction(function)
            }
        }
    }

    private fun inlineClassesInFunction(function: BirFunction) {
        val crossinlineParameters = function.valueParameters.filter { it.isCrossinline }
        val classesToExtract = mutableListOf<BirClass>()
        function.traverseStackBased { element ->
            if (element is BirClass) {
                if (
                    crossinlineParameters.none { param ->
                        param.referencedBy.any { ref ->
                            ref is BirGetValue && element.isAncestorOf(ref)
                        }
                    }
                ) {
                    classesToExtract += element
                }
            } else {
                element.walkIntoChildren()
            }
        }

        if (classesToExtract.isNotEmpty()) {
            popupLocalClasses(function) { it in classesToExtract }
        }
    }
}