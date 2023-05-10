/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.utils

import org.jetbrains.kotlin.bir.backend.BirBackendContext
import org.jetbrains.kotlin.bir.declarations.BirDeclarationHost
import org.jetbrains.kotlin.bir.declarations.BirPackageFragment
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.expressions.impl.BirTypeOperatorCallImpl
import org.jetbrains.kotlin.bir.symbols.BirFunctionSymbol
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.bir.types.isNothing
import org.jetbrains.kotlin.bir.types.isUnit
import org.jetbrains.kotlin.bir.utils.ancestors
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull


context(BirBackendContext)
fun BirCall.setEquals(arg1: BirExpression, arg2: BirExpression, origin: IrStatementOrigin = IrStatementOrigin.EQEQ) {
    target = birBuiltIns.eqeqSymbol
    type = birBuiltIns.booleanType
    this.origin = origin
    valueArguments += arg1
    valueArguments += arg2
}

context(BirBackendContext)
fun BirCall.setNot(arg: BirExpression, origin: IrStatementOrigin = IrStatementOrigin.EXCLEQ) {
    target = birBuiltIns.booleanNotSymbol
    type = birBuiltIns.booleanType
    this.origin = origin
    dispatchReceiver = arg
}

fun isTypeOfIntrinsic(symbol: BirFunctionSymbol): Boolean =
    symbol is BirSimpleFunction && symbol.let { function ->
        function.name.asString() == "typeOf" &&
                function.valueParameters.isEmpty() &&
                (function.ancestors()
                    .firstIsInstanceOrNull<BirDeclarationHost>() as? BirPackageFragment)?.fqName == StandardNames.KOTLIN_REFLECT_FQ_NAME
    }


fun BirExpression.implicitCastIfNeededTo(type: BirType) =
    if (type == this.type || this.type.isNothing())
        this
    else
        BirTypeOperatorCallImpl(sourceSpan, type, IrTypeOperator.IMPLICIT_CAST, this, type)


// TODO: support more cases like built-in operator call and so on
context (BirBackendContext)
fun BirExpression?.isPure(
    anyVariable: Boolean,
    checkFields: Boolean = true,
): Boolean {
    if (this == null) return true

    fun BirExpression.isPureImpl(): Boolean {
        return when (this) {
            is BirConst<*> -> true
            is BirGetValue -> {
                if (anyVariable) return true
                val valueDeclaration = target
                if (valueDeclaration is BirVariable) !valueDeclaration.isVar
                else true
            }
            is BirTypeOperatorCall ->
                (
                        operator == IrTypeOperator.INSTANCEOF ||
                                operator == IrTypeOperator.REINTERPRET_CAST ||
                                operator == IrTypeOperator.NOT_INSTANCEOF
                        ) && argument.isPure(anyVariable, checkFields)
            is BirCall -> if (isSideEffectFree(this)) {
                return valueArguments.all {
                    it.isPure(anyVariable, checkFields)
                }
            } else false
            is BirGetObjectValue -> type.isUnit()
            is BirVararg -> elements.all { (it as? BirExpression)?.isPure(anyVariable, checkFields) == true }
            else -> false
        }
    }

    if (isPureImpl()) return true

    if (!checkFields) return false

    if (this is BirGetField) {
        if (!target.asElement.isFinal) {
            if (!anyVariable) {
                return false
            }
        }
        return receiver.isPure(anyVariable)
    }

    return false
}
