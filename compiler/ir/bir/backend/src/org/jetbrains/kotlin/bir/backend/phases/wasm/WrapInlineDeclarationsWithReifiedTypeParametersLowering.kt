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
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.expressions.BirCall
import org.jetbrains.kotlin.bir.expressions.BirFunctionReference
import org.jetbrains.kotlin.bir.expressions.impl.BirBlockBodyImpl
import org.jetbrains.kotlin.bir.expressions.impl.BirBlockImpl
import org.jetbrains.kotlin.bir.expressions.impl.BirGetValueImpl
import org.jetbrains.kotlin.bir.expressions.impl.BirReturnImpl
import org.jetbrains.kotlin.bir.replaceWith
import org.jetbrains.kotlin.bir.types.BirTypeArgument
import org.jetbrains.kotlin.bir.types.BirTypeSubstitutor
import org.jetbrains.kotlin.bir.utils.typeSubstitutionMap
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.runIf

context(WasmBirContext)
class WrapInlineDeclarationsWithReifiedTypeParametersLowering : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        getElementsOfClass<BirFunctionReference>().forEach { funRef ->
            val function = funRef.target as? BirSimpleFunction
            if (function != null && function.isInline && function.typeParameters.any { it.isReified }) {
                wrapFunctionReference(funRef, function)
            }
        }
    }

    private fun wrapFunctionReference(funRef: BirFunctionReference, srcFunction: BirSimpleFunction) {
        val substitutionMap = funRef.typeSubstitutionMap
            .entries
            .map { (key, value) ->
                key to (value as BirTypeArgument)
            }
        val typeSubstitutor = BirTypeSubstitutor(
            substitutionMap.map { it.first },
            substitutionMap.map { it.second },
            birBuiltIns
        )

        val newFunction = BirSimpleFunction.build {
            sourceSpan = SourceSpan.SYNTHETIC
            name = Name.identifier("${srcFunction.name}${"$"}wrap")
            returnType = typeSubstitutor.substitute(srcFunction.returnType)
            visibility = DescriptorVisibilities.LOCAL
            origin = IrDeclarationOrigin.ADAPTER_FOR_CALLABLE_REFERENCE
        }

        val forwardExtensionReceiverAsParam = srcFunction.extensionReceiverParameter?.let { extensionReceiver ->
            runIf(funRef.extensionReceiver == null) {
                newFunction.valueParameters += BirValueParameter.build {
                    name = extensionReceiver.name
                    type = typeSubstitutor.substitute(extensionReceiver.type)
                }
                true
            }
        } ?: false

        srcFunction.valueParameters.forEach {
            newFunction.valueParameters += BirValueParameter.build {
                name = it.name
                type = typeSubstitutor.substitute(it.type)
            }
        }

        newFunction.body = BirBlockBodyImpl(funRef.sourceSpan).also { body ->
            val call = BirCall.build {
                sourceSpan = funRef.sourceSpan
                setCall(srcFunction)

                val (extensionReceiver, forwardedParams) = if (forwardExtensionReceiverAsParam) {
                    val param = newFunction.valueParameters.first()
                    BirGetValueImpl(funRef.sourceSpan, param.type, param, null) to newFunction.valueParameters.drop(1)
                } else {
                    funRef.extensionReceiver to newFunction.valueParameters
                }
                this.extensionReceiver = extensionReceiver
                dispatchReceiver = funRef.dispatchReceiver

                forwardedParams.forEach {
                    valueArguments += BirGetValueImpl(funRef.sourceSpan, it.type, it, null)
                }

                typeArguments = funRef.typeArguments
            }
            body.statements += BirReturnImpl(funRef.sourceSpan, birBuiltIns.nothingType, call, newFunction)
        }

        val block = BirBlockImpl(funRef.sourceSpan, funRef.type, IrStatementOrigin.ADAPTED_FUNCTION_REFERENCE)
        funRef.replaceWith(block)
        block.statements += newFunction
        block.statements += funRef
        funRef.target = newFunction
    }
}