/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.utils.constTrue
import org.jetbrains.kotlin.bir.backend.utils.setEquals
import org.jetbrains.kotlin.bir.backend.utils.setNot
import org.jetbrains.kotlin.bir.backend.utils.string
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.builders.*
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.expressions.impl.*
import org.jetbrains.kotlin.bir.replaceWith
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.types.utils.isPrimitiveType
import org.jetbrains.kotlin.bir.types.utils.makeNullable
import org.jetbrains.kotlin.bir.utils.render
import org.jetbrains.kotlin.bir.utils.resolveFakeOverride
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

context(WasmBirContext)
class LateinitLowering : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        transformLateinitProperties()
        transformLateinitVariables()
        transformIsLateinitInitialized()
    }

    private val lateinitPropertiesKey = registerElementsWithFeatureCacheKey<BirProperty>(false) {
        it.isLateinit && !it.isFakeOverride
    }

    private fun transformLateinitProperties() {
        getElementsWithFeature(lateinitPropertiesKey).forEach { property ->
            property.backingField!!.let {
                it.type = it.type.makeNullable()
            }
            transformLateinitPropertyGetter(property.getter!!, property.backingField!!)
        }
    }

    private val lateinitVariableKey = registerElementsWithFeatureCacheKey<BirVariable>(false) {
        it.isLateinit
    }

    private fun transformLateinitVariables() {
        getElementsWithFeature(lateinitVariableKey).forEach { variable ->
            variable.type = variable.type.makeNullable()
            variable.initializer = BirConst.constNull(variable.sourceSpan, birBuiltIns.nothingNType)
            variable.referencedBy.forEach {
                if (it is BirGetValue && it.target == variable) {
                    transformGetLateinitVariable(it, variable)
                }
            }

            variable.isLateinit = false
        }
    }

    private val lateinitInitializerKey = registerElementsWithFeatureCacheKey<BirCall>(false) {
        it.target == birBuiltIns.lateinitIsInitialized
    }

    private fun transformIsLateinitInitialized() {
        getElementsWithFeature(lateinitInitializerKey).forEach { call ->
            transformCallToLateinitIsInitializedPropertyGetter(call)
        }
    }

    private fun transformLateinitPropertyGetter(getter: BirFunction, backingField: BirField) {
        val type = backingField.type
        assert(!type.isPrimitiveType()) { "'lateinit' modifier is not allowed on primitive types" }
        getter.body = BirBlockBody.build {
            val resultVar = BirVariable.build {
                setTemporary()
                this.type = backingField.type
                initializer = BirGetFieldImpl(
                    getter.sourceSpan,
                    backingField.type,
                    backingField,
                    null,
                    getter.dispatchReceiverParameter?.let {
                        BirGetValueImpl(getter.sourceSpan, it.type, it, null)
                    },
                    null
                )
            }
            statements += resultVar
            val throwIfNull = BirWhenImpl(getter.sourceSpan, birBuiltIns.nothingType, null)
            throwIfNull.addIfThenElse(
                {
                    BirBranchImpl(
                        getter.sourceSpan,
                        BirCall.build {
                            sourceSpan = getter.sourceSpan
                            setNot(
                                BirCall.build {
                                    sourceSpan = getter.sourceSpan
                                    setEquals(
                                        BirGetValueImpl(getter.sourceSpan, resultVar.type, resultVar, null),
                                        BirConst.constNull(getter.sourceSpan, birBuiltIns.nothingNType),
                                        origin = IrStatementOrigin.EXCLEQ
                                    )
                                }
                            )
                        },
                        BirReturnImpl(
                            getter.sourceSpan,
                            birBuiltIns.nothingType,
                            BirGetValueImpl(getter.sourceSpan, resultVar.type, resultVar, null),
                            getter
                        )
                    )
                }, {
                    BirElseBranchImpl(
                        getter.sourceSpan,
                        BirConst.constTrue(),
                        throwUninitializedPropertyAccessException(backingField.name.asString())
                    )
                }
            )
            statements += throwIfNull
        }
    }

    private fun transformGetLateinitVariable(expression: BirGetValue, variable: BirVariable) {
        val newGet = BirWhenImpl(expression.sourceSpan, expression.type, null)
        newGet.addIfThenElse(
            {
                BirBranchImpl(
                    expression.sourceSpan,
                    BirCall.build {
                        sourceSpan = expression.sourceSpan
                        setEquals(
                            BirGetValueImpl(expression.sourceSpan, variable.type, variable, null),
                            BirConst.constNull(expression.sourceSpan, birBuiltIns.nothingNType),
                        )
                    },
                    throwUninitializedPropertyAccessException(variable.name.asString())
                )
            }, {
                BirElseBranchImpl(
                    expression.sourceSpan,
                    BirConst.constTrue(),
                    BirGetValueImpl(expression.sourceSpan, variable.type, variable, null),
                )
            }
        )
        expression.replaceWith(newGet)
    }

    private fun throwUninitializedPropertyAccessException(name: String): BirCall {
        return BirCall.build {
            setCall(wasmSymbols.throwUninitializedPropertyAccessException)
            valueArguments += BirConst.string(name)
        }
    }

    private fun transformCallToLateinitIsInitializedPropertyGetter(call: BirCall) {
        val new = call.extensionReceiver!!.replaceTailExpression {
            require(it is BirPropertyReference) { "isInitialized cannot be invoked on ${it.render()}" }
            val property = it.getter?.asElement?.resolveFakeOverride()?.correspondingProperty?.asElement
            require(property?.isLateinit == true) { "isInitialized invoked on non-lateinit property ${property?.render()}" }
            val backingField = property?.backingField ?: error("Lateinit property is supposed to have a backing field")
            BirCall.build {
                sourceSpan = it.sourceSpan
                setNot(
                    BirCall.build {
                        sourceSpan = it.sourceSpan
                        setEquals(
                            BirGetFieldImpl(
                                it.sourceSpan,
                                property.backingField!!.type,
                                property.backingField!!,
                                null,
                                it.dispatchReceiver,
                                null
                            ),
                            BirConst.constNull(it.sourceSpan, birBuiltIns.nothingNType),
                        )
                    }
                )
            }
        }
        call.replaceWith(new)
    }

    private fun BirExpression.replaceTailExpression(transform: (BirExpression) -> BirExpression): BirExpression {
        var current = this
        var block: BirContainerExpression? = null
        while (current is BirContainerExpression) {
            block = current
            current = current.statements.last() as BirExpression
        }
        current = transform(current)
        if (block == null) {
            return current
        }
        block.statements.setElementAt(block.statements.size - 1, current)
        return this
    }
}