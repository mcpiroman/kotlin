/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases.wasm

import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.builders.build
import org.jetbrains.kotlin.bir.builders.setCall
import org.jetbrains.kotlin.bir.builders.string
import org.jetbrains.kotlin.bir.declarations.BirDeclarationContainer
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.declarations.BirProperty
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.expressions.impl.BirGetValueImpl
import org.jetbrains.kotlin.bir.expressions.impl.BirReturnImpl
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.bir.utils.copyTo
import org.jetbrains.kotlin.bir.utils.copyTypeParametersFrom
import org.jetbrains.kotlin.bir.utils.defaultType
import org.jetbrains.kotlin.name.Name

context(WasmBirContext)
class BirJsCodeCallsLowering : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        getElementsOfClass<BirSimpleFunction>().forEach { function ->
            visitFunction(function)
        }

        getElementsOfClass<BirProperty>().forEach { property ->
            visitProperty(property)
        }
    }

    private fun visitFunction(function: BirSimpleFunction) {
        val body = function.body as? BirBlockBody ?: return
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

            function.body = BirBlockBody.build {
                val call = BirCall.build {
                    setCall(externalFun)
                    valueArguments += function.valueParameters.map {
                        BirGetValueImpl(SourceSpan.UNDEFINED, it.type, it, null)
                    }
                    typeArguments = function.typeParameters.map { it.defaultType }
                }
                statements += BirReturnImpl(SourceSpan.UNDEFINED, externalFun.returnType, call, function)
            }

            (function.parent as BirDeclarationContainer).declarations += externalFun
        } else {
            function.annotations += BirConstructorCall.build {
                setCall(wasmSymbols.jsFunConstructor)
                typeArguments = emptyList()
                valueArguments += BirConst.string(SourceSpan.UNDEFINED, birBuiltIns.stringType, jsFunCode)
            }
            function.body = null
        }
    }

    private fun visitProperty(property: BirProperty) {
        val field = property.backingField ?: return
        val initializer = field.initializer ?: return
        val jsCode = initializer.expression.getJsCode() ?: return
        val externalFun = createExternalJsFunction(
            property.name,
            "_js_code",
            field.type,
            jsCode = "() => ($jsCode)",
        )
        initializer.expression = BirCall.build {
            setCall(externalFun)
        }
        (property.parent as BirDeclarationContainer).declarations += externalFun
    }

    private fun BirExpression.getJsCode(): String? {
        val call = this as? BirCall ?: return null
        if (call.target != wasmSymbols.jsCode) return null
        @Suppress("UNCHECKED_CAST")
        return (call.valueArguments.first() as BirConst<String>).value
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