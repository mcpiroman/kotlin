/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.wasm

import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.backend.BirBackendContext
import org.jetbrains.kotlin.bir.backend.InnerClassesSupport
import org.jetbrains.kotlin.bir.backend.compilationException
import org.jetbrains.kotlin.bir.builders.build
import org.jetbrains.kotlin.bir.builders.copyFlagsFrom
import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.declarations.BirConstructor
import org.jetbrains.kotlin.bir.declarations.BirField
import org.jetbrains.kotlin.bir.declarations.BirValueParameter
import org.jetbrains.kotlin.bir.utils.copyTo
import org.jetbrains.kotlin.bir.utils.copyTypeParametersFrom
import org.jetbrains.kotlin.bir.utils.defaultType
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.backend.js.utils.Namer
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

context (BirBackendContext)
class JsInnerClassesSupport : InnerClassesSupport {
    override fun getOuterThisField(innerClass: BirClass): BirField =
        if (!innerClass.isInner) {
            compilationException(
                "Class is not inner",
                innerClass
            )
        } else {
            innerClass.getOrPutAuxData(OuterThisFieldSymbolToken) {
                val outerClass = innerClass.parent as? BirClass
                    ?: compilationException(
                        "No containing class for inner class",
                        innerClass
                    )

                BirField.build {
                    origin = IrDeclarationOrigin.FIELD_FOR_OUTER_THIS
                    name = Name.identifier(Namer.SYNTHETIC_RECEIVER_NAME)
                    type = outerClass.defaultType
                    visibility = DescriptorVisibilities.PROTECTED
                    isFinal = true
                    isExternal = false
                    isStatic = false
                }
            }
        }

    override fun getInnerClassConstructorWithOuterThisParameter(innerClassConstructor: BirConstructor): BirConstructor {
        val innerClass = innerClassConstructor.parent as BirClass
        assert(innerClass.isInner) { "Class is not inner: $innerClass" }

        return innerClassConstructor.getOrPutAuxData(InnerClassConstructorToken) {
            createInnerClassConstructorWithOuterThisParameter(innerClassConstructor)
        }.also {
            if (innerClassConstructor.isPrimary) {
                innerClass[OriginalInnerClassPrimaryConstructorToken] = innerClassConstructor
            }
        }
    }

    override fun getInnerClassOriginalPrimaryConstructorOrNull(innerClass: BirClass): BirConstructor? {
        assert(innerClass.isInner) { "Class is not inner: $innerClass" }
        return innerClass[OriginalInnerClassPrimaryConstructorToken]
    }

    private fun createInnerClassConstructorWithOuterThisParameter(oldConstructor: BirConstructor): BirConstructor {
        val irClass = oldConstructor.parent as BirClass
        val outerThisType = (irClass.parent as BirClass).defaultType

        val newConstructor = BirConstructor.build {
            copyFlagsFrom(oldConstructor)
            origin = oldConstructor.origin
            visibility = oldConstructor.visibility
            returnType = oldConstructor.returnType
            annotations = oldConstructor.annotations
        }

        newConstructor.copyTypeParametersFrom(oldConstructor)

        newConstructor.valueParameters += BirValueParameter.build {
            origin = JsIrBuilder.SYNTHESIZED_DECLARATION
            name = Name.identifier(Namer.OUTER_NAME)
            type = outerThisType
        }

        for (p in oldConstructor.valueParameters) {
            newConstructor.valueParameters += p.copyTo(newConstructor)
        }

        return newConstructor
    }

    companion object {
        private val OuterThisFieldSymbolKey = BirElementAuxStorageKey<BirClass, BirField>()
        private val OuterThisFieldSymbolToken = GlobalBirElementAuxStorageTokens.manager.registerToken(OuterThisFieldSymbolKey)

        private val InnerClassConstructorKey = BirElementAuxStorageKey<BirConstructor, BirConstructor>()
        private val InnerClassConstructorToken = GlobalBirElementAuxStorageTokens.manager.registerToken(InnerClassConstructorKey)

        private val OriginalInnerClassPrimaryConstructorKey = BirElementAuxStorageKey<BirClass, BirConstructor>()
        private val OriginalInnerClassPrimaryConstructorToken =
            GlobalBirElementAuxStorageTokens.manager.registerToken(OriginalInnerClassPrimaryConstructorKey)
    }
}
