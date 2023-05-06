/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.traversal

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementBase

class BirTreeParentBasedTraverseScope(
    private val block: BirTreeParentBasedTraverseScope.(node: BirElement) -> NextWalkStep,
) : BirTreeTraverseScope() {
}

fun BirElement.traverseParentBased(includeSelf: Boolean = true, block: BirTreeParentBasedTraverseScope.(node: BirElement) -> NextWalkStep) {
    this as BirElementBase

    if (!includeSelf && !hasChildren) return

    val scope = BirTreeParentBasedTraverseScope(block)
    var next: BirElementBase? = if (includeSelf) this else breach()

    traversal@ while (true) {
        val current = next ?: break
        val result = block(scope, current)
        scope.lastVisited = current

        if (result == NextWalkStep.EndTraversal) return

        if (result == NextWalkStep.StepInto) {
            if (current.hasChildren) {
                next = current.getFirstChild() as BirElementBase
                continue
            }
        }

        next = current.next
        if (next != null) continue

        var ancestor = current.parent
        while (ancestor !== this && ancestor != null) {
            next = ancestor.next
            if (next != null) continue@traversal
            ancestor = ancestor.parent
        }

        return
    }
}

enum class NextWalkStep {
    StepInto,
    StepOver,
    EndTraversal
}

fun BirElement.traverseParentBasedWithInnerPtr(
    includeSelf: Boolean = true,
    block: BirTreeParentBasedTraverseScope.(node: BirElement) -> NextWalkStep
) {
    this as BirElementBase

    if (!includeSelf && !hasChildren) return

    val scope = BirTreeParentBasedTraverseScope(block)
    var next: BirElementBase? = if (includeSelf) this else breach()

    traversal@ while (true) {
        val current = next ?: break
        val result = block(scope, current)
        scope.lastVisited = current

        if (result == NextWalkStep.EndTraversal) return

        if (result == NextWalkStep.StepInto) {
            next = current.firstChildPtr
            if (next != null) continue
        }

        next = current.next
        if (next != null) continue

        var ancestor = current.parent
        while (ancestor !== this && ancestor != null) {
            next = ancestor.next
            if (next != null) continue@traversal
            ancestor = ancestor.parent
        }

        return
    }
}
