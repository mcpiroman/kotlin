/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept

class BirChildElementList<E : BirElement>(
    internal val parent: BirElementBase,
) : BirElementBaseOrList(), MutableCollection<E> {
    override var size: Int = 0
        private set

    // if tail == null -> next
    // if tail != null -> head
    private var headOrNext: BirElementBase? = null
    private var tail: BirElementBase? = null

    override fun isEmpty() = tail == null

    override fun contains(element: E) = (element as? BirElementBase)?.rawParent === this

    override fun containsAll(elements: Collection<E>) = elements.all { it in this }

    override fun add(element: E): Boolean {
        element as BirElementBase
        element.checkCanBoundToTree()

        val tail = tail
        if (tail == null) {
            element.next = headOrNext
            this.headOrNext = element

            linkPreviousSiblingToNewHead(element)
        } else {
            element.next = tail.next
            tail.next = element
        }

        this.tail = element
        element.rawParent = this
        size++
        return true
    }

    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach {
            add(it)
        }
        return true
    }

    private fun linkPreviousSiblingToNewHead(newHead: BirElementBase?) {
        parent.setNextAfterNewChildSetSlow(newHead, this)
    }

    fun replace(old: E, new: E): Boolean = replace(old, new, null)

    fun replace(old: E, new: E, hintPreviousElement: BirElementBase?): Boolean {
        if (old !in this) return false
        old as BirElementBase

        new as BirElementBase
        new.checkCanBoundToTree()

        val tail = tail!!
        val previous = findPreviousNode(tail, old, hintPreviousElement)
        if (previous == null) {
            new.next = headOrNext!!.next
            headOrNext = new
            linkPreviousSiblingToNewHead(new)
        } else {
            new.next = old.next
            previous.next = new
        }

        if (old === tail) {
            this.tail = new
        }

        new.rawParent = this
        old.rawParent = null
        old.next = null
        return true
    }


    override fun remove(element: E): Boolean = remove(element, null)

    fun remove(element: E, hintPreviousElement: BirElementBase?): Boolean {
        if (element !in this) return false
        element as BirElementBase

        val tail = tail!!
        val previous = findPreviousNode(tail, element, hintPreviousElement)
        if (previous == null) {
            headOrNext = element.next
            linkPreviousSiblingToNewHead(headOrNext)
        } else {
            previous.next = element.next
        }

        if (element === tail) {
            this.tail = previous
        }

        element.rawParent = null
        element.next = null
        size--
        return true
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

    override fun removeAll(elements: Collection<E>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        headOrNext = tail?.next
        tail = null
        size = 0
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

    override fun iterator(): MutableIterator<E> = headOrNext?.let { Iterator(this, it, tail!!) } ?: EmptyIterator as MutableIterator<E>

    // Works only for non-empty collection
    private class Iterator<E : BirElement>(
        private val list: BirChildElementList<E>,
        head: BirElementBase,
        private val tail: BirElementBase,
    ) : MutableIterator<E> {
        private var last: BirElementBase? = null
        private var next = head

        override fun hasNext() = last !== tail

        override fun next(): E {
            val n = next
            last = n
            next = n.next!!
            return n as E
        }

        override fun remove() {
            list.remove(last as E) // todo: supply hint previous element
        }
    }
}

private object EmptyIterator : MutableIterator<Any> {
    override fun hasNext(): Boolean = false
    override fun next(): Nothing = throw NoSuchElementException()
    override fun remove(): Nothing = throw NoSuchElementException()
}
