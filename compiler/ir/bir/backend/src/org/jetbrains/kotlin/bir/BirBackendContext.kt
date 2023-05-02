/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import org.jetbrains.kotlin.bir.declarations.BirPackageFragment
import org.jetbrains.kotlin.bir.declarations.impl.BirExternalPackageFragmentImpl
import org.jetbrains.kotlin.bir.types.BirTypeSystemContext
import org.jetbrains.kotlin.bir.types.BirTypeSystemContextImpl
import org.jetbrains.kotlin.bir.utils.Ir2BirConverter
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.FqName

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

abstract class BirBackendContext : BirTreeContext() {
    abstract val builtIns: KotlinBuiltIns
    abstract val birBuiltIns: BirBuiltIns
    abstract val typeSystem: BirTypeSystemContext
    abstract val internalPackageFqn: FqName

    abstract val configuration: CompilerConfiguration
}

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
    override val internalPackageFqn = FqName("kotlin.wasm")
    override val typeSystem: BirTypeSystemContext = BirTypeSystemContextImpl(birBuiltIns, this)

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

    init {
        converter.finalizeTreeConversion(this)
    }
}