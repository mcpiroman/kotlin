/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.BirDeclaration
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.bir.utils.isInlineParameter

context (WasmBirContext)
class SharedVariablesLowering : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        val visitedCalledFunctions = hashSetOf<BirSimpleFunction>()
        getElementsOfClass<BirCall>().forEach { call ->
            if (call.target.asElement.isInline) {
                for ((param, arg) in call.target.asElement.valueParameters zip call.valueArguments) {
                    if (arg is BirFunctionExpression
                        && param.isInlineParameter()
                        // This is somewhat conservative but simple.
                        // If a user put redundant <crossinline> modifier on a parameter,
                        // may be it's their fault?
                        && !param.isCrossinline
                    ) {
                        if (visitedCalledFunctions.add(arg.function)) {
                            searchForSharedVariables(arg.function)
                        }
                    }
                }
            }
        }
    }

    private fun searchForSharedVariables(function: BirSimpleFunction) {
        function.traverseStackBased(false) { element ->
            if (element !is BirDeclaration) {
                element.recurse()
            }

            if (element is BirVariable) {
                // A val-variable can be initialized from another container (and thus can require shared variable transformation)
                // in case that container is a lambda with a corresponding contract, e.g. with invocation kind EXACTLY_ONCE.
                if (element.isVar || element.initializer == null) {
                    if (element.referencedBy.any {
                            if (!element.isVar) it is BirSetValue && it.target == element
                            else it is BirValueAccessExpression && it.target == element
                        }
                    ) {
                        rewriteSharedVariable(element)
                    }
                }
            }
        }
    }

    private fun rewriteSharedVariable(variable: BirVariable) {
        val newVariable = sharedVariablesManager.declareSharedVariable(variable)
        val newDeclaration = sharedVariablesManager.defineSharedValue(variable, newVariable)
        // todo: use traversal context for replace
        variable.replace(newVariable)

        variable.referencedBy.forEach { ref ->
            if (ref is BirGetValue && ref.target == variable) {
                ref.replace(sharedVariablesManager.getSharedValue(newVariable, ref))
            }
            if (ref is BirSetValue && ref.target == variable) {
                ref.replace(sharedVariablesManager.setSharedValue(newVariable, ref))
            }
        }
    }
}