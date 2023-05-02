/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.wasm

import org.jetbrains.kotlin.bir.BirStatement
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.backend.SharedVariablesManager
import org.jetbrains.kotlin.bir.builders.build
import org.jetbrains.kotlin.bir.builders.constNull
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.BirConst
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirGetValue
import org.jetbrains.kotlin.bir.expressions.BirSetValue
import org.jetbrains.kotlin.bir.expressions.impl.*
import org.jetbrains.kotlin.bir.types.BirSimpleTypeImpl
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.bir.types.utils.defaultType
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.backend.js.JsLoweredDeclarationOrigin
import org.jetbrains.kotlin.ir.backend.js.JsStatementOrigins
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

context(WasmBirContext)
class WasmSharedVariablesManager(
    val implicitDeclarationsFile: BirPackageFragment
) : SharedVariablesManager {
    override fun declareSharedVariable(originalDeclaration: BirVariable): BirVariable {
        val initializer = originalDeclaration.initializer ?: BirConst.constNull(
            originalDeclaration.sourceSpan,
            birBuiltIns.nothingNType
        )

        val call = BirConstructorCallImpl(
            sourceSpan = initializer.sourceSpan,
            type = closureBoxType,
            target = closureBoxConstructorDeclaration,
            constructorTypeArgumentsCount = closureBoxConstructorDeclaration.typeParameters.size,
            dispatchReceiver = null,
            extensionReceiver = null,
            origin = null,
            source = SourceElement.NO_SOURCE,
            contextReceiversCount = 0,
            typeArguments = emptyList(),
        ).apply {
            valueArguments += initializer
        }

        return BirVariable.build {
            sourceSpan = originalDeclaration.sourceSpan
            origin = originalDeclaration.origin
            name = originalDeclaration.name
            type = call.type
            this.initializer = call
        }
    }

    override fun defineSharedValue(originalDeclaration: BirVariable, sharedVariableDeclaration: BirVariable): BirStatement =
        sharedVariableDeclaration

    override fun getSharedValue(sharedVariable: BirValueDeclaration, originalGet: BirGetValue): BirExpression {
        val getField = BirGetFieldImpl(
            originalGet.sourceSpan,
            closureBoxFieldDeclaration.type,
            closureBoxFieldDeclaration,
            null,
            BirGetValueImpl(
                originalGet.sourceSpan,
                closureBoxType,
                sharedVariable,
                originalGet.origin
            ),
            originalGet.origin
        )

        return BirTypeOperatorCallImpl(
            originalGet.sourceSpan,
            originalGet.type,
            IrTypeOperator.IMPLICIT_CAST,
            getField,
            originalGet.type,
        )
    }

    override fun setSharedValue(sharedVariable: BirValueDeclaration, originalSet: BirSetValue): BirExpression {
        return BirSetFieldImpl(
            originalSet.sourceSpan,
            originalSet.type,
            closureBoxFieldDeclaration,
            null,
            BirGetValueImpl(
                originalSet.sourceSpan,
                closureBoxType,
                sharedVariable,
                originalSet.origin,
            ),
            originalSet.origin,
            originalSet.value,
        )
    }

    private val boxTypeName = "\$closureBox\$"

    private val closureBoxClassDeclaration: BirClass by lazy {
        createClosureBoxClassDeclaration()
    }

    private val closureBoxConstructorDeclaration: BirConstructor by lazy {
        createClosureBoxConstructorDeclaration()
    }

    private val closureBoxFieldDeclaration by lazy {
        closureBoxPropertyDeclaration
    }

    private val closureBoxPropertyDeclaration by lazy {
        createClosureBoxPropertyDeclaration()
    }

    private lateinit var closureBoxType: BirType

    private fun createClosureBoxClassDeclaration(): BirClass {
        val declaration = BirClass.build {
            origin = JsLoweredDeclarationOrigin.JS_CLOSURE_BOX_CLASS_DECLARATION
            name = Name.identifier(boxTypeName)
        }

        // TODO: substitute
        closureBoxType = BirSimpleTypeImpl(declaration, false, emptyList(), emptyList())
        declaration.thisReceiver = BirValueParameter.build {
            name = Name.identifier("_this_")
            type = closureBoxType
        }
        implicitDeclarationsFile.declarations += declaration

        return declaration
    }

    private fun createClosureBoxPropertyDeclaration(): BirField {
        val fieldName = Name.identifier("v")
        return BirField.build {
            origin = IrDeclarationOrigin.FIELD_FOR_OUTER_THIS
            name = fieldName
            type = birBuiltIns.anyNType
            visibility = DescriptorVisibilities.PUBLIC
        }.also {
            closureBoxClassDeclaration.declarations += it
        }
    }

    private fun createClosureBoxConstructorDeclaration(): BirConstructor {
        val declaration = BirConstructor.build {
            origin = JsLoweredDeclarationOrigin.JS_CLOSURE_BOX_CLASS_DECLARATION
            name = SpecialNames.INIT
            returnType = closureBoxClassDeclaration.defaultType
        }

        val parameterDeclaration = createClosureBoxConstructorParameterDeclaration()
        declaration.valueParameters += parameterDeclaration

        val receiver = BirGetValueImpl(
            SourceSpan.UNDEFINED,
            closureBoxClassDeclaration.thisReceiver!!.type,
            closureBoxClassDeclaration.thisReceiver!!,
            JsStatementOrigins.SYNTHESIZED_STATEMENT
        )
        val value =
            BirGetValueImpl(SourceSpan.UNDEFINED, parameterDeclaration.type, parameterDeclaration, JsStatementOrigins.SYNTHESIZED_STATEMENT)

        val setField = BirSetFieldImpl(
            SourceSpan.UNDEFINED,
            birBuiltIns.unitType,
            closureBoxFieldDeclaration,
            null,
            receiver,
            JsStatementOrigins.SYNTHESIZED_STATEMENT,
            value
        )

        declaration.body = BirBlockBodyImpl(SourceSpan.UNDEFINED).also {
            it.statements += setField
        }

        closureBoxClassDeclaration.declarations += declaration
        return declaration
    }

    private fun createClosureBoxConstructorParameterDeclaration(): BirValueParameter {
        return BirValueParameter.build {
            name = Name.identifier("p")
            type = closureBoxPropertyDeclaration.type
            origin = JsIrBuilder.SYNTHESIZED_DECLARATION
        }
    }
}