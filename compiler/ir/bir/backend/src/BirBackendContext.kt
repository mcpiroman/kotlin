/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.Ir2BirConverter
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.types.IrTypeSystemContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.FqName

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

interface BirBackendContext {
    val builtIns: KotlinBuiltIns
    val birBuiltIns: BirBuiltIns
    val typeSystem: IrTypeSystemContext
    val internalPackageFqn: FqName

    val configuration: CompilerConfiguration
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
class WasmBirContext(
    override val builtIns: KotlinBuiltIns,
    irBuiltIns: IrBuiltIns,
    symbolTable: SymbolTable,
    module: ModuleDescriptor,
    override val configuration: CompilerConfiguration,
) : BirTreeContext(), BirBackendContext {
    private val converter = Ir2BirConverter()
    override val birBuiltIns: BirBuiltIns = BirBuiltIns(this, irBuiltIns, converter)
    val wasmSymbols = BirWasmSymbols(birBuiltIns, symbolTable, this, converter, module)
    override val internalPackageFqn = FqName("kotlin.wasm")
    override val typeSystem: IrTypeSystemContext = IrTypeSystemContextImpl(irBuiltIns)
}