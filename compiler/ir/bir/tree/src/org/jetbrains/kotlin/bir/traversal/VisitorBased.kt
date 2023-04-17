/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.traversal

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.BirTreeTraverseScope

abstract class BirElementVisitor : BirTreeTraverseScope() {
    abstract fun visitElement(element: BirElement)

    internal fun doVisit(element: BirElement) {
        current = element as BirElementBase
        visitElement(element)
        lastVisited = element
    }
}

fun BirElement.accept(visitor: BirElementVisitor) {
    visitor.doVisit(this)
}