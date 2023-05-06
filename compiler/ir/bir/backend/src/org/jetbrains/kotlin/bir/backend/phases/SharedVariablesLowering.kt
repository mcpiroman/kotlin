/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.BirDeclarationHost
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.replaceWith
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.bir.utils.ancestors
import org.jetbrains.kotlin.bir.utils.isInlineParameter
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

context (WasmBirContext)
class SharedVariablesLowering : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        val transformedSharedVariables = hashSetOf<BirVariable>()
        getElementsOfClass<BirVariable>().forEach { variable ->
            if (variableMayBeShared(variable)) {
                val variableHost = variable.ancestors().firstIsInstanceOrNull<BirDeclarationHost>()
                if (
                    variable.referencedBy.any {
                        variableHost != it.ancestors().firstIsInstanceOrNull<BirDeclarationHost>()
                                && isVariableUsedAsShared(variable, it)
                    }
                ) {
                    rewriteSharedVariable(variable)
                    transformedSharedVariables += variable
                }
            }
        }

        getElementsOfClass<BirCall>().forEach { call ->
            val callee = call.target.asElement
            if (callee.isInline) {
                for ((param, arg) in callee.valueParameters zip call.valueArguments) {
                    if (arg is BirFunctionExpression
                        && param.isInlineParameter()
                        // This is somewhat conservative but simple.
                        // If a user put redundant <crossinline> modifier on a parameter,
                        // may be it's their fault?
                        && !param.isCrossinline
                    ) {
                        arg.function.traverseStackBased { body ->
                            if (body is BirBody) {
                                body.traverseStackBased { element ->
                                    if (element is BirVariable && variableMayBeShared(element)) {
                                        if (element.referencedBy.any { isVariableUsedAsShared(element, it) }) {
                                            if (transformedSharedVariables.add(element)) {
                                                rewriteSharedVariable(element)
                                            }
                                        }
                                    }
                                    if (element !is BirBody) element.walkIntoChildren()
                                }
                            } else body.walkIntoChildren()
                        }
                    }
                }
            }
        }
    }

    private fun variableMayBeShared(variable: BirVariable): Boolean {
        // A val-variable can be initialized from another container (and thus can require shared variable transformation)
        // in case that container is a lambda with a corresponding contract, e.g. with invocation kind EXACTLY_ONCE.
        return variable.isVar || variable.initializer == null
    }

    private fun isVariableUsedAsShared(variable: BirVariable, usedBy: BirElementBase): Boolean =
        if (!variable.isVar) usedBy is BirSetValue && usedBy.target == variable
        else usedBy is BirValueAccessExpression && usedBy.target == variable

    /* private fun hasRealHost(currentHost: BirDeclarationHost, soughtHost: BirDeclarationHost): Boolean {
         return if (currentHost is BirFunction) {
             currentHost.referencedBy.any { ref ->
                 if (ref is BirFunctionExpression && ref.function == currentHost) {
                     (ref.parent as? BirCall)?.let { call ->
                         val callee = call.target.asElement
                         if (callee.isInline && ref in call.valueArguments) {
                             val param = callee.valueParameters.elementAt(call.valueArguments.indexOf(ref))
                             if (param.isInlineParameter()
                                 // This is somewhat conservative but simple.
                                 // If a user put redundant <crossinline> modifier on a parameter,
                                 // may be it's their fault?
                                 && !param.isCrossinline
                             ) {
                                 call.ancestors().firstIsInstanceOrNull<BirDeclarationHost>()?.let {
                                     if (hasRealHost(it, soughtHost)) {
                                         return@any true
                                     }
                                 }
                             }
                         }
                     }
                 }
                 false
             }
         } else {
             currentHost == soughtHost
         }
     }*/

    /*val callee = call.target.asElement
    for ((param, arg) in callee.valueParameters zip call.valueArguments) {
        if (arg is BirFunctionExpression
            && param.isInlineParameter()
            // This is somewhat conservative but simple.
            // If a user put redundant <crossinline> modifier on a parameter,
            // may be it's their fault?
            && !param.isCrossinline
        ) {
            if (visitedCalledFunctions.add(arg.function)) {
                val realParent = if (callee.isInline)
                    call.ancestors().firstIsInstance<BirDeclaration>()
                else
                    arg.function
                searchForSharedVariables(arg.function, realParent)
            }
        }
    }*/

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