/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.traversal

import org.jetbrains.kotlin.bir.*

class BirTreeStackBasedTraverseScope(
    private val block: BirTreeStackBasedTraverseScope.(node: BirElement) -> Unit,
) : BirTreeTraverseScope() {
    fun BirElement.recurse() {
        this as BirElementBase

        if (!hasChildren) return

        current = getFirstChild() as BirElementBase?
        while (true) {
            val next = current ?: break
            lastVisited = next
            current = next.next
            block(this@BirTreeStackBasedTraverseScope, next)
        }
    }

    context(BirTreeContext)
    override fun BirElement.replace(new: BirElement?) {
        if (this@replace === current) {
            current = this@replace.next
        }
        replace(new, lastVisited)
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

        current = firstChildPtr
        while (true) {
            val next = current ?: break
            lastVisited = next
            current = next.next
            block(this@BirTreeStackBasedTraverseScopeWithInnerPtr, next)
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
