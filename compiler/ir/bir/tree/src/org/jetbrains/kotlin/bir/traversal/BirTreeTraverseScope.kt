/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.traversal

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.replaceWith

abstract class BirTreeTraverseScope {
    internal var lastVisited: BirElement? = null

    context (BirTreeContext)
    open fun BirElement.replaceWith(new: BirElement?) = replaceWith(new, lastVisited as BirElementBase?)

    context (BirTreeContext)
    fun BirElement.remove() = replaceWith(null)
}