/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

open class BirTreeContext {
    private val elementsByClass = hashMapOf<Class<*>, ElementOfClassList>()
    private var currentElementsOfClassIterator: ElementOfClassListIterator<*>? = null
    private var currentElementsOfClassIterationIsOdd = false

    internal fun elementAttached(element: BirElementBase, prev: BirElementBase?) {
        attachElement(element, prev)
        element.traverseTreeFast { descendantElement, descendantPrev ->
            attachElement(descendantElement, descendantPrev)
        }
    }

    private fun attachElement(element: BirElementBase, prev: BirElementBase?) {
        assert(prev == null || prev.next === element)
        element.attachedToTree = true
        element.attachedInOddByClassIteration = currentElementsOfClassIterationIsOdd
        element.updateLevel()

        if (prev != null && prev.javaClass == element.javaClass) {
            element.inByClassCacheViaNextPtr = true
        } else {
            addElementToClassCache(element)
        }

        element.registerTrackedBackReferences(null)
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
        element.updateLevel()

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

    fun <E : BirElementBase> getElementsOfClass(klass: Class<E>): Iterator<E> {
        currentElementsOfClassIterator?.let {
            it.cancelled = true
            currentElementsOfClassIterator = null
        }

        val list = elementsByClass[klass]
            ?: return EmptyIterator as Iterator<E>

        currentElementsOfClassIterationIsOdd = !currentElementsOfClassIterationIsOdd
        val iter = ElementOfClassListIterator<E>(list, currentElementsOfClassIterationIsOdd)
        currentElementsOfClassIterator = iter
        return iter
    }

    private inner class ElementOfClassList(
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
                        val last = array[lastIdx]!!
                        array[i] = last
                        currentElementsOfClassIterator?.let {
                            if (it.mainListIdx > i) {
                                it.addAuxElementsToVisit(last)
                            }
                        }
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
        private val list: ElementOfClassList,
        private val isOddIteration: Boolean,
    ) : AbstractIterator<E>() {
        internal var cancelled = false
        internal var mainListIdx = 0
            private set
        private var nextSecondary: BirElementBase? = null
        private var auxElementsToVisit: MutableList<BirElementBase>? = null

        override fun computeNext() {
            auxElementsToVisit?.let { list ->
                while (true) {
                    val element = list.lastOrNull() ?: break
                    if (element.attachedToTree) {
                        setNext(element as E)
                        return
                    } else {
                        list.removeLast()
                    }
                }
            }

            while (true) {
                var nextSecondary = nextSecondary
                while (nextSecondary != null && nextSecondary.inByClassCacheViaNextPtr) {
                    if (nextSecondary.availableInCurrentIteration()) {
                        setNext(nextSecondary as E)
                        this.nextSecondary = nextSecondary.next
                        return
                    }
                    nextSecondary = nextSecondary.next
                }

                val idx = mainListIdx
                if (idx < list.size) {
                    mainListIdx++
                    val element = list.array[idx]!!
                    this.nextSecondary = element.next
                    if (element.availableInCurrentIteration()) {
                        setNext(element as E)
                        return
                    }
                } else {
                    done()
                    return
                }
            }
        }

        /*override fun hasNext(): Boolean {
            checkCancelled()

            if (mainListIdx < list.size) {
                return true
            }

            if (nextSecondary?.inByClassCacheViaNextPtr == true) {
                return true
            } else {
                nextSecondary = null
            }

            auxElementsToVisit?.let { list ->
                while (true) {
                    val element = list.lastOrNull() ?: break
                    if (element.attachedToTree) {
                        return true
                    } else {
                        list.removeLast()
                    }
                }
            }

            return false
        }

        override fun next(): E {
            checkCancelled()

            val nextSecondary = nextSecondary
            if (nextSecondary != null) {
                this.nextSecondary = nextSecondary.next
                return nextSecondary as E
            } else {
                auxElementsToVisit?.removeLastOrNull()?.let {
                    return it as E
                }

                val next = list.array[mainListIdx++]!!
                this.nextSecondary = next.next
                return next as E
            }
        }*/

        private fun checkCancelled() {
            check(!cancelled) { "Iterator is stale - new iteration over elements of given class has begun" }
        }

        private fun BirElementBase.availableInCurrentIteration(): Boolean {
            return attachedInOddByClassIteration != isOddIteration
        }

        internal fun addAuxElementsToVisit(element: BirElementBase) {
            auxElementsToVisit?.apply { add(element) } ?: run {
                auxElementsToVisit = mutableListOf(element)
            }
        }
    }
}