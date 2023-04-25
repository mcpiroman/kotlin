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
    internal var rawParent: BirElementBaseOrList? = null
    override var next: BirElementBase? = null
    internal var firstChildPtr: BirElementBase? = null
    private var auxStorage: Array<Any?>? = null
    private var flags: Byte = 0
    private var registeredBackRefs: Byte = 0

    final override val parent: BirElementBase?
        get() = when (val rawParent = rawParent) {
            null -> null
            is BirElementBase -> rawParent
            is BirChildElementList<*> -> rawParent.parent
        }

    internal fun getContainingList(): BirChildElementList<*>? =
        rawParent as? BirChildElementList<*>

    internal var attachedToTree: Boolean
        get() = hasFlag(FLAG_ATTACHED_TO_TREE)
        set(value) = setFlag(FLAG_ATTACHED_TO_TREE, value)

    internal var hasChildren: Boolean
        get() = hasFlag(FLAG_HAS_CHILDREN)
        set(value) = setFlag(FLAG_HAS_CHILDREN, value)

    internal var inByClassCacheViaNextPtr: Boolean
        get() = hasFlag(FLAG_IN_BY_CLASS_CACHE_VIA_NEXT_PTR)
        set(value) = setFlag(FLAG_IN_BY_CLASS_CACHE_VIA_NEXT_PTR, value)

    private fun hasFlag(flag: Byte): Boolean =
        (flags and flag).toInt() != 0

    private fun setFlag(flag: Byte, value: Boolean) {
        flags = if (value) flags or flag else flags and flag.inv()
    }

    internal open fun getFirstChild(): BirElement? = null
    internal open fun getChildren(children: Array<BirElementOrList?>): Int = 0
    override fun acceptChildren(visitor: BirElementVisitor) = Unit
    protected open fun replaceChildProperty(old: BirElement, new: BirElement?) = Unit
    internal open fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) = Unit
    internal open fun registerTrackedBackReferences(unregisterFrom: BirElementBase?) = Unit

    protected fun throwChildForReplacementNotFound(old: BirElement) {
        throw IllegalStateException("The child property $old not found in its parent $this")
    }

    internal fun checkCanBoundToTree() {
        require(rawParent == null) { "Element $this is already bound to the tree as a child of $parent." }
    }

    context (BirTreeContext)
    fun replace(new: BirElement?) {
        val owner = rawParent
        require(owner != null) { "Element is not bound to a tree - its parent is null" }
        when (owner) {
            is BirElementBase -> {
                owner.replaceChildProperty(this, new)
            }
            is BirChildElementList<*> -> {
                owner as BirChildElementList<BirElement>
                replaceInsideList(owner, new, null)
            }
        }
    }

    context (BirTreeContext)
    internal fun replaceInsideList(
        list: BirChildElementList<BirElement>,
        new: BirElement?,
        hintPreviousElementBase: BirElementBase?,
    ) {
        val success = if (new == null) {
            list.remove(this)
        } else {
            list.replace(this, new)
        }

        if (!success) list.parent.throwChildForReplacementNotFound(this)
    }

    context (BirTreeContext)
    fun remove() = replace(null)

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
        prevChildOrList: BirElementOrList?
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

        if (old != null) {
            old.rawParent = null
            old.next = null
        }

        hasChildren = new != null || newsNext != null

        if (old != null) {
            elementDetached(old, prev)
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
            new.checkCanBoundToTree()
            new.rawParent = this
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
        lastListBeforeTheNewElement: BirChildElementList<*>
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

    context (BirTreeContext)
    internal fun childAttached(element: BirElementBase, prev: BirElementBase?) {
        if (attachedToTree) {
            elementAttached(element, prev)
        }
    }

    context (BirTreeContext)
    internal fun childDetached(element: BirElementBase, prev: BirElementBase?) {
        if (attachedToTree) {
            elementDetached(element, prev)
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


    companion object {
        private const val FLAG_ATTACHED_TO_TREE: Byte = 1
        private const val FLAG_HAS_CHILDREN: Byte = 2
        private const val FLAG_IN_BY_CLASS_CACHE_VIA_NEXT_PTR: Byte = 4
    }
}