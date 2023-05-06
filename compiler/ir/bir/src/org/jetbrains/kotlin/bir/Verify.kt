/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.expressions.BirNoExpression
import org.jetbrains.kotlin.bir.traversal.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.utils.addToStdlib.measureTimeMillisWithResult

private class Node(
    val element: Any
) {
    val children = mutableListOf<Node>()

    override fun toString() = "(${children.size} ${element})"
}

private class IrIterator(val root: IrElement) : IrElementVisitorVoid {
    val tree = Node(root)
    private var head = tree

    fun lower() {
        root.acceptChildrenVoid(this)
    }

    override fun visitElement(element: IrElement) {
        val child = Node(element)
        head.children.add(child)
        val oldHead = head
        head = child
        element.acceptChildrenVoid(this)
        head = oldHead
    }
}

private class BirIterator(val root: BirElement) {
    val tree = Node(root)
    private var head = tree

    fun lower() {
        root.traverseStackBased(false) { element ->
            if (element !is BirNoExpression) {
                val child = Node(element)
                head.children.add(child)
                val oldHead = head
                head = child
                element.walkIntoChildren()
                head = oldHead
            }
        }
    }
}

fun checkIterationsMatch(irRoot: IrElement, birRoot: BirElement) {
    val lowerIr = IrIterator(irRoot)
    lowerIr.lower()

    val lowerBir = BirIterator(birRoot)
    lowerBir.lower()

    fun visitNode(ir: Node, bir: Node) {
        if (ir.children.size != bir.children.size) {
            println("Here it is! IR(${ir.children.size}): $ir, BIR(${bir.children.size}): $bir")
        }

        val irElements = bir.children.associateBy { (it.element as BirElementBase)/*.originalIrElement!!*/ }
        for (oldNode in ir.children) {
            val birChild = irElements[oldNode.element]
            if (birChild != null) {
                visitNode(oldNode, birChild)
            }
        }
    }

    visitNode(lowerIr.tree, lowerBir.tree)
}

fun runAllTreeTraversals(birTree: BirElement, irTree: IrElement) {
    runTraversal("bir by stack") { traverseBirStackBased(birTree) }
    runTraversal("bir by parent") { traverseBirParentBased(birTree) }
    runTraversal("bir by visitor") { traverseBirVisitorBased(birTree) }
    runTraversal("ir") { traverseIr(irTree) }
}

private fun runTraversal(name: String, traverse: () -> Int) {
    val result = measureTimeMillisWithResult { traverse() }
    println("Traversed $name=${result.second} in ${result.first}ms")
}

private fun traverseIr(root: IrElement): Int {
    var i = 0
    root.acceptVoid(object : IrElementVisitorVoid {
        override fun visitElement(element: IrElement) {
            i++
            element.acceptChildrenVoid(this)
        }
    })
    return i
}

private fun traverseBirParentBased(root: BirElement): Int {
    var i = 0
    root.traverseParentBased {
        if (it !is BirNoExpression) i++
        NextWalkStep.StepInto
    }
    return i
}

private fun traverseBirStackBased(root: BirElement): Int {
    var i = 0
    root.traverseStackBased {
        if (it !is BirNoExpression) i++
        it.walkIntoChildren()
    }
    return i
}

private fun traverseBirParentBasedWithInnerPtr(root: BirElement): Int {
    var i = 0
    root.traverseParentBasedWithInnerPtr {
        if (it !is BirNoExpression) i++
        NextWalkStep.StepInto
    }
    return i
}

private fun traverseBirStackBasedWithInnerPtr(root: BirElement): Int {
    var i = 0
    root.traverseStackBasedWithInnerPtr {
        if (it !is BirNoExpression) i++
        it.walkIntoChildren()
    }
    return i
}


private fun traverseBirVisitorBased(root: BirElement): Int {
    var i = 0
    root.accept(object : BirElementVisitor() {
        override fun visitElement(element: BirElement) {
            if (element !is BirNoExpression) i++
            element.acceptChildren(this)
        }
    })
    return i
}