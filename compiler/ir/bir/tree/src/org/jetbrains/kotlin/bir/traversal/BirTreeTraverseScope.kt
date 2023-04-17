/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

abstract class BirTreeTraverseScope {
    internal var current: BirElementBase? = null
    internal var lastVisited: BirElementBase? = null

    fun replaceCurrent(new: BirElement?) {
        val current = current!!
        val owner = current.rawParent
        if (owner is BirChildElementList<*>) {
            owner as BirChildElementList<BirElement>
            current.replaceInsideList(owner, new, lastVisited)
        } else {
            current.replace(new)
        }
    }

    fun removeCurrent() = replaceCurrent(null)
}