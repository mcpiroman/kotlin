/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

sealed class BirElementBaseOrList : BirElementOrList {
    internal abstract val next: BirElementBase?
}

abstract class BirElementBase : BirElement, BirElementBaseOrList() {
    //var originalIrElement: IrElement? = null
    final override var parent: BirElementBase? = null
        internal set
    final override var next: BirElementBase? = null
    private var auxStorage: Array<Any?>? = null
    private var levelAndContainingListId: Short = 0
    private var flags: Byte = 0
    private var registeredBackRefs: Byte = 0

    //internal var firstChildPtr: BirElementBase? = null
    internal var firstChildPtr: BirElementBase?
        set(value) {}
        get() = getFirstChild() as BirElementBase?


    private var level: Short
        get() = levelAndContainingListId and LEVEL_MASK
        set(value) {
            levelAndContainingListId = value or (levelAndContainingListId and CONTAINING_LIST_ID_MASK)
        }

    internal var containingListId: Int
        get() = levelAndContainingListId.toInt() shr (16 - CONTAINING_LIST_ID_BITS)
        set(value) {
            levelAndContainingListId = (levelAndContainingListId and LEVEL_MASK) or (value shl (16 - CONTAINING_LIST_ID_BITS)).toShort()
        }

    internal fun getContainingList(): BirChildElementList<*>? {
        val containingListId = containingListId
        return if (containingListId == 0) null
        else parent?.getChildrenListById(containingListId)
    }

    internal var attachedToTree: Boolean
        get() = hasFlag(FLAG_ATTACHED_TO_TREE)
        set(value) = setFlag(FLAG_ATTACHED_TO_TREE, value)

    internal var hasChildren: Boolean
        get() = hasFlag(FLAG_HAS_CHILDREN)
        set(value) = setFlag(FLAG_HAS_CHILDREN, value)

    internal var isInClassCache: Boolean
        get() = hasFlag(FLAG_IS_IN_CLASS_CACHE)
        set(value) = setFlag(FLAG_IS_IN_CLASS_CACHE, value)

    internal var nextElementIsOptimizedFromClassCache: Boolean
        get() = hasFlag(FLAG_NEXT_ELEMENT_IS_OPTIMIZED_FROM_CLASS_CACHE)
        set(value) = setFlag(FLAG_NEXT_ELEMENT_IS_OPTIMIZED_FROM_CLASS_CACHE, value)

    internal var attachedDuringByClassIteration: Boolean
        get() = hasFlag(FLAG_ATTACHED_DURING_BY_CLASS_ITERATION)
        set(value) = setFlag(FLAG_ATTACHED_DURING_BY_CLASS_ITERATION, value)

    private fun hasFlag(flag: Byte): Boolean =
        (flags and flag).toInt() != 0

    private fun setFlag(flag: Byte, value: Boolean) {
        flags = if (value) flags or flag else flags and flag.inv()
    }

    fun isAncestorOf(other: BirElementBase): Boolean {
        if (attachedToTree != other.attachedToTree) {
            return false
        }

        val distance = other.level - level
        if (distance < 0 || (distance == 0 && level != Short.MAX_VALUE)) {
            return false
        }

        var n = other
        repeat(distance.toInt()) {
            n = n.parent ?: return false
            if (n === this) return true
        }

        return false
    }

    internal fun updateLevel(parent: BirElementBase?) {
        level = if (parent != null) {
            val parentLevel = parent.level
            if (parentLevel == Short.MAX_VALUE) Short.MAX_VALUE else (parentLevel + 1).toShort()
        } else 0
    }

    internal open fun getFirstChild(): BirElement? = null
    internal open fun getChildren(children: Array<BirElementOrList?>): Int = 0
    internal open fun getChildrenListById(id: Int): BirChildElementList<*> {
        throwChildrenListWithIdNotFound(id)
    }

    override fun acceptChildren(visitor: BirElementVisitor) = Unit
    context (BirTreeContext)
    internal open fun replaceChildProperty(old: BirElement, new: BirElement?) {
        throwChildForReplacementNotFound(old)
    }

