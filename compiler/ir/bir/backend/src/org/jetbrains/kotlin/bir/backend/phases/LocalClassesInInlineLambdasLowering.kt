/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.BirBlock
import org.jetbrains.kotlin.bir.expressions.BirCall
import org.jetbrains.kotlin.bir.expressions.BirFunctionExpression
import org.jetbrains.kotlin.bir.expressions.impl.BirBlockImpl
import org.jetbrains.kotlin.bir.expressions.impl.BirCompositeImpl
import org.jetbrains.kotlin.bir.replaceWith
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.bir.utils.ancestors
import org.jetbrains.kotlin.bir.utils.isAdaptedFunctionReference
import org.jetbrains.kotlin.bir.utils.isInlineParameter
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

context (WasmBirContext)
class LocalClassesInInlineLambdasLowering : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        getElementsOfClass<BirCall>().forEach { call ->
            val rootCallee = call.target.asElement
            if (rootCallee.isInline) {
                val inlineLambdas = mutableListOf<BirFunction>()
                for ((arg, param) in call.valueArguments zip rootCallee.valueParameters) {
                    if (arg is BirFunctionExpression && param.isInlineParameter()) {
                        inlineLambdas += arg.function
                    }
                }

                val localClasses = mutableSetOf<BirClass>()
                var hasLocalAnyLocalFunction = false
                val adaptedFunctions = mutableSetOf<BirSimpleFunction>()
                for (lambda in inlineLambdas) {
                    lambda.traverseStackBased(false) { element ->
                        when (element) {
                            is BirClass -> {
                                localClasses += element
                                element.replaceWith(BirCompositeImpl(element.sourceSpan, birBuiltIns.unitType, null))
                            }
                            is BirFunctionExpression -> element.function.walkIntoChildren()
                            is BirFunction -> {
                                hasLocalAnyLocalFunction = true
                                element.walkIntoChildren()
                            }
                            is BirCall -> {
                                val callee = element.target.asElement
                                if (callee.isInline) {
                                    for ((arg, param) in element.valueArguments zip callee.valueParameters) {
                                        if (arg.isAdaptedFunctionReference() && param.isInlineParameter()) {
                                            adaptedFunctions += (arg as BirBlock).statements.first() as BirSimpleFunction
                                        }
                                    }
                                }
                            }
                            else -> element.walkIntoChildren()
                        }
                    }
                }

                if (localClasses.isNotEmpty() || hasLocalAnyLocalFunction) {
                    val block = BirBlockImpl(call.sourceSpan, call.type, null)
                    call.replaceWith(block)
                    block.statements += call

                    val container = block.ancestors().firstIsInstance<BirDeclaration>()
                    LocalDeclarationsLowering().lower(
                        block,
                        container,
                        container.ancestors(true).firstIsInstance<BirDeclarationHost>(),
                        localClasses,
                        adaptedFunctions
                    )

                    block.statements.addAll(0, localClasses)
                }
            }
        }
    }
}