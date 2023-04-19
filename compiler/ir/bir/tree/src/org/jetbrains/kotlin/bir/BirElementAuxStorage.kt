/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

class BirElementAuxStorageManager {
    private val elementClasses = hashMapOf<Class<*>, ElementClassData>()
    private var totalTokenRegistrations = 0

    fun <E : BirElement, T> registerToken(key: BirElementAuxStorageKey<E, T>): BirElementAuxStorageToken<E, T> {
        val classData = getElementClassData(key.elementClass)

        refreshKeysFromAncestors(classData)
        classData.keys += key
        if (key.index == -1) {
            key.index = classData.keys.size - 1
        }
        totalTokenRegistrations++

        return BirElementAuxStorageToken(this, key)
    }

    private fun getElementClassData(elementClass: Class<*>): ElementClassData {
        elementClasses[elementClass]?.let {
            return it
        }

        val ancestorElements = mutableSetOf<ElementClassData>()
        fun visitParents(clazz: Class<*>) {
            if (clazz !== elementClass) {
                if (!BirElement::class.java.isAssignableFrom(clazz)) {
                    return
                }

                if (!ancestorElements.add(getElementClassData(clazz))) {
                    return
                }
            }

            clazz.superclass?.let {
                visitParents(it)
            }
            clazz.interfaces.forEach {
                visitParents(it)
            }
        }
        visitParents(elementClass)

        val data = ElementClassData(elementClass, ancestorElements.toList())
        elementClasses[elementClass] = data
        return data
    }

    private fun refreshKeysFromAncestors(element: ElementClassData) {
        if (element.lastSeenTotalTokenRegistrations < totalTokenRegistrations) {
            for (ancestor in element.ancestorElements) {
                element.keys += ancestor.keys
            }
            element.lastSeenTotalTokenRegistrations = totalTokenRegistrations
        }
    }

    internal fun getInitialAuxStorageArraySize(elementClass: Class<*>): Int {
        val data = getElementClassData(elementClass)
        refreshKeysFromAncestors(data)
        return data.keys.size
    }

    private class ElementClassData(
        val elementClass: Class<*>,
        val ancestorElements: List<ElementClassData>
    ) {
        val keys = mutableSetOf<BirElementAuxStorageKey<*, *>>()
        var lastSeenTotalTokenRegistrations = 0

        override fun toString() = elementClass.simpleName
    }
}

class BirElementAuxStorageKey<E : BirElement, T>(
    internal val elementClass: Class<E>
) {
    internal var index = -1
}

inline fun <reified E : BirElement, T> BirElementAuxStorageKey() = BirElementAuxStorageKey<E, T>(E::class.java)

class BirElementAuxStorageToken<E : BirElement, T> internal constructor(
    internal val manager: BirElementAuxStorageManager,
    val key: BirElementAuxStorageKey<E, T>
)