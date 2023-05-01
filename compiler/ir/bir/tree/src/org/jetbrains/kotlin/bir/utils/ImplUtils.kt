/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.utils

internal object EmptyIterator : MutableIterator<Any> {
    override fun hasNext(): Boolean = false
    override fun next(): Nothing = throw NoSuchElementException()
    override fun remove(): Nothing = throw NoSuchElementException()
}

internal class SingleElementIterator<T : Any>(
    element: T
) : Iterator<T> {
    private var element: T? = element

    override fun hasNext() = element != null

    override fun next(): T {
        val next = element!!
        element = null
        return next
    }
}