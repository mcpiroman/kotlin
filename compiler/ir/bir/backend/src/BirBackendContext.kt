/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import org.jetbrains.kotlin.backend.wasm.WasmSymbols
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.types.IrTypeSystemContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.name.FqName

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

interface BirBackendContext {
    val builtIns: KotlinBuiltIns
    val irBuiltIns: IrBuiltIns
    val typeSystem: IrTypeSystemContext
    val internalPackageFqn: FqName

    val configuration: CompilerConfiguration
}

class WasmBirContext(
    override val builtIns: KotlinBuiltIns,
    override val irBuiltIns: IrBuiltIns,
    override val configuration: CompilerConfiguration,
    val wasmSymbols: WasmSymbols
) : BirTreeContext(), BirBackendContext {
    override val internalPackageFqn = FqName("kotlin.wasm")
    override val typeSystem: IrTypeSystemContext = IrTypeSystemContextImpl(irBuiltIns)
}