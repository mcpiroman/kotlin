/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import java.lang.AutoCloseable

open class BirTreeContext {
    private var totalElements = 0
    private val rootElements = mutableListOf<BirElementBase>()
    private val elementByFeatureCacheSlots = arrayOfNulls<ElementsWithFeatureCacheSlot>(256)
    private val elementByFeatureCacheConditions = arrayOfNulls<ElementFeatureCacheCondition>(elementByFeatureCacheSlots.size)
    private var elementByFeatureCacheSlotCount = 0
    private var registeredElementByFeatureCacheSlotCount = 0
    private var currentElementsWithFeatureIterator: ElementsWithFeatureCacheSlotIterator<*>? = null
    private var currentFeatureCacheSlot = 0
    private var bufferedElementWithInvalidatedFeature: BirElementBase? = null

    internal fun elementAttached(element: BirElementBase, parent: BirElementBase?, prev: BirElementBase?) {
        attachElement(element, parent, prev)
        element.traverseTreeFast { descendantElement, descendantParent, descendantPrev ->
            attachElement(descendantElement, descendantParent, descendantPrev)
        }
    }

    private fun attachElement(element: BirElementBase, parent: BirElementBase?, prev: BirElementBase?) {
        assert(prev == null || prev.next === element)
        element.ownerTreeContext = this
        element.updateLevel(parent)

        addElementToFeatureCache(element)
        element.registerTrackedBackReferences(null)

        totalElements++
    }

    internal fun elementDetached(element: BirElementBase, parent: BirElementBase?, prev: BirElementBase?) {
        detachElement(element, parent, prev)
        element.traverseTreeFast { descendantElement, descendantParent, descendantPrev ->
            detachElement(descendantElement, descendantParent, descendantPrev)
        }
    }

    private fun detachElement(element: BirElementBase, parent: BirElementBase?, prev: BirElementBase?) {
        element.ownerTreeContext = null
        element.updateLevel(parent)
        removeElementFromFeatureCache(element)

        totalElements--
    }

    internal fun attachElementAsRoot(element: BirElementBase) {
        elementAttached(element, null, null)
        rootElements += element
    }

    private fun BirElementBase.traverseTreeFast(block: (element: BirElementBase, parent: BirElementBase?, prev: BirElementBase?) -> Unit) {
        if (!hasChildren) return

        var prev: BirElementBase? = null
        var current = getFirstChild() as BirElementBase
        while (true) {
            block(current, this, prev)
            current.traverseTreeFast(block)
            prev = current
            current = current.next ?: break
        }
    }


    private fun addElementToFeatureCache(element: BirElementBase) {
        val elementByFeatureCacheConditions = elementByFeatureCacheConditions
        var targetSlot: ElementsWithFeatureCacheSlot? = null
        for (i in currentFeatureCacheSlot + 1..<elementByFeatureCacheSlotCount) {
            val condition = elementByFeatureCacheConditions[i]!!
            if (condition.matches(element)) {
                targetSlot = elementByFeatureCacheSlots[i]!!
                break
            }
        }

        if (targetSlot != null) {
            if (element.featureCacheSlotIndex.toInt() != targetSlot.index) {
                removeElementFromFeatureCache(element)
                targetSlot.add(element)
                element.featureCacheSlotIndex = targetSlot.index.toByte()
            }
        } else {
            removeElementFromFeatureCache(element)
        }
    }

    private fun removeElementFromFeatureCache(element: BirElementBase) {
        element.featureCacheSlotIndex = 0

        // Don't eagerly remove element from feature cache as it is too slow.
        //  But, when detaching a bigger subtree, maybe we can not find and remove each element individually
        //  but rather scan the list for removed elements / detached elements.
        //  Maybe also formalize and leverage the invariant that sub-elements must appear later than their
        //  ancestor (so start scanning from the index of the root one).

        /*val slot = elementByFeatureCacheSlots[element.cacheSlotIndex.toInt()]!!
        slot.remove(element)*/
    }

    internal fun elementFeatureInvalidated(element: BirElementBase) {
        if (element !== bufferedElementWithInvalidatedFeature) {
            flushElementsWithInvalidatedFeatureBuffer()
            bufferedElementWithInvalidatedFeature = element
        }
    }

