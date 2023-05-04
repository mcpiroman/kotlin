/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.wasm

import org.jetbrains.kotlin.bir.BirBuiltIns
import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.backend.BirBackendContext
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment
import org.jetbrains.kotlin.bir.declarations.BirPackageFragment
import org.jetbrains.kotlin.bir.declarations.impl.BirExternalPackageFragmentImpl
import org.jetbrains.kotlin.bir.declarations.impl.BirFileImpl
import org.jetbrains.kotlin.bir.types.BirTypeSystemContext
import org.jetbrains.kotlin.bir.types.BirTypeSystemContextImpl
import org.jetbrains.kotlin.bir.utils.Ir2BirConverter
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.EmptyPackageFragmentDescriptor
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(ObsoleteDescriptorBasedAPI::class)
class WasmBirContext(
    override val builtIns: KotlinBuiltIns,
    irBuiltIns: IrBuiltIns,
    symbolTable: SymbolTable,
    module: ModuleDescriptor,
    override val configuration: CompilerConfiguration,
    converter: Ir2BirConverter
) : BirBackendContext() {
    override val birBuiltIns: BirBuiltIns = BirBuiltIns(irBuiltIns, converter)
    val wasmSymbols = BirWasmSymbols(birBuiltIns, symbolTable, this, converter, module)
    override val typeSystem: BirTypeSystemContext = BirTypeSystemContextImpl(birBuiltIns, this)
    lateinit var birModuleFragment: BirModuleFragment
        private set

    // Place to store declarations excluded from code generation
    private val excludedDeclarations = mutableMapOf<FqName, BirPackageFragment>()

    fun getExcludedPackageFragment(fqName: FqName): BirPackageFragment = excludedDeclarations.getOrPut(fqName) {
        BirExternalPackageFragmentImpl(
            SourceSpan.UNDEFINED,
            null,
            fqName,
            null
        )
    }

    fun setModuleFragment(birModuleFragment: BirModuleFragment) {
        this.birModuleFragment = birModuleFragment
        sharedVariablesManager = WasmSharedVariablesManager(internalPackageFragment)
    }

    override val internalPackageFqn = FqName("kotlin.wasm")
    val kotlinWasmInternalPackageFqn = internalPackageFqn.child(Name.identifier("internal"))
    private val internalPackageFragmentDescriptor = EmptyPackageFragmentDescriptor(builtIns.builtInsModule, kotlinWasmInternalPackageFqn)

    // TODO: Merge with JS IR Backend context lazy file
    val internalPackageFragment by lazy {
        BirFileImpl(
            sourceSpan = SourceSpan.UNDEFINED,
            _descriptor = internalPackageFragmentDescriptor,
            fqName = internalPackageFragmentDescriptor.fqName,
            annotations = emptyList(),
            module = birModuleFragment,
            fileEntry = object : IrFileEntry {
                override val name = "<implicitDeclarations>"
                override val maxOffset = UNDEFINED_OFFSET

                override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int) =
                    SourceRangeInfo(
                        "",
                        UNDEFINED_OFFSET,
                        UNDEFINED_LINE_NUMBER,
                        UNDEFINED_COLUMN_NUMBER,
                        UNDEFINED_OFFSET,
                        UNDEFINED_LINE_NUMBER,
                        UNDEFINED_COLUMN_NUMBER
                    )

                override fun getLineNumber(offset: Int) = UNDEFINED_LINE_NUMBER
                override fun getColumnNumber(offset: Int) = UNDEFINED_COLUMN_NUMBER
            }
        ).also {
            birModuleFragment.files += it
        }
    }

    override lateinit var sharedVariablesManager: WasmSharedVariablesManager
        private set
}