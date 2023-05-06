/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.traversal.BirElementVisitor
import org.jetbrains.kotlin.bir.traversal.accept

private interface MockBirDeclaration : BirElement {
    context (BirTreeContext)
    var field1: MockBirValueParameter?

    companion object
}

private abstract class MockBirClass() : BirElementBase(), MockBirDeclaration, BirElementTrackingBackReferences {
    context (BirTreeContext)
    abstract override var field1: MockBirValueParameter?

    context (BirTreeContext)
    abstract var field2: MockBirValueParameter

    context (BirTreeContext)
    abstract var field3: MockBirTypeParameter?

    abstract val list1: BirChildElementList<MockBirDeclaration>
    abstract val list2: BirChildElementList<MockBirDeclaration>
    abstract val list3: BirChildElementList<MockBirDeclaration>
}

private class MockBirClassImpl(
    override var sourceSpan: SourceSpan,
    field2: MockBirValueParameter
) : MockBirClass() {
    override var _referencedBy = BirBackReferenceCollectionArrayStyleImpl()

    private var _field1: MockBirValueParameter? = null
    context (BirTreeContext)
    override var field1: MockBirValueParameter?
        get() = _field1
        set(value) {
            setChildField(_field1, value, null)
            _field1 = value
        }

    private var _field2: MockBirValueParameter = field2
    context (BirTreeContext)
    override var field2: MockBirValueParameter
        get() = _field2
        set(value) {
            setChildField(_field2, value, field1)
            _field2 = value
        }

    private var _field3: MockBirTypeParameter? = null
    context (BirTreeContext)
    override var field3: MockBirTypeParameter?
        get() = _field3
        set(value) {
            setChildField(_field3, value, field2)
            _field3 = value
        }

    override val list1 = BirChildElementList<MockBirDeclaration>(this)
    override val list2 = BirChildElementList<MockBirDeclaration>(this)
    override val list3 = BirChildElementList<MockBirDeclaration>(this)

    init {
        initChildField(_field1, null)
        initChildField(_field2, _field1)
        initChildField(_field3, _field2)
    }

    override fun getFirstChild(): BirElement =
        _field1 ?: _field2

    override fun getChildren(children: Array<BirElementOrList?>): Int {
        children[0] = _field1
        children[1] = _field2
        children[2] = _field3
        children[3] = list1
        children[4] = list2
        children[5] = list3
        return 6
    }

    override fun acceptChildren(visitor: BirElementVisitor) {
        _field1?.accept(visitor)
        _field2.accept(visitor)
        _field3?.accept(visitor)
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

    context (BirTreeContext)
    override fun replaceChildProperty(old: BirElement, new: BirElement?) {
        when {
            old === _field1 -> field1 = new as MockBirValueParameter?
            old === _field2 -> field2 = new as MockBirValueParameter
            old === _field3 -> field3 = new as MockBirTypeParameter?
            else -> throwChildForReplacementNotFound(old)
        }
    }
}

private class MockBirConstructor(
    override var sourceSpan: SourceSpan,
) : BirElementBase() {
    var target: MockBirClass? = null
        set(value) {
            setTrackedElementReference(field, value, 0)
            field = value
        }

    override fun registerTrackedBackReferences(unregisterFrom: BirElementBase?) {
        registerTrackedBackReferenceTo(target, 0, unregisterFrom)
    }
}

private interface MockBirValueParameter : BirElement
private interface MockBirTypeParameter : BirElement