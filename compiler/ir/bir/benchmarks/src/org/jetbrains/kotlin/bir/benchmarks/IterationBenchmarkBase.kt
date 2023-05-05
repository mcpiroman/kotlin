/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.benchmarks

import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.GeneralBirTreeContext
import org.jetbrains.kotlin.bir.prepareIr
import org.jetbrains.kotlin.bir.utils.Ir2BirConverterBase.Companion.convertToBir
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.openjdk.jmh.infra.BenchmarkParams

@State(Scope.Benchmark)
abstract class IterationBenchmarkBase {
    protected lateinit var irRoot: IrElement
    protected lateinit var birRoot: BirElement

    @Setup
    fun setup(params: BenchmarkParams) {
        val srcIr =
            prepareIr("../../../../libraries/stdlib/wasm/build/libs/kotlin-stdlib-wasm-wasm-1.9.255-SNAPSHOT.klib").moduleInfo.module
        irRoot = srcIr.deepCopyWithSymbols()
        birRoot = srcIr.convertToBir(GeneralBirTreeContext())
    }
}