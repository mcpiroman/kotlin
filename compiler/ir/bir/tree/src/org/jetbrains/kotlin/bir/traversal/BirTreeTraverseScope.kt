/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

abstract class BirTreeTraverseScope {
    internal var current: BirElementBase? = null
    internal var lastVisited: BirElementBase? = null

    context (BirTreeContext)
    open fun BirElement.replace(new: BirElement?) = replace(new, lastVisited)

    context (BirTreeContext)
    fun BirElement.remove() = replace(null)
}