/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package phases

import BirLoweringPhase
import WasmBirContext
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.builders.build
import org.jetbrains.kotlin.bir.builders.string
import org.jetbrains.kotlin.bir.copyTo
import org.jetbrains.kotlin.bir.copyTypeParametersFrom
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.declarations.impl.BirSimpleFunctionImpl
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.name.Name

context(WasmBirContext)
class BirJsCodeCallsLowering : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        module.traverseStackBased { element ->
            element.recurse()
            if (element is BirSimpleFunction) {
                visitFunction(element)
            }
        }

        getElementsOfClass(BirSimpleFunctionImpl::class.java).forEach { function ->
            visitFunction(function)
        }
    }

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

        val jsFunCode = buildString {
            append('(')
            append(function.valueParameters.joinToString { it.name.identifier })
            append(") => ")
            if (!isSingleExpressionJsCode) append("{ ")
            append(jsCode)
            if (!isSingleExpressionJsCode) append(" }")
        }

        if (function.valueParameters.any { it.defaultValue != null }) {
            // Create a separate external function without default arguments
            // and delegate calls to it.
            val externalFun = createExternalJsFunction(
                function.name,
                "_js_code",
                function.returnType,
                jsCode = jsFunCode,
            )
            externalFun.copyTypeParametersFrom(function)
            externalFun.valueParameters += function.valueParameters.map { it.copyTo(externalFun, defaultValue = null) }
            /*function.body = context.createIrBuilder(function.symbol).irBlockBody {
                val call = irCall(externalFun.symbol)
                function.valueParameters.forEachIndexed { index, parameter ->
                    call.putValueArgument(index, irGet(parameter))
                }
                function.typeParameters.forEachIndexed { index, typeParameter ->
                    call.putTypeArgument(index, typeParameter.defaultType)
                }
                +irReturn(call)
            }
            return listOf(function, externalFun)*/
        }
    }

    private fun BirExpression.getJsCode(): String? {
        val call = this as? BirCall ?: return null
        if (call.target != wasmSymbols.jsCode) return null
        @Suppress("UNCHECKED_CAST")
        return (call.valueArguments.first() as IrConst<String>).value
    }
}

context (WasmBirContext)
private fun createExternalJsFunction(
    originalName: Name,
    suffix: String,
    resultType: BirType,
    jsCode: String,
): BirSimpleFunction {
    val res = BirSimpleFunction.build {
        name = Name.identifier(originalName.asStringStripSpecialMarkers() + suffix)
        returnType = resultType
        isExternal = true
        annotations = listOf(
            BirConstructorCall.build {
                target = wasmSymbols.jsFunConstructor
                valueArguments += BirConst.string(SourceSpan.UNDEFINED, birBuiltIns.stringType, jsCode)
            }
        )
    }
    return res
}