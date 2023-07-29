/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept

class BirChildElementList<E : BirElement>(
    internal val parent: BirElementBase,
    id: Int,
) : BirElementBaseOrList(), Collection<E> {
    init {
        assert(id > 0)
    }

    private var sizeAndId: Int = id shl (32 - ID_BITS)

    // if tail == null -> next
    // if tail != null -> head
    private var headOrNext: BirElementBase? = null
    private var tail: BirElementBase? = null

    override var size: Int
        get() = sizeAndId and SIZE_MASK
        private set(value) {
            sizeAndId = value or (sizeAndId and ID_MASK)
        }

    internal val id: Int
        get() = sizeAndId.toInt() shr (32 - ID_BITS)

    override fun isEmpty() = tail == null

    override fun contains(element: E) =
        (element as? BirElementBase)?.parent === parent
                && element.containingListId == id

    override fun containsAll(elements: Collection<E>) = elements.all { it in this }

    fun add(element: E): Boolean {
        return addInternal(element, tail)
    }

    fun add(index: Int, element: E): Boolean {
        if (index < 0 || index > size)
            throw IndexOutOfBoundsException("index: $index, size: $size")

        val prev = findPrevElementForInsert(index)
        return addInternal(element, prev)
    }

    private fun findPrevElementForInsert(index: Int): BirElementBase? {
        var prev: BirElementBase? = null
        if (isNotEmpty()) {
            prev = headOrNext
            var i = index - 1
            while (i > 0) {
                prev = prev?.next
                i--
            }
        }
        return prev
    }

    private fun addInternal(element: E, prev: BirElementBase?): Boolean {
        element as BirElementBase
        element.checkCanBeAttachedAsChild(parent)

        val newPrev: BirElementBase?
        if (prev == null) {
            element.next = headOrNext
            this.headOrNext = element

            newPrev = setupNewHeadElement(element)
        } else {
            newPrev = prev
            element.next = newPrev.next
            newPrev.next = element
        }

        if (prev === tail) {
            tail = element
        }

        element.parent = parent
        element.containingListId = id
        sizeAndId++

        parent.childAttached(element, newPrev)
        return true
    }

    fun addAll(elements: Collection<E>): Boolean {
        elements.forEach {
            add(it)
        }
        return true
    }

    fun addAll(index: Int, elements: Collection<E>): Boolean {
        var prev = findPrevElementForInsert(index)
        elements.forEach {
            addInternal(it, prev)
            prev = it as BirElementBase
        }
        return true
    }

    private fun setupNewHeadElement(newHead: BirElementBase?): BirElementBase? {
        return parent.setNextAfterNewChildSetSlow(newHead, this)
    }

    fun replace(old: E, new: E): Boolean = replace(old, new, null)

    fun replace(old: E, new: E, hintPreviousElement: BirElementBase?): Boolean {
        if (old !in this) return false
        old as BirElementBase

        new as BirElementBase
        new.checkCanBeAttachedAsChild(parent)

        val tail = tail!!
        val prevInList = findPreviousNode(tail, old, hintPreviousElement)
        val prev: BirElementBase?
        if (prevInList == null) {
            new.next = headOrNext!!.next
            headOrNext = new
            prev = setupNewHeadElement(new)
        } else {
            new.next = old.next
            prevInList.next = new
            prev = prevInList
        }

        if (old === tail) {
            this.tail = new
        }

        new.parent = parent
        new.containingListId = id
        old.resetAttachment()

        parent.childDetached(old, prev)
        parent.childAttached(new, prev)

        return true
    }


    fun remove(element: E): Boolean = remove(element, null)

    fun remove(element: E, hintPreviousElement: BirElementBase?): Boolean {
        if (element !in this) return false
        element as BirElementBase

        val tail = tail!!
        val prevInList = findPreviousNode(tail, element, hintPreviousElement)
        val prev: BirElementBase?
        if (prevInList == null) {
            headOrNext = element.next
            prev = setupNewHeadElement(headOrNext)
        } else {
            prevInList.next = element.next
            prev = prevInList
        }

        if (element === tail) {
            this.tail = prevInList
        }

        element.resetAttachment()
        sizeAndId--

        parent.childDetached(element, prev)

        return true
    }

    fun clear() {
        val tail = tail
            ?: return

        var prev: BirElementBase? = null
        var element = headOrNext!!
        headOrNext = tail.next
        while (true) {
            val next = element.next
            element.resetAttachment()
            parent.childDetached(element, prev)

            if (element === tail) break

            prev = element
            element = next!!
        }

        this.tail = null
        size = 0
    }

    private fun findPreviousNode(tail: BirElementBase, element: E, hintPreviousElement: BirElementBase?): BirElementBase? {
        var previous: BirElementBase? = null
        if (hintPreviousElement?.next === element) {
            previous = hintPreviousElement
        } else {
            var p: BirElementBase? = null
            var n: BirElementBase = headOrNext!!
            while (true) {
                if (n === element) {
                    previous = p
                    break
                }

                if (n === tail) error("Element not found in the list")

                p = n
                n = n.next!!
            }
        }

        return previous
    }

    override val next: BirElementBase?
        get() = tail.let {
            if (it != null) it.next else headOrNext
        }

    internal fun setNextSibling(newNext: BirElementBase?): Boolean {
        val tail = tail
        if (tail == null) {
            headOrNext = newNext
            return false
        } else {
            tail.next = newNext
            return true
        }
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        if (isEmpty()) return
        var i = headOrNext!!
        while (true) {
            i.accept(visitor)
            if (i === tail) return
            i = i.next!!
        }
    }

    fun first(): E = if (tail != null) headOrNext!! as E else throw NoSuchElementException("Collection is empty.")
    fun firstOrNull(): E? = if (tail != null) headOrNext!! as E else null

    fun last(): E = tail as E? ?: throw NoSuchElementException("Collection is empty.")
    fun lastOrNull(): E? = tail as E?

    fun singleOrNull(): E? = if (size == 1) tail as E else null

    fun indexOf(element: E): Int {
        if (element !in this) {
            return -1
        }

        var e = headOrNext!!
        var index = 0
        while (e !== element) {
            index++
            e = e.next!!
        }
        return index
    }

    fun elementAt(index: Int): E {
        checkIndex(index)

        var e = headOrNext!!
        var i = index
        while (i > 0) {
            e = e.next!!
            i--
        }
        return e as E
    }

    fun setElementAt(index: Int, element: E): E {
        checkIndex(index)

        var old = headOrNext!!
        var last: BirElementBase? = null
        var i = index
        while (i > 0) {
            last = old
            old = old.next!!
            i--
        }

        val replaced = replace(old as E, element, last)
        check(replaced)
        return old
    }

    private fun checkIndex(index: Int) {
        if (index >= size) {
            throw IndexOutOfBoundsException("index: $index, size: $size")
        }
    }


    operator fun plusAssign(element: E) {
        add(element)
    }

    operator fun plusAssign(elements: Iterable<E>) {
        for (el in elements) add(el)
    }

    operator fun plusAssign(elements: Sequence<E>) {
        for (el in elements) add(el)
    }

    operator fun minusAssign(element: E) {
        remove(element)
    }

    operator fun minusAssign(elements: Iterable<E>) {
        for (el in elements) remove(el)
    }

    operator fun minusAssign(elements: Sequence<E>) {
        for (el in elements) remove(el)
    }


    override fun iterator(): Iterator<E> = ReadonlyIterator(this)

    fun mutableIterator(): kotlin.collections.MutableIterator<E> = MutableIterator(this)

    private class ReadonlyIterator<E : BirElement>(
        list: BirChildElementList<E>,
    ) : Iterator<E> {
        private val tail = list.tail
        private var next: BirElementBase? = if (list.isEmpty()) null else list.headOrNext

        override fun hasNext() = next != null

        override fun next(): E {
            val current = next!!
            this.next = if (current === tail) null else current.next!!
            return current as E
        }
    }

    class MutableIterator<E : BirElement>(
        private val list: BirChildElementList<E>,
    ) : kotlin.collections.MutableIterator<E> {
        private val tail = list.tail
        private var last: BirElementBase? = null
        private var current: BirElementBase? = null

        override fun hasNext() = current !== tail // this also works when list is empty

        override fun next(): E {
            var current = current
            if (current == null) {
                current = list.headOrNext
            } else {
                last = current
                current = current.next!!
            }
            this.current = current
            return current as E
        }

        override fun remove() {
            val toRemove = current!!
            if (toRemove !== tail) {
                this.current = last
            }
            list.remove(toRemove as E, last)
        }

        fun replace(new: E) {
            val toReplace = current!!
            if (toReplace !== tail) {
                this.current = new as BirElementBase
            }
            list.replace(toReplace as E, new, last)
        }
    }

    companion object {
        private const val ID_BITS = 3
        private const val SIZE_MASK: Int = (-1 ushr (32 + ID_BITS))
        private const val ID_MASK: Int = (-1 shl (32 - ID_BITS))
    }
}