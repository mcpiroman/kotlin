/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.benchmarks

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Param
import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.BirCall
import org.jetbrains.kotlin.bir.symbols.BirSimpleFunctionSymbol
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.openjdk.jmh.infra.BenchmarkParams

open class ElementCacheSlotSelectionBenchmark : BenchmarkOnRealCodeBase(false) {
    private var allElements = emptyArray<BirElementBase>()

    @Param("0")
    private var findFromMinimalIndex: Int = 0

    private lateinit var backendContext_wasmSymbols_jsCode: BirSimpleFunctionSymbol
    private var allowExternalInlining = false

    override fun setup(params: BenchmarkParams) {
        super.setup(params)
        var allElementCount = 0
        birRoot.traverseStackBased {
            allElementCount++
            it.walkIntoChildren()
        }

        allElements = arrayOfNulls<BirElementBase>(allElementCount) as Array<BirElementBase>
        var i = 0
        birRoot.traverseStackBased {
            allElements[i++] = it as BirElementBase

            if (!::backendContext_wasmSymbols_jsCode.isInitialized && it is BirSimpleFunction) {
                backendContext_wasmSymbols_jsCode = it
            }

            it.walkIntoChildren()
        }
    }

    private inline fun classifyAll(selector: (BirElementBase, Int) -> Int): Int {
        val minimalIndex = findFromMinimalIndex
        var sum = 0
        for (element in allElements) {
            val i = selector(element, minimalIndex)
            sum += i
        }
        return sum
    }


    private val conditions = arrayOf<BirElementFeatureCacheCondition?>(
        null,
        BirElementFeatureCacheCondition { it is BirCall && it.target == backendContext_wasmSymbols_jsCode },
        BirElementFeatureCacheCondition { it is BirProperty && it.isLateinit && !it.isFakeOverride },
        BirElementFeatureCacheCondition { it is BirVariable && it.isLateinit },
        BirElementFeatureCacheCondition { it is BirCall && it.target is BirSimpleFunction },
        BirElementFeatureCacheCondition { it is BirVariable && (it.isVar || it.initializer == null) },
        BirElementFeatureCacheCondition { it is BirSimpleFunction && it.isInline },
        BirElementFeatureCacheCondition { it is BirFunction && it.isInline },
        BirElementFeatureCacheCondition { it is BirFunction && it.isInline },
        BirElementFeatureCacheCondition { it is BirSimpleFunction && it.isInline },
        BirElementFeatureCacheCondition { it is BirFunction && it.isInline && (allowExternalInlining || !it.isExternal) },
        BirElementFeatureCacheCondition { it is BirFunction && it.isInline },
        BirElementFeatureCacheCondition { it is BirSimpleFunction && it.isTailrec },
    )

    @Benchmark
    fun array() = classifyAll { it, minimalIndex ->
        val conditions = conditions
        for (i in minimalIndex + 1..conditions.lastIndex) {
            val condition = conditions[i]!!
            if (condition.matches(it, 0)) {
                return@classifyAll i
            }
        }
        return@classifyAll 0
    }

    private class GeneratedFunctionVirtualDispatch(conditions: Array<BirElementFeatureCacheCondition?>) {
        private val condition1 = conditions[1]!!
        private val condition2 = conditions[2]!!
        private val condition3 = conditions[3]!!
        private val condition4 = conditions[4]!!
        private val condition5 = conditions[5]!!
        private val condition6 = conditions[6]!!
        private val condition7 = conditions[7]!!
        private val condition8 = conditions[8]!!
        private val condition9 = conditions[9]!!
        private val condition10 = conditions[10]!!
        private val condition11 = conditions[11]!!
        private val condition12 = conditions[12]!!

