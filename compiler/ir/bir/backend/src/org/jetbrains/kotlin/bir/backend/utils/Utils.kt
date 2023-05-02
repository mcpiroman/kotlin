/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.utils

import org.jetbrains.kotlin.bir.backend.BirBackendContext
import org.jetbrains.kotlin.bir.expressions.BirCall
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin


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
    valueArguments += arg
}