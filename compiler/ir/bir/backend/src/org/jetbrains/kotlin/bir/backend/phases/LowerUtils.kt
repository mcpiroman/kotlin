/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.backend.common.lower.ConstructorDelegationKind
import org.jetbrains.kotlin.bir.backend.BirBackendContext
import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.declarations.BirConstructor
import org.jetbrains.kotlin.bir.expressions.BirDelegatingConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.bir.types.BirSimpleType
import org.jetbrains.kotlin.bir.types.utils.classifierOrFail
import org.jetbrains.kotlin.bir.utils.isPartialLinkageRuntimeError
import org.jetbrains.kotlin.descriptors.ClassKind

context(BirBackendContext)
fun BirConstructor.determineDelegationKind(): ConstructorDelegationKind {
    val constructedClass = parent as BirClass
    val superClass = constructedClass.superTypes
        .mapNotNull { it as? BirSimpleType }
        .firstOrNull { (it.classifier as BirClass).run { kind == ClassKind.CLASS || kind == ClassKind.ANNOTATION_CLASS || kind == ClassKind.ENUM_CLASS } }
        ?: birBuiltIns.anyType
    var callsSuper = false
    var numberOfDelegatingCalls = 0
    var hasPartialLinkageError = false
    traverseStackBased { element ->
        // Skip nested
        if (element !is BirClass) {
            element.walkIntoChildren()
        }

        if (element is BirDelegatingConstructorCall) {
            numberOfDelegatingCalls++
            val delegatingClass = element.target.asElement.parent as BirClass
            // TODO: figure out why Lazy IR multiplies Declarations for descriptors and fix it
            // It happens because of BirBuiltIns whose BirDeclarations are different for runtime and test
            if (delegatingClass == superClass.classifierOrFail)
                callsSuper = true
            else if (delegatingClass != constructedClass)
                throw AssertionError(
                    "Expected either call to another constructor of the class being constructed or" +
                            " call to super class constructor. But was: $delegatingClass with '${delegatingClass.name}' name"
                )
        }

        if (element is BirExpression) {
            hasPartialLinkageError = hasPartialLinkageError || element.isPartialLinkageRuntimeError()
        }
    }

    val delegationKind: ConstructorDelegationKind? = when (numberOfDelegatingCalls) {
        0 -> if (hasPartialLinkageError) ConstructorDelegationKind.PARTIAL_LINKAGE_ERROR else null
        1 -> if (callsSuper) ConstructorDelegationKind.CALLS_SUPER else ConstructorDelegationKind.CALLS_THIS
        else -> null
    }

    if (delegationKind != null)
        return delegationKind
    else
        throw AssertionError("Expected exactly one delegating constructor call but $numberOfDelegatingCalls encountered: ${this}")
}