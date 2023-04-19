/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.traversal.BirElementVisitor

sealed class BirElementBaseOrList : BirElementOrList {
    internal abstract val next: BirElementBase?
}

abstract class BirElementBase : BirElement, BirElementBaseOrList() {
    //var originalIrElement: IrElement? = null
    internal var rawParent: BirElementBaseOrList? = null
    override var next: BirElementBase? = null
    internal var firstChildPtr: BirElementBase? = null
    private var auxStorage: Array<Any?>? = null

    final override val parent: BirElementBase?
        get() = when (val owner = rawParent) {
            null -> null
            is BirElementBase -> owner
            is BirChildElementList<*> -> owner.parent
        }

    internal open fun getFirstChild(): BirElement? = null
    internal open fun getChildren(children: Array<BirElementOrList?>): Int = 0
    override fun acceptChildren(visitor: BirElementVisitor) = Unit
    protected open fun replaceChildProperty(old: BirElement, new: BirElement?) = Unit
    internal open fun replaceSymbolProperty(old: BirSymbol, new: BirSymbol) = Unit

    protected fun throwChildForReplacementNotFound(old: BirElement) {
        throw IllegalStateException("The child property $old not found in its parent $this")
    }

    internal fun checkCanBoundToTree() {
        require(rawParent == null) { "Element $this is already bound to the tree as a child of $parent." }
    }

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
        value as BirElementBase?
        prevChildOrList as BirElementBaseOrList?

        setChildFieldCommon(
            value,
            prevChildOrList,
            null,
            value
        )
    }

    protected fun setChildField(
        old: BirElement?,
        new: BirElement?,
        prevChildOrList: BirElementOrList?,
    ) {
        if (new === old) return

        old as BirElementBase?
        new as BirElementBase?
        prevChildOrList as BirElementBaseOrList?

        setChildFieldCommon(
            new,
            prevChildOrList,
            if (old != null) old.next
            else if (prevChildOrList != null) prevChildOrList.next
            else firstChildPtr,
            new ?: old?.next
        )

        if (old != null) {
            old.rawParent = null
            old.next = null
        }
    }

    private fun setChildFieldCommon(
        new: BirElementBase?,
        prevChildOrList: BirElementBaseOrList?,
        next: BirElementBase?,
        prevNext: BirElementBase?,
    ) {
        if (new != null) {
            new.checkCanBoundToTree()
            new.rawParent = this
            new.next = next
        }

        when (prevChildOrList) {
            null -> firstChildPtr = prevNext
            is BirElementBase -> prevChildOrList.next = prevNext
            is BirChildElementList<*> -> {
                prevChildOrList.setNextSibling(prevNext)
                if (prevChildOrList.isEmpty()) {
                    setNextAfterNewChildSetSlow(prevNext, prevChildOrList)
                }
            }
        }
    }

    internal fun setNextAfterNewChildSetSlow(newNext: BirElementBase?, lastListBeforeTheNewElement: BirChildElementList<*>) {
        val children = arrayOfNulls<BirElementOrList?>(8)
        val maxChildrenCount = getChildren(children)
        val listIndex = children.indexOf(lastListBeforeTheNewElement)
        assert(listIndex in 0 until maxChildrenCount)
        for (i in listIndex - 1 downTo 0) {
            when (val child = children[i]) {
                null -> {}
                is BirElementBase -> {
                    child.next = newNext
                    return
                }
                is BirChildElementList<*> -> {
                    child.setNextSibling(newNext)
                    if (child.isNotEmpty()) return
                }
                else -> error(child.toString())
            }
        }

        firstChildPtr = newNext
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
                    old.referencedBy = BirBackReferenceCollectionLinkedListStyle(newNext)
                } else {
                    nextTrackedElementReferenceProperty.set(last as T, newNext)
                }
            }

            if (new == null) {
                nextTrackedElementReferenceProperty.set(this as T, null)
            }
        } else if (new != null) {
            val oldFirst = new.referencedBy.first
            new.referencedBy = BirBackReferenceCollectionLinkedListStyle(this)
            nextTrackedElementReferenceProperty.set(this as T, oldFirst)
        }
    }*/

    protected fun initTrackedElementReferenceArrayStyle(value: Any?) {
        if (value is BirElementTrackingBackReferences) {
            value.referencedBy = value.referencedBy.add(this)
        }
    }

    protected fun setTrackedElementReferenceArrayStyle(
        old: Any?,
        new: Any?,
    ) {
        if (old === new) return

        if (old is BirElementTrackingBackReferences) {
            val newCollection = old.referencedBy.remove(this)
            require(newCollection != null) { "Element $this was not registered as a back reference on element $old" }
            old.referencedBy = newCollection
        }
        if (new is BirElementTrackingBackReferences) {
            new.referencedBy = new.referencedBy.add(this)
        }
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
}

interface BirElementPropertyTransformer {
    fun <T> transform(value: T, isMutable: Boolean): T
}