/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.backend.common.linkage.issues.checkNoUnboundSymbols
import org.jetbrains.kotlin.backend.common.phaser.AbstractNamedCompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.common.phaser.toPhaseMap
import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.backend.wasm.wasmPhases
import org.jetbrains.kotlin.bir.backend.phases.LateinitLowering
import org.jetbrains.kotlin.bir.backend.phases.SharedVariablesLowering
import org.jetbrains.kotlin.bir.backend.phases.wasm.BirJsCodeCallsLowering
import org.jetbrains.kotlin.bir.backend.phases.wasm.ExcludeDeclarationsFromCodegen
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.utils.Ir2BirConverter
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.IrModuleInfo
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.ExternalDependenciesGenerator
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.psi2ir.descriptors.IrBuiltInsOverDescriptors
import org.jetbrains.kotlin.utils.addToStdlib.measureTimeMillisWithResult

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun createBirBackendContext(moduleInfo: IrModuleInfo, configuration: CompilerConfiguration, converter: Ir2BirConverter) =
    WasmBirContext(
        (moduleInfo.bultins as IrBuiltInsOverDescriptors).builtIns,
        moduleInfo.bultins,
        moduleInfo.symbolTable,
        moduleInfo.module.descriptor,
        configuration,
        converter,
    )

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun prepareIrForCompilationCommon(moduleInfo: IrModuleInfo) {
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
}

fun runBirCompilation(backendContext: WasmBirContext, birModule: BirModuleFragment, showTime: Boolean) {
    for (phase in birPhases) {
        val phaseObj = phase(backendContext)
        maybeShowPhaseTime(showTime) {
            phaseObj(birModule)
            phaseObj.javaClass.simpleName
        }
    }
}

private val birPhases = listOf(
    ::BirJsCodeCallsLowering,
    ::ExcludeDeclarationsFromCodegen,
    ::LateinitLowering,
    ::SharedVariablesLowering,
)

private val correspondingIrPhaseNames = setOf(
    "JsCodeCallsLowering",
    "ExcludeDeclarationsFromCodegen",
    "LateinitNullableFields",
    "LateinitDeclarations",
    "LateinitUsage",
    "SharedVariablesLowering"
)
private val correspondingIrPhases = wasmPhases.toPhaseMap()
    .filterValues { it.name in correspondingIrPhaseNames }.values.map { it as AbstractNamedCompilerPhase<WasmBackendContext, Any?, Any?> }

fun prepareCorrespondingIrPhasesCompilation(
    moduleFragment: IrModuleFragment,
    moduleInfo: IrModuleInfo,
    configuration: CompilerConfiguration
): IrPhasesCompilationSetup {
    val (_, dependencyModules, irBuiltIns, symbolTable, irLinker) = moduleInfo
    val moduleDescriptor = moduleFragment.descriptor
    val allModules = dependencyModules
    val context = WasmBackendContext(moduleDescriptor, irBuiltIns, symbolTable, moduleFragment, true, configuration)

    val phaseConfig = PhaseConfig(wasmPhases)

    return IrPhasesCompilationSetup(allModules, context, phaseConfig)
}

class IrPhasesCompilationSetup(
    val allModules: List<IrModuleFragment>,
    val context: WasmBackendContext,
    val phaseConfig: PhaseConfig,
)

fun IrPhasesCompilationSetup.runCorrespondingIrPhases(showTime: Boolean) {
    //val topPhase = correspondingIrPhases.reduce { acc, new -> (acc then new) }
    //topPhase.invokeToplevel(phaseConfig, context, allModules)

    val phaseState = PhaserState<Any?>()
    for (phase in correspondingIrPhases) {
        maybeShowPhaseTime(showTime) {
            phase.invoke(phaseConfig, phaseState, context, allModules)
            phase.name
        }
    }
}

private fun maybeShowPhaseTime(showTime: Boolean, block: () -> String) {
    if (showTime) {
        measureTimeMillisWithResult {
            block()
        }.also {
            println("Phase ${it.second} in ${it.first}ms")
        }
    } else {
        block()
    }
}