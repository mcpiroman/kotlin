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
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.expressions.impl.BirGetValueImpl
import org.jetbrains.kotlin.bir.expressions.impl.BirReturnImpl
import org.jetbrains.kotlin.bir.symbols.maybeAsElement
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.bir.types.utils.defaultType
import org.jetbrains.kotlin.bir.utils.ancestors
import org.jetbrains.kotlin.bir.utils.copyTo
import org.jetbrains.kotlin.bir.utils.copyTypeParametersFrom
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

context(WasmBirContext)
class JsCodeCallsLowering : BirLoweringPhase() {
    private val callsToJsCodeKey = registerElementsWithFeatureCacheKey<BirCall>(false) {
        it.target == wasmSymbols.jsCode
    }

    override fun invoke(module: BirModuleFragment) {
        getElementsWithFeature(callsToJsCodeKey).forEach { call ->
            call.ancestors().firstIsInstanceOrNull<BirBody>()?.let { body ->
                visitFunction(call, body)
            }
            (call.parent as? BirField)?.let { property ->
                visitField(call, property)
            }
        }
    }

    private fun visitFunction(jsCall: BirCall, body: BirBody) {
        if (body !is BirBlockBody) return
        val function = body.parent as? BirSimpleFunction ?: return
        val statement = body.statements.singleOrNull() ?: return

        val isSingleExpressionJsCode = when (statement) {
            is BirReturn -> {
                if (statement.value != jsCall) return
                true
            }
            is BirCall -> {
                if (statement != jsCall) return
                false
            }
            else -> return
        }

        val jsCode = jsCall.getJsCode()
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
                val newCall = BirCall.build {
                    setCall(externalFun)
                    valueArguments += function.valueParameters.map {
                        BirGetValueImpl(SourceSpan.UNDEFINED, it.type, it, null)
                    }
                    typeArguments = function.typeParameters.map { it.defaultType }
                }
                statements += BirReturnImpl(SourceSpan.UNDEFINED, birBuiltIns.nothingType, newCall, function)
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

    private fun visitField(jsCall: BirCall, field: BirField) {
        val initializer = field.initializer ?: return
        if (initializer.expression != jsCall) return
        val property = field.correspondingProperty?.maybeAsElement ?: return

        val jsCode = jsCall.getJsCode()
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

    private fun BirCall.getJsCode(): String {
        @Suppress("UNCHECKED_CAST")
        return (valueArguments.first() as BirConst<String>).value
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