        fun select_flat(it: BirElementBase, minimumIndex: Int): Int {
            if (condition1.matches(it, 0) && 1 >= minimumIndex) return 1
            if (condition2.matches(it, 0) && 2 >= minimumIndex) return 2
            if (condition3.matches(it, 0) && 3 >= minimumIndex) return 3
            if (condition4.matches(it, 0) && 4 >= minimumIndex) return 4
            if (condition5.matches(it, 0) && 5 >= minimumIndex) return 5
            if (condition6.matches(it, 0) && 6 >= minimumIndex) return 6
            if (condition7.matches(it, 0) && 7 >= minimumIndex) return 7
            if (condition8.matches(it, 0) && 8 >= minimumIndex) return 8
            if (condition9.matches(it, 0) && 9 >= minimumIndex) return 9
            if (condition10.matches(it, 0) && 10 >= minimumIndex) return 10
            if (condition11.matches(it, 0) && 11 >= minimumIndex) return 11
            if (condition12.matches(it, 0) && 12 >= minimumIndex) return 12
            return 0
        }

        fun select_dispatchOnType_repeatOnSubtype(it: BirElementBase, minimumIndex: Int): Int {
            when (it) {
                is BirCall -> when {
                    condition1.matches(it, 0) && 1 >= minimumIndex -> return 1
                    condition4.matches(it, 0) && 4 >= minimumIndex -> return 4
                }
                is BirProperty -> when {
                    condition2.matches(it, 0) && 2 >= minimumIndex -> return 2
                }
                is BirVariable -> when {
                    condition3.matches(it, 0) && 3 >= minimumIndex -> return 3
                    condition5.matches(it, 0) && 5 >= minimumIndex -> return 5
                }
                is BirFunction -> when (it) {
                    is BirSimpleFunction -> when {
                        condition6.matches(it, 0) && 6 >= minimumIndex -> return 6
                        condition7.matches(it, 0) && 7 >= minimumIndex -> return 7
                        condition8.matches(it, 0) && 8 >= minimumIndex -> return 8
                        condition9.matches(it, 0) && 9 >= minimumIndex -> return 9
                        condition10.matches(it, 0) && 10 >= minimumIndex -> return 10
                        condition11.matches(it, 0) && 11 >= minimumIndex -> return 11
                        condition12.matches(it, 0) && 12 >= minimumIndex -> return 12
                    }
                    else -> when {
                        condition7.matches(it, 0) && 7 >= minimumIndex -> return 7
                        condition8.matches(it, 0) && 8 >= minimumIndex -> return 8
                        condition10.matches(it, 0) && 10 >= minimumIndex -> return 10
                        condition11.matches(it, 0) && 11 >= minimumIndex -> return 11
                    }
                }
            }

            return 0
        }
    }

    private val generatedFunctionVirtualDispatch = GeneratedFunctionVirtualDispatch(conditions)

    @Benchmark
    fun generatedFunction_withMinimalIndex_virtualDispatch_flat() = classifyAll { it, minimalIndex ->
        generatedFunctionVirtualDispatch.select_flat(it, minimalIndex)
    }

    @Benchmark
    fun generatedFunction_withMinimalIndex_virtualDispatch_dispatchOnType_repeatOnSubtype() = classifyAll { it, minimalIndex ->
        generatedFunctionVirtualDispatch.select_dispatchOnType_repeatOnSubtype(it, minimalIndex)
    }

    @Benchmark
    fun manual_function_withMinimalIndex_flat() = classifyAll { it, minimalIndex ->
        when {
            it is BirCall && it.target == backendContext_wasmSymbols_jsCode && 1 >= minimalIndex -> 1
            it is BirProperty && it.isLateinit && !it.isFakeOverride && 2 >= minimalIndex -> 2
            it is BirVariable && it.isLateinit && 3 >= minimalIndex -> 3
            it is BirCall && it.target is BirSimpleFunction && 4 >= minimalIndex -> 4
            it is BirVariable && (it.isVar || it.initializer == null) && 5 >= minimalIndex -> 5
            it is BirSimpleFunction && it.isInline && 6 >= minimalIndex -> 6
            it is BirFunction && it.isInline && 7 >= minimalIndex -> 7
            it is BirFunction && it.isInline && 8 >= minimalIndex -> 8
            it is BirSimpleFunction && it.isInline && 9 >= minimalIndex -> 9
            it is BirFunction && it.isInline && (allowExternalInlining || !it.isExternal) && 10 >= minimalIndex -> 10
            it is BirFunction && it.isInline && 11 >= minimalIndex -> 11
            it is BirSimpleFunction && it.isTailrec && 12 >= minimalIndex -> 12
            else -> 0
        }
    }

