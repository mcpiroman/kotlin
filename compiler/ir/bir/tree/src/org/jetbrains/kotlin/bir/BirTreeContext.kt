/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.declarations.BirDeclaration
import org.jetbrains.kotlin.bir.expressions.BirCall

open class BirTreeContext {
    private var totalElements = 0
    private val elementsByClass = hashMapOf<Class<*>, ElementOfClassList>()
    private var currentElementsOfClassIterator: ElementsOfClassListIterator<*>? = null
    private var currentElementsOfClassIterationIsOdd = false
    private val elementsAddedDuringCurrentElementsOfClassIteration = ArrayList<BirElementBase>(1024)

    internal fun elementAttached(element: BirElementBase, prev: BirElementBase?) {
        attachElement(element, prev)
        element.traverseTreeFast { descendantElement, descendantPrev ->
            attachElement(descendantElement, descendantPrev)
        }
    }

    private fun attachElement(element: BirElementBase, prev: BirElementBase?) {
        assert(prev == null || prev.next === element)
        element.attachedToTree = true
        element.updateLevel()

        if (checkCacheElementByClass(element)) {
            if (prev != null && prev.javaClass == element.javaClass) {
                prev.nextElementIsOptimizedFromClassCache = true
            } else {
                addElementToClassCache(element)
            }

            if (currentElementsOfClassIterator != null) {
                element.attachedDuringByClassIteration = true
                elementsAddedDuringCurrentElementsOfClassIteration += element
            }
        }

        element.registerTrackedBackReferences(null)
        totalElements++
    }

    internal fun elementDetached(element: BirElementBase, prev: BirElementBase?) {
        assert(!(prev != null && prev.nextElementIsOptimizedFromClassCache))
        val prevNextElementIsOptimizedFromClassCache = prev?.nextElementIsOptimizedFromClassCache == true

        detachElement(element, prev)
        element.traverseTreeFast { descendantElement, descendantPrev ->
            detachElement(descendantElement, descendantPrev)
        }

        if (prev != null) {
            val prevNext = prev.next
            assert(prevNext !== element) { "prev should have already reassigned next" }
            if (prevNext != null && prevNextElementIsOptimizedFromClassCache) {
                if (prevNext.javaClass == prev.javaClass) {
                    prev.nextElementIsOptimizedFromClassCache = true
                } else {
                    assert(!prev.nextElementIsOptimizedFromClassCache)
                    if (checkCacheElementByClass(prevNext)) {
                        addElementToClassCache(prevNext)
                    }
                }
            }
        }
    }

    private fun detachElement(element: BirElementBase, prev: BirElementBase?) {
        element.attachedToTree = false
        element.updateLevel()
        element.attachedDuringByClassIteration = false

        if (prev?.nextElementIsOptimizedFromClassCache == true) {
            prev.nextElementIsOptimizedFromClassCache = false
        } else {
            // TODO: when detaching a bigger subtree, maybe don't find and remove each element individually
            //  but rather scan the list for removed elements / detached elements.
            //  Maybe also formalize and leverage the invariant that sub-elements must appear latter than their
            //  ancestor (so start scanning from the index of the root one).
            val list = getElementsOfClassList(element.javaClass)
            list.remove(element)
        }
        totalElements--
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


    private fun checkCacheElementByClass(element: BirElementBase): Boolean {
        return element is BirDeclaration ||
                element is BirCall
    }

    private fun addElementToClassCache(element: BirElementBase) {
        val list = getElementsOfClassList(element.javaClass)
        list.add(element)
    }

    private fun getElementsOfClassList(elementClass: Class<*>): ElementOfClassList {
        elementsByClass[elementClass]?.let {
            return it
        }

        val list = ElementOfClassList(elementClass)

        val ancestorElements = mutableSetOf<ElementOfClassList>()
        fun visitParents(clazz: Class<*>) {
            if (clazz !== elementClass) {
                if (!BirElement::class.java.isAssignableFrom(clazz)) {
                    return
                }

                val ancestor = getElementsOfClassList(clazz)
                if (ancestorElements.add(ancestor)) {
                    ancestor.leafClasses += list
                } else {
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

        elementsByClass[elementClass] = list
        return list
    }

    inline fun <reified E : BirElement> getElementsOfClass(): Iterator<E> = getElementsOfClass(E::class.java)

    fun <E : BirElement> getElementsOfClass(elementClass: Class<E>): Iterator<E> {
        currentElementsOfClassIterator?.let { iterator ->
            iterator.cancelled = true
            currentElementsOfClassIterator = null

            elementsAddedDuringCurrentElementsOfClassIteration.forEach {
                it.attachedDuringByClassIteration = false
            }
            elementsAddedDuringCurrentElementsOfClassIteration.clear()
        }

        val list = getElementsOfClassList(elementClass)
            ?: error("Class ${elementClass.simpleName} has not been registered")

        currentElementsOfClassIterationIsOdd = !currentElementsOfClassIterationIsOdd
        val iter = ElementsOfClassListIterator<E>(ArrayList(list.leafClasses))
        currentElementsOfClassIterator = iter
        return iter
    }

    private inner class ElementOfClassList(
        val elementClass: Class<*>,
    ) {
        val leafClasses = mutableListOf<ElementOfClassList>()
        var array = arrayOfNulls<BirElementBase>(0)
            private set
        var size = 0
            private set
        var currentIterator: ElementsOfConcreteClassListIterator<*>? = null

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
                        currentIterator?.let {
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

    private class ElementsOfClassListIterator<E : BirElement>(
        private val concreteClassLists: List<ElementOfClassList>,
    ) : Iterator<E> {
        internal var cancelled = false
        val listsIterator = concreteClassLists.iterator()
        var listIterator: ElementsOfConcreteClassListIterator<BirElementBase>? = null

        override fun next(): E {
            return listIterator!!.next() as E
        }

        override fun hasNext(): Boolean {
            checkCancelled()
            if (listIterator?.hasNext() == true)
                return true

            listIterator = null
            while (listIterator == null) {
                if (listsIterator.hasNext()) {
                    val list = listsIterator.next()
                    val nextClassIterator = ElementsOfConcreteClassListIterator<BirElementBase>(list)
                    if (nextClassIterator.hasNext()) {
                        listIterator = nextClassIterator
                        list.currentIterator = nextClassIterator
                        return true
                    }
                } else {
                    return false
                }
            }
            return false
        }

        private fun checkCancelled() {
            check(!cancelled) { "Iterator is stale - new iteration over elements of given class has begun" }
        }
    }

    private class ElementsOfConcreteClassListIterator<E : BirElementBase>(
        private val list: ElementOfClassList,
    ) : AbstractIterator<E>() {
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
                while (nextSecondary != null && nextSecondary.nextElementIsOptimizedFromClassCache) {
                    nextSecondary = nextSecondary.next!!
                    if (nextSecondary.availableInCurrentIteration()) {
                        this.nextSecondary = nextSecondary
                        setNext(nextSecondary as E)
                        return
                    }
                }

                val idx = mainListIdx
                if (idx < list.size) {
                    mainListIdx++
                    val element = list.array[idx]!!
                    this.nextSecondary = element
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

        private fun BirElementBase.availableInCurrentIteration(): Boolean {
            return !attachedDuringByClassIteration
        }

        internal fun addAuxElementsToVisit(element: BirElementBase) {
            auxElementsToVisit?.apply { add(element) } ?: run {
                auxElementsToVisit = mutableListOf(element)
            }
        }
    }
}