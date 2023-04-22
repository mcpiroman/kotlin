/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package phases

import BirLoweringPhase
import WasmBirContext
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.expressions.BirBlockBody
import org.jetbrains.kotlin.bir.expressions.BirCall
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirReturn
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.ir.expressions.IrConst

object BirJsCodeCallsLowering : BirLoweringPhase() {
    context(WasmBirContext)
    override fun invoke(module: BirModuleFragment) {
        module.traverseStackBased { element ->
            element.recurse()
            if (element is BirSimpleFunction) {
                visitFunction(element)
            }
        }
    }

    context(WasmBirContext)
    private fun visitFunction(function: BirSimpleFunction) {
        val body = function.body ?: return
        check(body is BirBlockBody)  // Should be lowered to block body
        val statement = body.statements.singleOrNull() ?: return

        val isSingleExpressionJsCode: Boolean
        val jsCode: String
        when (statement) {
            is BirReturn -> {
                jsCode = statement.value.getJsCode() ?: return
                isSingleExpressionJsCode = true
            }
            is BirCall -> {
                jsCode = statement.getJsCode() ?: return
                isSingleExpressionJsCode = false
            }
            else -> return
        }
    }

    context(WasmBirContext)
    private fun BirExpression.getJsCode(): String? {
        val call = this as? BirCall ?: return null
        //if (call.target != wasmSymbols.jsCode) return null
        @Suppress("UNCHECKED_CAST")
        return (call.valueArguments.first() as IrConst<String>).value
    }
}