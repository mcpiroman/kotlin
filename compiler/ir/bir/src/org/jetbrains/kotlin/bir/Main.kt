/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(ObsoleteDescriptorBasedAPI::class)

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.utils.Ir2BirConverter
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.utils.addToStdlib.measureTimeMillisWithResult
import java.io.File
import kotlin.time.ExperimentalTime

@OptIn(ObsoleteDescriptorBasedAPI::class, ExperimentalTime::class)
fun main(argv: Array<String>) {
    val args = argv.toList().chunked(2) { it[0] to it[1] }.toMap()
    println("Loading...")

    val mainModulePath = "../../../libraries/stdlib/wasm/build/libs/kotlin-stdlib-wasm-wasm-1.9.255-SNAPSHOT.klib"
    val (srcModule, configuration) = measureTimeMillisWithResult {
        prepareIr(mainModulePath)
    }.also {
        println("loaded in: ${it.first}ms")
    }.second

    val irDumpDir: File? = args["-dumpDir"]?.let { File(it) }
    prepareIrForCompilationCommon(srcModule)

    repeat(1) {
        val converter = Ir2BirConverter(465000)
        val (backendContext, birModule) = measureTimeMillisWithResult {
            val backendContext = createBirBackendContext(srcModule, configuration, converter)
            val birModule = converter.copyIrTree(backendContext, srcModule.allDependencies).first() as BirModuleFragment
            backendContext to birModule
        }.also {
            println("create context and ir->bir in: ${it.first}ms")
        }.second
        backendContext.setModuleFragment(birModule)

        runBirCompilation(
            backendContext,
            birModule,
            true,
            irDumpDir,
            //setOf("RemoveInlineDeclarationsWithReifiedTypeParametersLowering", "TailrecLowering")
        )
    }

    repeat(1) { iteration ->
        /*val allDependencies = (srcModule.allDependencies zip deepCopyIr(srcModule.allDependencies)).toMap()
        val newModuleFragment = allDependencies[srcModule.module]!!*/
        prepareCorrespondingIrPhasesCompilation(srcModule.module, srcModule, configuration/*, allDependencies.values.toList()*/)
            .runCorrespondingIrPhases(true, irDumpDir)
        println("Done $iteration iteration")
    }

    /*
     val irTree = srcModule.module
     checkIterationsMatch(irTree, birTree)
     */
}