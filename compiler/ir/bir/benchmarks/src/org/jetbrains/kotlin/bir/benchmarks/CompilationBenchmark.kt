/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.benchmarks

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.utils.Ir2BirConverter
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.IrModuleInfo
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.deepCopySavingMetadata
import org.openjdk.jmh.annotations.Level

open class CompilationBenchmark {
    @State(Scope.Benchmark)
    open class MasterData {
        lateinit var configuration: CompilerConfiguration
        lateinit var srcModule: IrModuleInfo
        lateinit var srcModuleFragment: IrModuleFragment

        @Setup
        fun load() {
            val mainModulePath =
                "F:\\code\\_extern\\kotlin\\libraries\\stdlib\\wasm\\build\\libs\\kotlin-stdlib-wasm-wasm-1.9.255-SNAPSHOT.klib"
            prepareIr(mainModulePath).also {
                srcModule = it.moduleInfo
                configuration = it.configuration
            }
            srcModuleFragment = srcModule.module
            prepareIrForCompilationCommon(srcModule)
        }
    }

    @State(Scope.Thread)
    open class BirData {
        lateinit var backendContext: WasmBirContext
        lateinit var birModule: BirModuleFragment

        @Setup(Level.Invocation)
        @OptIn(ObsoleteDescriptorBasedAPI::class)
        fun prepareCompilation(masterData: MasterData) {
            val converter = Ir2BirConverter()
            backendContext = createBirBackendContext(masterData.srcModule, masterData.configuration, converter)
            converter.setExpectedTreeSize(400000)
            birModule = converter.convertIrTree(backendContext, masterData.srcModule.allDependencies).first() as BirModuleFragment
            backendContext.setModuleFragment(birModule)
        }
    }

    @State(Scope.Thread)
    open class IrData {
        lateinit var irPhasesCompilationSetup: IrPhasesCompilationSetup

        @Setup(Level.Invocation)
        @OptIn(ObsoleteDescriptorBasedAPI::class)
        fun prepareCompilation(masterData: MasterData) {
            val newModuleFragment = masterData.srcModuleFragment.deepCopySavingMetadata()
            irPhasesCompilationSetup =
                prepareCorrespondingIrPhasesCompilation(newModuleFragment, masterData.srcModule, masterData.configuration)
        }
    }

    @Benchmark
    fun compileBir(data: BirData) {
        runBirCompilation(data.backendContext, data.birModule, false)
    }

    @Benchmark
    fun compileIr(data: IrData) {
        data.irPhasesCompilationSetup.runCorrespondingIrPhases(false)
    }
}