    private fun flushElementsWithInvalidatedFeatureBuffer() {
        bufferedElementWithInvalidatedFeature?.let {
            addElementToFeatureCache(it)
        }
    }

    fun registerFeatureCacheSlot(key: ElementsWithFeatureCacheKey<*>) {
        val i = ++registeredElementByFeatureCacheSlotCount
        val slot = ElementsWithFeatureCacheSlot(i)
        elementByFeatureCacheSlots[i] = slot
        elementByFeatureCacheConditions[i] = key.condition
        key.index = i
    }

    fun reindexElementByFeatureCache() {
        elementByFeatureCacheSlotCount = registeredElementByFeatureCacheSlotCount

        rootElements.retainAll { it.ownerTreeContext == this && it.parent == null }
        for (root in rootElements) {
            addElementToFeatureCache(root)
            root.traverseTreeFast { element, _, _ ->
                addElementToFeatureCache(element)
            }
        }
    }

    fun <E : BirElement> getElementsWithFeature(key: ElementsWithFeatureCacheKey<E>): Iterator<E> {
        val cacheSlotIndex = key.index
        require(cacheSlotIndex == currentFeatureCacheSlot + 1)

        flushElementsWithInvalidatedFeatureBuffer()

        currentElementsWithFeatureIterator?.let { iterator ->
            cancelElementsWithFeatureIterator(iterator)
        }

        currentFeatureCacheSlot++

        val slot = elementByFeatureCacheSlots[cacheSlotIndex]!!

        val iter = ElementsWithFeatureCacheSlotIterator<E>(slot)
        currentElementsWithFeatureIterator = iter
        return iter
    }

    private fun cancelElementsWithFeatureIterator(iterator: ElementsWithFeatureCacheSlotIterator<*>) {
        iterator.close()
        currentElementsWithFeatureIterator = null
    }


    private inner class ElementsWithFeatureCacheSlot(
        val index: Int,
    ) {
        var array = emptyArray<BirElementBase?>()
            private set
        var size = 0
        var currentIterator: ElementsWithFeatureCacheSlotIterator<*>? = null

        fun add(element: BirElementBase) {
            var array = array
            val size = size

            if (array.isEmpty()) {
                for (i in 1..<currentFeatureCacheSlot) {
                    val slot = elementByFeatureCacheSlots[i]!!
                    if (slot.array.size > size) {
                        // Steal a nice, preallocated and nulled-out array from some previous slot.
                        // It won't use it anyway.
                        array = slot.array
                        slot.array = emptyArray<BirElementBase?>()
                        break
                    }
                }

                if (array.isEmpty()) {
                    array = arrayOfNulls(8)
                }

                this.array = array
            } else if (size == array.size) {
                array = array.copyOf(size * 2)
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
                        TODO()
                    }
                }
                array[lastIdx] = null
                this.size = size - 1
            }
        }
    }

    private inner class ElementsWithFeatureCacheSlotIterator<E : BirElement>(
        private val slot: ElementsWithFeatureCacheSlot,
    ) : Iterator<E>, AutoCloseable {
        private var canceled = false
        var mainListIdx = 0
            private set
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
            require(!canceled) { "Iterator was cancelled" }
            val array = slot.array

            while (true) {
                val idx = mainListIdx
                var element: BirElementBase? = null
                while (idx < slot.size) {
                    element = array[idx]!!
                    if (element.featureCacheSlotIndex.toInt() == slot.index) {
                        deregisterElement(array, idx)
                        break
                    } else {
                        val lastIdx = slot.size - 1
                        if (idx < lastIdx) {
                            array[idx] = array[lastIdx]
                        }
                        array[lastIdx] = null

                        slot.size--
                        element = null
                    }
                }

                if (element != null) {
                    mainListIdx++
                    return element
                } else {
                    mainListIdx = 0
                    slot.size = 0
                    return null
                }
            }
        }

        private fun deregisterElement(array: Array<BirElementBase?>, index: Int) {
            val last = array[index]!!
            array[index] = null
            last.featureCacheSlotIndex = 0
            addElementToFeatureCache(last)
        }

        override fun close() {
            for (i in mainListIdx..<slot.size) {
                deregisterElement(slot.array, i)
            }

            slot.size = 0
            canceled = true
        }
    }
}