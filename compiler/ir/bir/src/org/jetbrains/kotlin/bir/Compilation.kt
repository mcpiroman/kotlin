/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.backend.common.linkage.issues.checkNoUnboundSymbols
import org.jetbrains.kotlin.bir.backend.phases.LateinitLowering
import org.jetbrains.kotlin.bir.backend.phases.SharedVariablesLowering
import org.jetbrains.kotlin.bir.backend.phases.wasm.BirJsCodeCallsLowering
import org.jetbrains.kotlin.bir.backend.phases.wasm.ExcludeDeclarationsFromCodegen
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.utils.Ir2BirConverter
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.IrModuleInfo
import org.jetbrains.kotlin.ir.util.ExternalDependenciesGenerator
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.utils.addToStdlib.measureTimeMillisWithResult
import kotlin.system.measureTimeMillis

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun runBirCompilation(backendContext: WasmBirContext, moduleInfo: IrModuleInfo, converter: Ir2BirConverter) {
    val allModules = moduleInfo.allDependencies
    val symbolTable = moduleInfo.symbolTable
    val irLinker = moduleInfo.deserializer

    // Load declarations referenced during `context` initialization
    allModules.forEach {
        ExternalDependenciesGenerator(symbolTable, listOf(irLinker)).generateUnboundSymbolsAsDependencies()
    }

    // Create stubs
    ExternalDependenciesGenerator(symbolTable, listOf(irLinker)).generateUnboundSymbolsAsDependencies()
    allModules.forEach { it.patchDeclarationParents() }

    irLinker.postProcess(inOrAfterLinkageStep = true)
    irLinker.checkNoUnboundSymbols(symbolTable, "at the end of IR linkage process")
    irLinker.clear()

    /*for (module in allModules)
        for (file in module.files)
            markExportedDeclarations(context, file, exportedDeclarations)*/

    val birModule = measureTimeMillisWithResult {
        converter.setExpectedTreeSize(400000)
        converter.convertIrTree(backendContext, allModules).first() as BirModuleFragment
    }.also {
        println("ir->bir in: ${it.first}ms")
    }.second
    backendContext.setModuleFragment(birModule)

    for (phase in phases) {
        val phaseObj = phase(backendContext)
        measureTimeMillis {
            phaseObj(birModule)
        }.also {
            println("Phase ${phaseObj.javaClass.simpleName} in ${it}ms")
        }
    }
}

private val phases = listOf(
    ::BirJsCodeCallsLowering,
    ::ExcludeDeclarationsFromCodegen,
    ::LateinitLowering,
    ::SharedVariablesLowering,
)