    internal open fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) = Unit
    internal open fun registerTrackedBackReferences(unregisterFrom: BirElementBase?) = Unit

    protected fun throwChildForReplacementNotFound(old: BirElement): Nothing {
        throw IllegalStateException("The child property $old not found in its parent $this")
    }

    protected fun throwChildrenListWithIdNotFound(id: Int): Nothing {
        throw IllegalStateException("The element $this does not have a children list with id $id")
    }

    internal fun checkCanBeAttachedAsChild(newParent: BirElement) {
        require(parent == null) { "Cannot attach element $this as a child of $newParent as it is already a child of $parent." }
    }

    context (BirTreeContext)
    internal fun replaceInsideList(
        list: BirChildElementList<BirElement>,
        new: BirElement?,
        hintPreviousElement: BirElementBase?,
    ) {
        val success = if (new == null) {
            list.remove(this, hintPreviousElement)
        } else {
            list.replace(this, new, hintPreviousElement)
        }

        if (!success) list.parent.throwChildForReplacementNotFound(this)
    }

    fun nextNonChild(): BirElementBase? {
        next?.let { return it }

        var p = parent
        while (p != null) {
            p.next?.let { return it }
            p = p.parent
        }

        return null
    }

    fun breach(): BirElementBase? {
        return getFirstChild() as BirElementBase? ?: nextNonChild()
    }

    protected fun initChildField(
        value: BirElement?,
        prevChildOrList: BirElementOrList?,
    ) {
        if (value != null) {
            value as BirElementBase
            prevChildOrList as BirElementBaseOrList?

            setChildFieldCommon(
                value,
                prevChildOrList,
                null,
                value
            )

            hasChildren = true
            // No need to call [attachChild] as this element is being initialized,
            // so it itself is definitely not yet attached.
        }
    }

    context (BirTreeContext)
    protected fun setChildField(
        old: BirElement?,
        new: BirElement?,
        prevChildOrList: BirElementOrList?,
    ) {
        if (new === old) return

        old as BirElementBase?
        new as BirElementBase?
        prevChildOrList as BirElementBaseOrList?

        val newsNext = if (old != null) old.next
        else if (prevChildOrList != null) prevChildOrList.next
        else firstChildPtr

        val prev = setChildFieldCommon(
            new,
            prevChildOrList,
            newsNext,
            new ?: old?.next
        )

        old?.resetAttachment()

        hasChildren = new != null || newsNext != null

        if (old != null) {
            childDetached(old, prev)
        }

        if (new != null) {
            childAttached(new, prev)
        }
    }

    private fun setChildFieldCommon(
        new: BirElementBase?,
        prevChildOrList: BirElementBaseOrList?,
        next: BirElementBase?,
        prevNext: BirElementBase?,
    ): BirElementBase? {
        if (new != null) {
            new.checkCanBeAttachedAsChild(this)
            new.parent = this
            new.next = next
        }

        val prev = when (prevChildOrList) {
            null -> {
                firstChildPtr = prevNext
                null
            }
            is BirElementBase -> {
                prevChildOrList.next = prevNext
                prevChildOrList
            }
            is BirChildElementList<*> -> {
                prevChildOrList.setNextSibling(prevNext)
                if (prevChildOrList.isNotEmpty()) {
                    prevChildOrList.last() as BirElementBase
                } else {
                    setNextAfterNewChildSetSlow(prevNext, prevChildOrList)
                }
            }
        }

        return prev
    }

    internal fun setNextAfterNewChildSetSlow(
        newNext: BirElementBase?,
        lastListBeforeTheNewElement: BirChildElementList<*>,
    ): BirElementBase? {
        val children = arrayOfNulls<BirElementOrList?>(8)
        val maxChildrenCount = getChildren(children)
        val listIndex = children.indexOf(lastListBeforeTheNewElement)
        assert(listIndex in 0 until maxChildrenCount)
        for (i in listIndex - 1 downTo 0) {
            when (val child = children[i]) {
                null -> {}
                is BirElementBase -> {
                    child.next = newNext
                    return child
                }
                is BirChildElementList<*> -> {
                    child.setNextSibling(newNext)
                    if (child.isNotEmpty()) {
                        return child.last() as BirElementBase
                    }
                }
                else -> error(child.toString())
            }
        }

        firstChildPtr = newNext
        hasChildren = newNext != null
        return null
    }

    internal fun resetAttachment() {
        parent = null
        next = null
        containingListId = 0
    }

    context (BirTreeContext)
    internal fun childAttached(element: BirElementBase, prev: BirElementBase?) {
        if (attachedToTree) {
            elementAttached(element, this, prev)
        }
    }

    context (BirTreeContext)
    internal fun childDetached(element: BirElementBase, prev: BirElementBase?) {
        if (attachedToTree) {
            elementDetached(element, this, prev)
        }
    }


    /*@OptIn(InternalBirApi::class)
    protected fun <T : BirElementBase> setTrackedElementReferenceLinkedListStyle(
        old: BirElementTrackingBackReferences?,
        new: BirElementTrackingBackReferences?,
        nextTrackedElementReferenceProperty: KMutableProperty1<T, BirElementBase?>
    ) {
        if (old === new) return

        if (old != null) {
            var next: T? = old.referencedBy.first as T?
            if (next != null) {
                var last: BirElement? = null
                while (next !== this) {
                    last = next
                    next = nextTrackedElementReferenceProperty.get(next!!) as T?
                }

                val newNext = new as T? ?: nextTrackedElementReferenceProperty.get(this)
                if (last == null) {
                    old.referencedBy = BirBackReferenceCollectionLinkedListStyleImpl(newNext)
                } else {
                    nextTrackedElementReferenceProperty.set(last as T, newNext)
                }
            }

            if (new == null) {
                nextTrackedElementReferenceProperty.set(this as T, null)
            }
        } else if (new != null) {
            val oldFirst = new.referencedBy.first
            new.referencedBy = BirBackReferenceCollectionLinkedListStyleImpl(this)
            nextTrackedElementReferenceProperty.set(this as T, oldFirst)
        }
    }*/

    protected fun registerTrackedBackReferenceTo(value: Any?, referenceIndex: Int, unregisterFrom: BirElementBase?) {
        if (unregisterFrom == null) {
            if (value is BirElementTrackingBackReferences) {
                val referenceMask = (1 shl referenceIndex).toByte()
                if ((registeredBackRefs and referenceMask).toInt() == 0) {
                    registerTrackedBackReference(value, referenceIndex)
                }
            }
        } else {
            if (value === unregisterFrom) {
                unsetTrackedBackReference(referenceIndex)
            }
        }
    }

    protected fun setTrackedElementReference(
        old: Any?,
        new: Any?,
        referenceIndex: Int,
    ) {
        if (old === new) return

        if (old is BirElementTrackingBackReferences) {
            val newCollection = old._referencedBy.remove(this)
            require(newCollection != null) { "Element $this was not registered as a back reference on element $old" }
            old._referencedBy = newCollection
            unsetTrackedBackReference(referenceIndex)
        }
        if (attachedToTree && new is BirElementTrackingBackReferences) {
            registerTrackedBackReference(new, referenceIndex)
        }
    }

    private fun registerTrackedBackReference(target: BirElementTrackingBackReferences, referenceIndex: Int) {
        val referenceMask = (1 shl referenceIndex).toByte()
        target._referencedBy = target._referencedBy.add(this)
        registeredBackRefs = registeredBackRefs or referenceMask
    }

    private fun unsetTrackedBackReference(referenceIndex: Int) {
        val referenceMask = (1 shl referenceIndex).toByte()
        registeredBackRefs = registeredBackRefs and referenceMask.inv()
    }


    internal fun <T> getAuxData(token: BirElementAuxStorageToken<*, T>): T? {
        return auxStorage?.get(token.key.index) as T?
    }

    internal fun <T> setAuxData(token: BirElementAuxStorageToken<*, T>, value: T?) {
        var auxStorage = auxStorage
        if (auxStorage == null) {
            if (value == null) {
                // optimization: next read will return null if the array is null, so no need to initialize it
                return
            }

            val size = token.manager.getInitialAuxStorageArraySize(javaClass)
            auxStorage = if (size == 0) null else arrayOfNulls(size)
            this.auxStorage = auxStorage
        }

        auxStorage!![token.key.index] = value
    }

    // to be replaced by fine-grained control of which data to copy
    internal fun tmpCopyAuxData(from: BirElementBase) {
        auxStorage = from.auxStorage?.copyOf()
    }


    companion object {
        private const val FLAG_ATTACHED_TO_TREE: Byte = (1 shl 0).toByte()
        private const val FLAG_HAS_CHILDREN: Byte = (1 shl 1).toByte()
        private const val FLAG_IS_IN_CLASS_CACHE: Byte = (1 shl 2).toByte()
        private const val FLAG_NEXT_ELEMENT_IS_OPTIMIZED_FROM_CLASS_CACHE: Byte = (1 shl 3).toByte()
        private const val FLAG_ATTACHED_DURING_BY_CLASS_ITERATION: Byte = (1 shl 4).toByte()

        private const val CONTAINING_LIST_ID_BITS = 3
        private const val LEVEL_MASK: Short = (-1 ushr (16 + CONTAINING_LIST_ID_BITS)).toShort()
        private const val CONTAINING_LIST_ID_MASK: Short = (-1 shl (16 - CONTAINING_LIST_ID_BITS)).toShort()
    }
}

context (BirTreeContext)
fun BirElement.replaceWith(new: BirElement?, hintPreviousElement: BirElementBase? = null) {
    this as BirElementBase

    val parent = parent
    require(parent != null) { "Element is not bound to a tree - its parent is null" }

    val list = getContainingList()
    if (list != null) {
        @Suppress("UNCHECKED_CAST")
        replaceInsideList(list as BirChildElementList<BirElement>, new, hintPreviousElement)
    } else {
        parent.replaceChildProperty(this, new)
    }
}

context (BirTreeContext)
fun BirElement.remove(hintPreviousElement: BirElementBase? = null) = replaceWith(null, hintPreviousElement)