/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases.wasm

import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.builders.build
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.expressions.BirCall
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.impl.BirBlockImpl
import org.jetbrains.kotlin.bir.replaceWith
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.utils.constructedClass

context(WasmBirContext)
class WasmArrayConstructorLowering : BirLoweringPhase() {
    private val possiblyArrayCreation = registerElementsWithFeatureCacheKey<BirConstructorCall>(false)

    override fun invoke(module: BirModuleFragment) {
        getElementsWithFeature(possiblyArrayCreation).forEach { call ->
            val constructor = call.target.asElement

            // Array(size, init) -> create###Array(size, init)
            val creator = when (constructor.valueParameters.size) {
                2 -> wasmSymbols.primitiveTypeToCreateTypedArray[constructor.constructedClass]
                else -> null
            } ?: return@forEach

            val creationExpression = BirBlockImpl(call.sourceSpan, call.type, null)
            creationExpression.statements += BirCall.build {
                sourceSpan = call.sourceSpan
                type = call.type
                target = creator
                typeArguments = call.typeArguments
                valueArguments moveAllFrom call.valueArguments
            }

            call.replaceWith(creationExpression)
        }
    }
}