    @Benchmark
    fun manualFunction_withMinimalIndex_dispatchOnType_flatOnSubtype() = classifyAll { it, minimalIndex ->
        when (it) {
            is BirCall -> when {
                it.target == backendContext_wasmSymbols_jsCode && 1 >= minimalIndex -> return@classifyAll 1
                it.target is BirSimpleFunction && 4 >= minimalIndex -> return@classifyAll 4
            }
            is BirProperty -> when {
                it.isLateinit && !it.isFakeOverride && 2 >= minimalIndex -> return@classifyAll 2
            }
            is BirVariable -> when {
                it.isLateinit && 3 >= minimalIndex -> return@classifyAll 3
                (it.isVar || it.initializer == null) && 5 >= minimalIndex -> return@classifyAll 5
            }
            is BirFunction -> when {
                it is BirSimpleFunction && it.isInline && 6 >= minimalIndex -> return@classifyAll 6
                it.isInline && 7 >= minimalIndex -> return@classifyAll 7
                it.isInline && 8 >= minimalIndex -> return@classifyAll 8
                it is BirSimpleFunction && it.isInline && 9 >= minimalIndex -> return@classifyAll 9
                it.isInline && (allowExternalInlining || !it.isExternal) && 10 >= minimalIndex -> return@classifyAll 10
                it.isInline && 11 >= minimalIndex -> return@classifyAll 11
                it is BirSimpleFunction && it.isTailrec && 12 >= minimalIndex -> return@classifyAll 12
            }
        }
        return@classifyAll 0
    }

    @Benchmark
    fun manualFunction_withMinimalIndexPostfix_dispatchOnType_repeatedForSubtype() = classifyAll { it, minimalIndex ->
        when (it) {
            is BirCall -> when {
                it.target == backendContext_wasmSymbols_jsCode && 1 >= minimalIndex -> return@classifyAll 1
                it.target is BirSimpleFunction && 4 >= minimalIndex -> return@classifyAll 4
            }
            is BirProperty -> when {
                it.isLateinit && !it.isFakeOverride && 2 >= minimalIndex -> return@classifyAll 2
            }
            is BirVariable -> when {
                it.isLateinit && 3 >= minimalIndex -> return@classifyAll 3
                (it.isVar || it.initializer == null) && 5 >= minimalIndex -> return@classifyAll 5
            }
            is BirFunction -> when (it) {
                is BirSimpleFunction -> when {
                    it.isInline && 6 >= minimalIndex -> return@classifyAll 6
                    it.isInline && 7 >= minimalIndex -> return@classifyAll 7
                    it.isInline && 8 >= minimalIndex -> return@classifyAll 8
                    it.isInline && 9 >= minimalIndex -> return@classifyAll 9
                    it.isInline && (allowExternalInlining || !it.isExternal) && 10 >= minimalIndex -> return@classifyAll 10
                    it.isInline && 11 >= minimalIndex -> return@classifyAll 11
                    it.isTailrec && 12 >= minimalIndex -> return@classifyAll 12
                }
                else -> when {
                    it.isInline && 7 >= minimalIndex -> return@classifyAll 7
                    it.isInline && 8 >= minimalIndex -> return@classifyAll 8
                    it.isInline && (allowExternalInlining || !it.isExternal) && 10 >= minimalIndex -> return@classifyAll 10
                    it.isInline && 11 >= minimalIndex -> return@classifyAll 11
                }
            }
        }
        return@classifyAll 0
    }

