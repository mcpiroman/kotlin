/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.BirDeclarationHost
import org.jetbrains.kotlin.bir.declarations.BirFunction
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.replaceWith
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.utils.ancestors
import org.jetbrains.kotlin.bir.utils.isInlineParameter
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

context (WasmBirContext)
class SharedVariablesLowering : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        getElementsOfClass<BirVariable>().forEach { variable ->
            if (variable.isVar
                // A val-variable can be initialized from another container (and thus can require shared variable transformation)
                // in case that container is a lambda with a corresponding contract, e.g. with invocation kind EXACTLY_ONCE.
                || variable.initializer == null
            ) {
                val variableHost = variable.findRealHost()

                if (
                    variable.referencedBy.any {
                        variableHost != it.findRealHost()
                                && if (!variable.isVar) it is BirSetValue && it.target == variable
                        else it is BirValueAccessExpression && it.target == variable
                    }
                ) {
                    rewriteSharedVariable(variable)
                }
            }
        }
    }

    private fun BirElement.findRealHost(): BirDeclarationHost {
        val host = ancestors().firstIsInstance<BirDeclarationHost>()
        if (host is BirFunction) {
            (host.parent as? BirFunctionExpression)?.takeIf { it.function == host }?.let { funExpr ->
                (funExpr.parent as? BirCall)?.let { call ->
                    val callee = call.target.asElement
                    if (callee.isInline && funExpr in call.valueArguments) {
                        val param = callee.valueParameters.elementAt(call.valueArguments.indexOf(funExpr))
                        if (param.isInlineParameter()
                            // This is somewhat conservative but simple.
                            // If a user put redundant <crossinline> modifier on a parameter,
                            // may be it's their fault?
                            && !param.isCrossinline
                        ) {
                            return call.findRealHost()
                        }
                    }
                }
            }
        }

        return host
    }

    private fun rewriteSharedVariable(variable: BirVariable) {
        sharedVariablesManager.transformSharedVariable(variable)

        variable.referencedBy.forEach { ref ->
            if (ref is BirGetValue && ref.target == variable) {
                ref.replaceWith(sharedVariablesManager.transformGetSharedValue(variable, ref))
            }
            if (ref is BirSetValue && ref.target == variable) {
                ref.replaceWith(sharedVariablesManager.transformSetSharedValue(variable, ref))
            }
        }
    }
}