/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.benchmarks

import kotlinx.benchmark.Benchmark
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.traversal.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

open class SimpleIterationBenchmark : BenchmarkOnRealCodeBase(true) {
    @Benchmark
    fun traverseIr(): Int {
        var i = 0
        irRoot.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                i++
                element.acceptChildrenVoid(this)
            }
        })
        return i
    }

    @Benchmark
    fun traverseBirParentBased(): Int {
        var i = 0
        birRoot.traverseParentBased {
            i++
            NextWalkStep.StepInto
        }
        return i
    }

    @Benchmark
    fun traverseBirStackBased(): Int {
        var i = 0
        birRoot.traverseStackBased {
            i++
            it.walkIntoChildren()
        }
        return i
    }

    @Benchmark
    fun traverseBirVisitorBased(): Int {
        var i = 0
        birRoot.accept(
            object : BirElementVisitor() {
                override fun visitElement(element: BirElement) {
                    i++
                    element.acceptChildren(this)
                }
            }
        )
        return i
    }
}