    @Benchmark
    fun manualFunction_withMinimalIndexPrefix_dispatchOnType_repeatedForSubtype() = classifyAll { it, minimalIndex ->
        when (it) {
            is BirCall -> when {
                1 >= minimalIndex && it.target == backendContext_wasmSymbols_jsCode -> return@classifyAll 1
                4 >= minimalIndex && it.target is BirSimpleFunction -> return@classifyAll 4
            }
            is BirProperty -> when {
                2 >= minimalIndex && it.isLateinit && !it.isFakeOverride -> return@classifyAll 2
            }
            is BirVariable -> when {
                3 >= minimalIndex && it.isLateinit -> return@classifyAll 3
                5 >= minimalIndex && (it.isVar || it.initializer == null) -> return@classifyAll 5
            }
            is BirFunction -> when (it) {
                is BirSimpleFunction -> when {
                    6 >= minimalIndex && it.isInline -> return@classifyAll 6
                    7 >= minimalIndex && it.isInline -> return@classifyAll 7
                    8 >= minimalIndex && it.isInline -> return@classifyAll 8
                    9 >= minimalIndex && it.isInline -> return@classifyAll 9
                    10 >= minimalIndex && it.isInline && (allowExternalInlining || !it.isExternal) -> return@classifyAll 10
                    11 >= minimalIndex && it.isInline -> return@classifyAll 11
                    12 >= minimalIndex && it.isTailrec -> return@classifyAll 12
                }
                else -> when {
                    7 >= minimalIndex && it.isInline -> return@classifyAll 7
                    8 >= minimalIndex && it.isInline -> return@classifyAll 8
                    10 >= minimalIndex && it.isInline && (allowExternalInlining || !it.isExternal) -> return@classifyAll 10
                    11 >= minimalIndex && it.isInline -> return@classifyAll 11
                }
            }
        }
        return@classifyAll 0
    }

    @Benchmark
    fun manualFunction_noMinimalIndex_dispatchOnType_repeatedForSubtype() = classifyAll { it, minimalIndex ->
        when (it) {
            is BirCall -> when {
                it.target == backendContext_wasmSymbols_jsCode -> return@classifyAll 1
                it.target is BirSimpleFunction -> return@classifyAll 4
            }
            is BirProperty -> when {
                it.isLateinit && !it.isFakeOverride -> return@classifyAll 2
            }
            is BirVariable -> when {
                it.isLateinit -> return@classifyAll 3
                (it.isVar || it.initializer == null) -> return@classifyAll 5
            }
            is BirFunction -> when (it) {
                is BirSimpleFunction -> when {
                    it.isInline -> return@classifyAll 6
                    it.isInline -> return@classifyAll 7
                    it.isInline -> return@classifyAll 8
                    it.isInline -> return@classifyAll 9
                    it.isInline && (allowExternalInlining || !it.isExternal) -> return@classifyAll 10
                    it.isInline -> return@classifyAll 11
                    it.isTailrec -> return@classifyAll 12
                }
                else -> when {
                    it.isInline -> return@classifyAll 7
                    it.isInline -> return@classifyAll 8
                    it.isInline && (allowExternalInlining || !it.isExternal) -> return@classifyAll 10
                    it.isInline -> return@classifyAll 11
                }
            }
        }
        return@classifyAll 0
    }

    @Benchmark
    fun manualFunction_return01_noMinimalIndex_dispatchOnType_repeatedForSubtype() = classifyAll { it, minimalIndex ->
        when (it) {
            is BirCall -> when {
                it.target == backendContext_wasmSymbols_jsCode -> return@classifyAll 1
                it.target is BirSimpleFunction -> return@classifyAll 1
            }
            is BirProperty -> when {
                it.isLateinit && !it.isFakeOverride -> return@classifyAll 1
            }
            is BirVariable -> when {
                it.isLateinit -> return@classifyAll 1
                (it.isVar || it.initializer == null) -> return@classifyAll 1
            }
            is BirFunction -> when (it) {
                is BirSimpleFunction -> when {
                    it.isInline -> return@classifyAll 1
                    it.isInline -> return@classifyAll 1
                    it.isInline -> return@classifyAll 1
                    it.isInline -> return@classifyAll 1
                    it.isInline && (allowExternalInlining || !it.isExternal) -> return@classifyAll 1
                    it.isInline -> return@classifyAll 1
                    it.isTailrec -> return@classifyAll 1
                }
                else -> when {
                    it.isInline -> return@classifyAll 1
                    it.isInline -> return@classifyAll 1
                    it.isInline && (allowExternalInlining || !it.isExternal) -> return@classifyAll 1
                    it.isInline -> return@classifyAll 1
                }
            }
        }
        return@classifyAll 0
    }
}