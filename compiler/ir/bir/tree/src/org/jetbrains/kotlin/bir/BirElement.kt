/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.traversal.BirElementVisitor

sealed interface BirElementOrList {
    fun acceptChildren(visitor: BirElementVisitor)
}

interface BirElement : BirElementOrList {
    val parent: BirElement?
    val sourceSpan: SourceSpan
}

operator fun <E : BirElement, T> E.get(token: BirElementAuxStorageToken<E, T>): T? {
    return (this as BirElementBase).getAuxData(token)
}

operator fun <E : BirElement, T> E.set(token: BirElementAuxStorageToken<E, T>, value: T?) {
    (this as BirElementBase).setAuxData(token, value)
}