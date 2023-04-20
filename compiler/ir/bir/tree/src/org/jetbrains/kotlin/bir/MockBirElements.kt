/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept

context (BirTreeContext)
private class MockBirClass(
    override val startOffset: Int,
    override val endOffset: Int,
    field2: MockBirValueParameter
) : BirElementBase(), BirElementTrackingBackReferences {
    override var referencedBy = BirBackReferenceCollectionArrayStyle()

    var field1: MockBirValueParameter? = null
        set(value) {
            setChildField(field, value, null)
            field = value
        }
    var field2: MockBirValueParameter = field2
        set(value) {
            setChildField(field, value, field1)
            field = value
        }
    var field3: MockBirTypeParameter? = null
        set(value) {
            setChildField(field, value, field2)
            field = value
        }
    val list1 = BirChildElementList<MockBirDeclaration>(this)
    val list2 = BirChildElementList<MockBirDeclaration>(this)
    val list3 = BirChildElementList<MockBirDeclaration>(this)

    init {
        initChildField(field1, null)
        initChildField(field2, field1)
        initChildField(field3, field2)
    }

    override fun getFirstChild(): BirElement =
        field1 ?: field2

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = field1
        children[1] = field2
        children[2] = field3
        children[3] = list1
        children[4] = list2
        children[5] = list3
        return 6
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        field1?.accept(visitor)
        field2.accept(visitor)
        field3?.accept(visitor)
        list1.acceptChildren(visitor)
        list2.acceptChildren(visitor)
        list3.acceptChildren(visitor)
    }

    /*override fun notifyChildListChangedHead(list: BirChildElementList<*>) {
        if (list !== list1) {
            if (list !== list2) {
                if (list2.setNextSibling(list.next)) {
                    return
                }
            }
            if (list2.setNextSibling(list.next)) {
                return
            }
        }
        assert(list === list3)
        val lastChild = field3 ?: field2
        (lastChild as BirElementBase).next = list.next
    }*/

    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
            old === field1 -> field1 = new as MockBirValueParameter?
            old === field2 -> field2 = new as MockBirValueParameter
            old === field3 -> field3 = new as MockBirTypeParameter?
            else -> throwChildForReplacementNotFound(old)
        }
    }
}

context (BirTreeContext)
private class MockBirConstructor(
    override val startOffset: Int,
    override val endOffset: Int,
) : BirElementBase() {
    var target: MockBirClass? = null
        set(value) {
            setTrackedElementReferenceArrayStyle(field, value)
            field = value
        }

    init {
        initTrackedElementReferenceArrayStyle(target)
    }
}

private interface MockBirDeclaration : BirElement
private interface MockBirValueParameter : BirElement
private interface MockBirTypeParameter : BirElement