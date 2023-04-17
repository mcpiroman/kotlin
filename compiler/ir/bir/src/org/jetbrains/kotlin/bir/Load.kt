/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import com.intellij.mock.MockProject
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.IrModuleInfo
import org.jetbrains.kotlin.ir.backend.js.MainModule
import org.jetbrains.kotlin.ir.backend.js.ModulesStructure
import org.jetbrains.kotlin.ir.backend.js.loadIr
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.js.config.EcmaVersion
import org.jetbrains.kotlin.js.config.ErrorTolerancePolicy
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.serialization.js.ModuleKind
import java.io.File

//data class prepareIrResult(val createContext: () -> JsIrBackendContext, val allModules: List<IrModuleFragment>)

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun prepareIr(mainModulePath: String): IrModuleInfo {
    val project = MockProject(null) {}

    val mainModule = MainModule.Klib(mainModulePath)
    val configuration = createConfig(
        listOf(mainModule.libPath).map { File(it).absolutePath },
        emptyList(),
        emptyList(),
        ErrorTolerancePolicy.NONE
    )
    val depsDescriptors = ModulesStructure(
        project,
        mainModule,
        configuration,
        listOf(mainModule.libPath),
        listOf()
    )

    val moduleInfo = loadIr(depsDescriptors, IrFactoryImpl, verifySignatures = false)
    return moduleInfo //prepareIrResult(createContext, allModules)
}

fun createConfig(
    dependencies: List<String>,
    allDependencies: List<String>,
    friends: List<String>,
    errorIgnorancePolicy: ErrorTolerancePolicy,
): CompilerConfiguration {
    val configuration = CompilerConfiguration()
    configuration.put(CommonConfigurationKeys.DISABLE_INLINE, false)

    configuration.put(JSConfigurationKeys.LIBRARIES, dependencies)
    configuration.put(JSConfigurationKeys.TRANSITIVE_LIBRARIES, allDependencies)
    configuration.put(JSConfigurationKeys.FRIEND_PATHS, friends)

    configuration.put(CommonConfigurationKeys.MODULE_NAME, "TODO stdlib")
    configuration.put(JSConfigurationKeys.MODULE_KIND, ModuleKind.PLAIN)
    configuration.put(JSConfigurationKeys.TARGET, EcmaVersion.v5)
    configuration.put(JSConfigurationKeys.ERROR_TOLERANCE_POLICY, errorIgnorancePolicy)

    if (errorIgnorancePolicy.allowErrors) {
        configuration.put(JSConfigurationKeys.DEVELOPER_MODE, true)
    }

    configuration.put(JSConfigurationKeys.META_INFO, false)
    configuration.put(JSConfigurationKeys.SOURCE_MAP, false)
    configuration.put(JSConfigurationKeys.TYPED_ARRAYS_ENABLED, true)
    configuration.put(JSConfigurationKeys.GENERATE_REGION_COMMENTS, true)
    configuration.put(CommonConfigurationKeys.EXPECT_ACTUAL_LINKER, false)
    return configuration
}
