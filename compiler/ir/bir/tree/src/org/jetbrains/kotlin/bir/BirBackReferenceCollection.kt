/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

@JvmInline
value class BirBackReferenceCollectionLinkedListStyle(
    val first: BirElementBase?
) : Iterable<BirElementBase> {
    constructor() : this(null)

    override fun iterator(): Iterator<BirElementBase> {
        TODO("Not yet implemented")
    }

    private class Iter(
        private var next: BirElementBase?
    ) : Iterator<BirElementBase> {
        override fun hasNext(): Boolean = next != null

        override fun next(): BirElementBase {
            val n = next!!
            next = TODO()
            return n
        }
    }
}

// Alternatively grow more aggressively (maybe exponentially) and find last element with binary search
@Suppress("UNCHECKED_CAST")
@JvmInline
value class BirBackReferenceCollectionArrayStyle(
    private val elementsOrSingle: Any? // Array<BirElementBase?> | BirElementBase | null
) : Iterable<BirElementBase> {
    constructor() : this(null)

    internal fun add(referencingElement: BirElementBase): BirBackReferenceCollectionArrayStyle {
        var elementsOrSingle = elementsOrSingle
        when (elementsOrSingle) {
            null -> return BirBackReferenceCollectionArrayStyle(referencingElement)
            is BirElementBase -> {
                val elements = arrayOfNulls<BirElementBase>(RESIZE_GRADUALITY)
                elements[0] = elementsOrSingle
                elements[1] = referencingElement
                return BirBackReferenceCollectionArrayStyle(elements)
            }
            else -> {
                elementsOrSingle as Array<BirElementBase?>

                val newIndex = elementsOrSingle.indexOfLast { it != null } + 1
                if (newIndex == elementsOrSingle.size) {
                    elementsOrSingle = elementsOrSingle.copyOf(elementsOrSingle.size + RESIZE_GRADUALITY)
                }
                elementsOrSingle[newIndex] = referencingElement

                return BirBackReferenceCollectionArrayStyle(elementsOrSingle)
            }
        }
    }

    internal fun remove(referencingElement: BirElementBase): BirBackReferenceCollectionArrayStyle? {
        val elementsOrSingle = elementsOrSingle
            ?: return null

        if (elementsOrSingle is BirElementBase) {
            return if (elementsOrSingle === referencingElement) {
                BirBackReferenceCollectionArrayStyle(null)
            } else {
                null
            }
        } else {
            elementsOrSingle as Array<BirElementBase?>

            var found = false
            var elIdx = 0
            while (elIdx < elementsOrSingle.size) {
                if (elementsOrSingle[elIdx] === referencingElement) {
                    found = true
                    break
                }
                elIdx++
            }

            if (!found) return null

            if (elementsOrSingle.size == 2) {
                return BirBackReferenceCollectionArrayStyle(
                    if (elIdx == 0) elementsOrSingle[1] else elementsOrSingle[0]
                )
            }

            val lastIdx = elementsOrSingle.indexOfLast { it != null }
            assert(lastIdx != -1)
            if (lastIdx != elIdx) {
                elementsOrSingle[elIdx] = elementsOrSingle[lastIdx]
            }

            val trailingGap = elementsOrSingle.size - lastIdx - 1
            if (trailingGap > RESIZE_GRADUALITY - 1) {
                return BirBackReferenceCollectionArrayStyle(
                    elementsOrSingle.copyOf(lastIdx / RESIZE_GRADUALITY * RESIZE_GRADUALITY)
                )
            } else {
                elementsOrSingle[lastIdx] = null
                return this
            }
        }
    }

    override fun iterator(): Iterator<BirElementBase> =
        when (val elementsOrSingle = elementsOrSingle) {
            null -> EmptyIterator as Iterator<BirElementBase>
            is BirElementBase -> SingleElementIterator(elementsOrSingle)
            else -> CompactingIter(elementsOrSingle as Array<BirElementBase?>)
        }

    companion object {
        private const val RESIZE_GRADUALITY = 4 // Must be at least 2, preferably power of 2
    }

    private class CompactingIter(
        private val elements: Array<BirElementBase?>
    ) : Iterator<BirElementBase> {
        private var nextIdx = -1
        private var lastElementIndex = -1

        override fun hasNext(): Boolean {
            if (nextIdx == -1) {
                return findNext()
            }

            return nextIdx != -2
        }

        override fun next(): BirElementBase {
            val elements = elements
            val next = elements[nextIdx]!!
            findNext()
            return next
        }

        private fun findNext(): Boolean {
            val elements = elements
            val i = nextIdx + 1
            if (i < elements.size) {
                while (true) {
                    val el = elements[i] ?: break
                    if (el.attachedToTree) {
                        nextIdx = i
                        return true
                    } else {
                        if (lastElementIndex == -1) {
                            lastElementIndex = elements.indexOfLast { it != null }
                            assert(lastElementIndex != -1)
                        }

                        elements[i] = elements[lastElementIndex]
                        elements[lastElementIndex] = null
                        lastElementIndex--
                    }
                }
            }

            nextIdx = -2
            return false
        }
    }

    // todo: filter out not attached elements
    private class SimpleIter(
        private val elements: Array<BirElementBase?>
    ) : Iterator<BirElementBase> {
        private var i = 0

        override fun hasNext(): Boolean =
            i < elements.size && elements[i] != null

        override fun next(): BirElementBase {
            return elements[i++]!!
        }
    }
}