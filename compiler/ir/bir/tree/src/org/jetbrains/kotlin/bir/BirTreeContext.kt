/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.traversal.traverseStackBased

class BirTreeContext {
    private val elementsByClass = hashMapOf<Class<*>, MutableList<BirElementBase>>()

    internal fun elementAttached(element: BirElementBase) {
        element.traverseStackBased {
            attachElement(it as BirElementBase)
            it.recurse()
        }
    }

    private fun attachElement(element: BirElementBase) {
        val klass = element.javaClass
        val list = elementsByClass[klass] ?: mutableListOf<BirElementBase>().also {
            elementsByClass[klass] = it
        }
        list.add(element)
    }

    internal fun elementDetached(element: BirElementBase) {
        element.traverseStackBased {
            detachElement(it as BirElementBase)
            it.recurse()
        }
    }

    private fun detachElement(element: BirElementBase) {
        val klass = element.javaClass
        val list = elementsByClass.getValue(klass)
        list.remove(element)
    }
}