/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.benchmarks

import kotlinx.benchmark.Benchmark
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirFunctionAccessExpression
import org.jetbrains.kotlin.bir.traversal.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

open class IterationFinding2ClassesBenchmark : BenchmarkOnRealCodeBase(true) {
    @Benchmark
    fun traverseIr(): Int {
        var i = 0
        var j = 0
        irRoot.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitFunctionAccess(expression: IrFunctionAccessExpression) {
                i++
                super.visitFunctionAccess(expression)
            }

            override fun visitProperty(declaration: IrProperty) {
                j++
                super.visitProperty(declaration)
            }
        })
        return i - j
    }


    @Benchmark
    fun traverseBirStackBased(): Int {
        var i = 0
        var j = 0
        birRoot.traverseStackBased { element ->
            if (element is BirFunctionAccessExpression) {
                i++
            } else if (element is BirVariable) {
                j++
            }
            element.walkIntoChildren()
        }
        return i - j
    }

    @Benchmark
    fun traverseBirVisitorBased(): Int {
        var i = 0
        var j = 0
        birRoot.accept(
            object : BirElementVisitor() {
                override fun visitElement(element: BirElement) {
                    if (element is BirFunctionAccessExpression) {
                        i++
                    } else if (element is BirVariable) {
                        j++
                    }
                    element.acceptChildren(this)
                }
            }
        )
        return i - j
    }

    @Benchmark
    fun traverseBirParentBased(): Int {
        var i = 0
        var j = 0
        birRoot.traverseParentBased { element ->
            if (element is BirFunctionAccessExpression) {
                i++
            } else if (element is BirVariable) {
                j++
            }
            NextWalkStep.StepInto
        }
        return i - j
    }
}