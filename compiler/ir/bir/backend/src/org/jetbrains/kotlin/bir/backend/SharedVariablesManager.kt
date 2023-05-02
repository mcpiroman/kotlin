/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend

import org.jetbrains.kotlin.bir.BirStatement
import org.jetbrains.kotlin.bir.declarations.BirValueDeclaration
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirGetValue
import org.jetbrains.kotlin.bir.expressions.BirSetValue

interface SharedVariablesManager {
    fun declareSharedVariable(originalDeclaration: BirVariable): BirVariable

    fun defineSharedValue(originalDeclaration: BirVariable, sharedVariableDeclaration: BirVariable): BirStatement

    fun getSharedValue(sharedVariable: BirValueDeclaration, originalGet: BirGetValue): BirExpression

    fun setSharedValue(sharedVariable: BirValueDeclaration, originalSet: BirSetValue): BirExpression
}