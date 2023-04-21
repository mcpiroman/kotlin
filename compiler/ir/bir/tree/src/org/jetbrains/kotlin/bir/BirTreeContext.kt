/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

class BirTreeContext {
    private val elementsByClass = hashMapOf<Class<*>, ElementOfClassList>()

    internal fun elementAttached(element: BirElementBase, prev: BirElementBase?) {
        attachElement(element, prev)
        element.traverseTreeFast { descendantElement, descendantPrev ->
            attachElement(descendantElement, descendantPrev)
        }
    }

    private fun attachElement(element: BirElementBase, prev: BirElementBase?) {
        assert(prev == null || prev.next === element)
        element.attachedToTree = true

        if (prev != null && prev.javaClass == element.javaClass) {
            element.inByClassCacheViaNextPtr = true
        } else {
            addElementToClassCache(element)
        }
    }

    internal fun elementDetached(element: BirElementBase, prev: BirElementBase?) {
        assert(!(prev != null && element.inByClassCacheViaNextPtr))

        detachElement(element, prev)
        element.traverseTreeFast { descendantElement, descendantPrev ->
            detachElement(descendantElement, descendantPrev)
        }

        if (prev != null) {
            val prevNext = prev.next
            assert(prevNext !== element) { "prev should have already reassigned next" }
            if (prevNext != null && prevNext.inByClassCacheViaNextPtr && prev.javaClass != prevNext.javaClass) {
                prevNext.inByClassCacheViaNextPtr = false
                addElementToClassCache(prevNext)
            }
        }
    }

    private fun detachElement(element: BirElementBase, prev: BirElementBase?) {
        element.attachedToTree = false

        if (element.inByClassCacheViaNextPtr) {
            element.inByClassCacheViaNextPtr = false
        } else {
            // TODO: when detaching a bigger subtree, maybe don't find and remove each element individually
            //  but rather scan the list for removed elements / detached elements.
            //  Maybe also formalize and leverage the invariant that sub-elements must appear latter than their
            //  ancestor (so start scanning from the index of the root one).
            val klass = element.javaClass
            val list = elementsByClass.getValue(klass)
            list.remove(element)
        }
    }

    private fun BirElementBase.traverseTreeFast(block: (element: BirElementBase, prev: BirElementBase?) -> Unit) {
        if (!hasChildren) return

        var prev: BirElementBase? = null
        var current = getFirstChild() as BirElementBase
        while (true) {
            block(current, prev)
            current.traverseTreeFast(block)
            prev = current
            current = current.next ?: break
        }
    }


    private fun addElementToClassCache(element: BirElementBase) {
        val klass = element.javaClass
        val list = elementsByClass[klass] ?: ElementOfClassList(klass).also {
            elementsByClass[klass] = it
        }
        list.add(element)
    }

    fun <E : BirElementBase> iterateElementsOfClass(klass: Class<E>): Iterator<E> {
        val list = elementsByClass[klass]
            ?: return EmptyIterator as Iterator<E>
        return ElementOfClassListIterator<E>(list)
    }

    private class ElementOfClassList(
        val klass: Class<*>
    ) {
        var array = arrayOfNulls<BirElementBase>(0)
            private set
        var size = 0
            private set

        fun add(element: BirElementBase) {
            var array = array
            val size = size
            if (size == array.size) {
                array = array.copyOf(if (size == 0) 8 else size * 2)
                this.array = array
            }
            array[size] = element
            this.size = size + 1
        }

        fun remove(element: BirElementBase) {
            val array = array
            val size = size
            var i = 0
            while (i < size) {
                if (array[i] === element) {
                    val lastIdx = size - 1
                    if (i != lastIdx) {
                        array[i] = array[lastIdx]
                    }
                    array[lastIdx] = null
                    this.size = size - 1
                    return
                }
                i++
            }
        }
    }

    private class ElementOfClassListIterator<E : BirElementBase>(
        private val list: ElementOfClassList
    ) : Iterator<E> {
        private var mainListIdx = 0
        private var nextSecondary: BirElementBase? = null

        override fun hasNext(): Boolean {
            return mainListIdx < list.size || nextSecondary != null
        }

        override fun next(): E {
            val nextSecondary = nextSecondary
            if (nextSecondary != null) {
                this.nextSecondary = nextSecondary.next?.takeIf { it.inByClassCacheViaNextPtr }
                return nextSecondary as E
            } else {
                val next = list.array[mainListIdx++]!!
                this.nextSecondary = next.next?.takeIf { it.inByClassCacheViaNextPtr }
                return next as E
            }
        }
    }
}