/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.declarations.BirFunction
import org.jetbrains.kotlin.bir.declarations.BirProperty
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.*

abstract class BirTreeContext {
    internal abstract fun elementAttached(element: BirElementBase, prev: BirElementBase?)
    internal abstract fun elementDetached(element: BirElementBase, prev: BirElementBase?)
}

object DummyBirTreeContext : BirTreeContext() {
    override fun elementAttached(element: BirElementBase, prev: BirElementBase?) {}
    override fun elementDetached(element: BirElementBase, prev: BirElementBase?) {}
}

open class GeneralBirTreeContext : BirTreeContext() {
    private var totalElements = 0
    private val elementsByClass = ElementsByClass()
    private var currentElementsOfClassIterator: ElementsOfClassListIterator<*>? = null
    private var currentElementsOfClassIterationIsOdd = false
    private val elementsAddedDuringCurrentElementsOfClassIteration = ArrayList<BirElementBase>(1024)

    private fun checkCacheElementByClass(element: BirElementBase): Boolean {
        return element is BirFunction
                || element is BirProperty
                || element is BirVariable
                || element is BirCall
                || element is BirConstructorCall
                || element is BirFunctionReference
                || element is BirBody
    }

    override internal fun elementAttached(element: BirElementBase, prev: BirElementBase?) {
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
            if (!element.isInClassCache) {
                if (prev != null && prev.javaClass == element.javaClass) {
                    prev.nextElementIsOptimizedFromClassCache = true
                } else {
                    addElementToClassCache(element)
                }
            }

            if (currentElementsOfClassIterator != null) {
                element.attachedDuringByClassIteration = true
                elementsAddedDuringCurrentElementsOfClassIteration += element
            }
        }

        element.registerTrackedBackReferences(null)
        totalElements++
    }

    override fun elementDetached(element: BirElementBase, prev: BirElementBase?) {
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
                assert(!prevNext.isInClassCache)
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
        element.nextElementIsOptimizedFromClassCache = false

        if (prev != null) {
            prev.nextElementIsOptimizedFromClassCache = false
        }

        // Don't eagerly remove element from class cache as it is too slow.
        //  But, when detaching a bigger subtree, maybe we can not find and remove each element individually
        //  but rather scan the list for removed elements / detached elements.
        //  Maybe also formalize and leverage the invariant that sub-elements must appear later than their
        //  ancestor (so start scanning from the index of the root one).

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


    private fun addElementToClassCache(element: BirElementBase) {
        val list = getElementsOfClassList(element.javaClass)
        list.add(element)
        element.isInClassCache = true
    }

    private fun getElementsOfClassList(elementClass: Class<*>): ElementOfClassList {
        return elementsByClass.get(elementClass)
    }

    inline fun <reified E : BirElement> getElementsOfClass(): Iterator<E> = getElementsOfClass(E::class.java)

    fun <E : BirElement> getElementsOfClass(elementClass: Class<E>): Iterator<E> {
        currentElementsOfClassIterator?.let { iterator ->
            cancelElementsOfClassIterator(iterator)
        }

        val list = getElementsOfClassList(elementClass)
            ?: error("Class ${elementClass.simpleName} has not been registered")

        currentElementsOfClassIterationIsOdd = !currentElementsOfClassIterationIsOdd
        val iter = ElementsOfClassListIterator<E>(ArrayList(list.leafClasses))
        currentElementsOfClassIterator = iter
        return iter
    }

    private fun cancelElementsOfClassIterator(iterator: ElementsOfClassListIterator<*>) {
        iterator.cancelled = true
        currentElementsOfClassIterator = null

        elementsAddedDuringCurrentElementsOfClassIteration.forEach {
            it.attachedDuringByClassIteration = false
        }
        elementsAddedDuringCurrentElementsOfClassIteration.clear()
    }

    private inner class ElementOfClassList(
        val elementClass: Class<*>, // todo: doesn't it create a leak wiht ClassValue?
    ) {
        val leafClasses = mutableListOf<ElementOfClassList>()
        var array = arrayOfNulls<BirElementBase>(0)
            private set
        var size = 0
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
            val idx = run {
                var i = 0
                while (i < size) {
                    if (array[i] === element) {
                        return@run i
                    }
                    i++
                }
                -1
            }

            if (idx != -1) {
                val lastIdx = size - 1
                if (idx != lastIdx) {
                    val last = array[lastIdx]!!
                    array[idx] = last
                    currentIterator?.let {
                        if (it.mainListIdx > idx) {
                            it.addAuxElementsToVisit(last)
                        }
                    }
                }
                array[lastIdx] = null
                this.size = size - 1
            }
        }
    }

    private inner class ElementsOfClassListIterator<E : BirElement>(
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
                    break
                }
            }

            if (currentElementsOfClassIterator === this) {
                cancelElementsOfClassIterator(this)
            }

            return false
        }

        private fun checkCancelled() {
            check(!cancelled) { "Iterator is stale - new iteration over elements of given class has begun" }
        }
    }

    private class ElementsOfConcreteClassListIterator<E : BirElementBase>(
        private val list: ElementOfClassList,
    ) : Iterator<E> {
        internal var mainListIdx = 0
            private set
        private var nextSecondary: BirElementBase? = null
        private var auxElementsToVisit: MutableList<BirElementBase>? = null
            set(value) = TODO("Currently unused")
        private var next: BirElementBase? = null

        override fun hasNext(): Boolean {
            if (next != null) return true
            val n = computeNext()
            next = n
            return n != null
        }

        override fun next(): E {
            val n = next
                ?: computeNext()
                ?: throw NoSuchElementException()
            next = null
            return n as E
        }

        private fun computeNext(): BirElementBase? {
            val array = list.array
            while (true) {
                var nextSecondary = nextSecondary
                while (nextSecondary != null && nextSecondary.nextElementIsOptimizedFromClassCache) {
                    nextSecondary = nextSecondary.next!!
                    if (nextSecondary.availableInCurrentIteration()) {
                        this.nextSecondary = nextSecondary
                        return nextSecondary
                    }
                }

                val idx = mainListIdx
                var element: BirElementBase? = null
                while (idx < list.size) {
                    element = array[idx]!!
                    if (element.attachedToTree) {
                        break
                    } else {
                        val lastIdx = list.size - 1
                        if (idx < lastIdx) {
                            array[idx] = array[lastIdx]
                        }
                        array[lastIdx] = null

                        list.size--
                        element.isInClassCache = false
                        element = null
                    }
                }

                if (element != null) {
                    mainListIdx++
                    this.nextSecondary = element
                    if (element.availableInCurrentIteration()) {
                        return element
                    }
                } else {
                    return null
                }
            }
        }

        private fun BirElementBase.availableInCurrentIteration(): Boolean {
            return !attachedDuringByClassIteration
        }

        internal fun addAuxElementsToVisit(element: BirElementBase) {
            auxElementsToVisit?.apply { add(element) } ?: run {
                auxElementsToVisit = mutableListOf(element)
            }
        }
    }

    private inner class ElementsByClass() : ClassValue<ElementOfClassList>() {
        override fun computeValue(elementClass: Class<*>): ElementOfClassList {
            val list = ElementOfClassList(elementClass)

            val ancestorElements = mutableSetOf<ElementOfClassList>()
            fun visitParents(clazz: Class<*>) {
                if (clazz !== elementClass) {
                    if (!BirElement::class.java.isAssignableFrom(clazz)) {
                        return
                    }

                    val ancestor = get(clazz)
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

            return list
        }
    }
}