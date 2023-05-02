/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.builders

import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.declarations.BirConstructor
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

fun BirCall.setCall(target: BirSimpleFunction) {
    this.target = target
    type = target.returnType
}

fun BirConstructorCall.setCall(target: BirConstructor) {
    this.target = target
    type = target.returnType
}

fun BirVariable.setTemporary(nameHint: String? = null) {
    origin = IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
    name = Name.identifier(nameHint ?: "tmp")
}

context(BirTreeContext)
fun BirWhen.addIfThenElse(`if`: () -> BirBranch, `else`: () -> BirElseBranch) {
    branches.add(`if`())
    branches.add(`else`())
}
