/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.traversal

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementBase

class BirTreeStackBasedTraverseScope(
    private val block: BirTreeStackBasedTraverseScope.(node: BirElement) -> Unit,
) : BirTreeTraverseScope() {
    fun BirElement.walkInto() {
        block(this)
    }

    fun BirElement.walkIntoChildren() {
        this as BirElementBase

        if (!hasChildren) return

        var nextChild = getFirstChild() as BirElementBase?
        while (nextChild != null) {
            val current = nextChild
            nextChild = current.next
            block(this@BirTreeStackBasedTraverseScope, current)
            lastVisited = current
        }
    }
}

fun BirElement.traverseStackBased(includeSelf: Boolean = true, block: BirTreeStackBasedTraverseScope.(node: BirElement) -> Unit) {
    this as BirElementBase

    if (!includeSelf && !hasChildren) return

    val scope = BirTreeStackBasedTraverseScope(block)
    if (includeSelf) {
        block(scope, this)
    } else {
        with(scope) {
            walkIntoChildren()
        }
    }
}

class BirTreeStackBasedTraverseScopeWithData<D>(
    private val block: BirTreeStackBasedTraverseScopeWithData<D>.(node: BirElement, data: D) -> Unit,
) : BirTreeTraverseScope() {
    fun BirElement.apply(data: D) {
        block(this@BirTreeStackBasedTraverseScopeWithData, this, data)
    }

    fun BirElement.walkIntoChildren(data: D) {
        this as BirElementBase

        if (!hasChildren) return

        var nextChild = getFirstChild() as BirElementBase?
        while (nextChild != null) {
            val current = nextChild
            nextChild = nextChild.next
            block(this@BirTreeStackBasedTraverseScopeWithData, current, data)
            lastVisited = current
        }
    }
}

fun <D> BirElement.traverseStackBased(
    data: D,
    includeSelf: Boolean = true,
    block: BirTreeStackBasedTraverseScopeWithData<D>.(node: BirElement, data: D) -> Unit
) {
    this as BirElementBase

    if (!includeSelf && !hasChildren) return

    val scope = BirTreeStackBasedTraverseScopeWithData<D>(block)
    if (includeSelf) {
        block(scope, this, data)
    } else {
        with(scope) {
            walkIntoChildren(data)
        }
    }
}

class BirTreeStackBasedTraverseScopeWithInnerPtr(
    private val block: BirTreeStackBasedTraverseScopeWithInnerPtr.(node: BirElement) -> Unit,
) : BirTreeTraverseScope() {
    fun BirElement.walkIntoChildren() {
        this as BirElementBase
        var nextChild = firstChildPtr
        while (nextChild != null) {
            val current = nextChild
            nextChild = nextChild.next
            block(this@BirTreeStackBasedTraverseScopeWithInnerPtr, current)
            lastVisited = current
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
        block(scope, this)
    } else {
        with(scope) {
            walkIntoChildren()
        }
    }
}
