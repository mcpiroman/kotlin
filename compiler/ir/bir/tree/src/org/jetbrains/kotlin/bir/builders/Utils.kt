/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.builders

import org.jetbrains.kotlin.bir.declarations.BirConstructor
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.expressions.BirCall
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall

fun BirCall.setCall(target: BirSimpleFunction) {
    this.target = target
    this.type = target.returnType
}

fun BirConstructorCall.setCall(target: BirConstructor) {
    this.target = target
    this.type = target.returnType
}
