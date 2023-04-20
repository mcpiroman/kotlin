/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.traversal

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.BirTreeTraverseScope

class BirTreeStackBasedTraverseScope(
    private val block: BirTreeStackBasedTraverseScope.(node: BirElement) -> Unit,
) : BirTreeTraverseScope() {
    fun BirElement.recurse() {
        this as BirElementBase

        if (!hasChildren) return

        var nextChild = getFirstChild() as BirElementBase?
        while (nextChild != null) {
            lastVisited = current
            current = nextChild
            block(this@BirTreeStackBasedTraverseScope, nextChild)
            nextChild = nextChild.next
        }
    }
}

fun BirElement.traverseStackBased(includeSelf: Boolean = true, block: BirTreeStackBasedTraverseScope.(node: BirElement) -> Unit) {
    this as BirElementBase

    if (!includeSelf && !hasChildren) return

    val scope = BirTreeStackBasedTraverseScope(block)
    if (includeSelf) {
        scope.current = this
        block(scope, this)
    } else {
        with(scope) {
            recurse()
        }
    }
}

class BirTreeStackBasedTraverseScopeWithInnerPtr(
    private val block: BirTreeStackBasedTraverseScopeWithInnerPtr.(node: BirElement) -> Unit,
) : BirTreeTraverseScope() {
    fun BirElement.recurse() {
        this as BirElementBase
        var nextChild = firstChildPtr
        while (nextChild != null) {
            lastVisited = current
            current = nextChild
            block(this@BirTreeStackBasedTraverseScopeWithInnerPtr, nextChild)
            nextChild = nextChild.next
        }
    }
}

fun BirElement.traverseStackBasedWithInnerPtr(
    includeSelf: Boolean = true,
    block: BirTreeStackBasedTraverseScopeWithInnerPtr.(node: BirElement) -> Unit
) {
    this as BirElementBase

    if (!includeSelf && !hasChildren) return

    val scope = BirTreeStackBasedTraverseScopeWithInnerPtr(block)
    if (includeSelf) {
        scope.current = this
        block(scope, this)
    } else {
        with(scope) {
            recurse()
        }
    }
}
