/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases.wasm

import org.jetbrains.kotlin.bir.backend.phases.DefaultInlineFunctionResolver
import org.jetbrains.kotlin.bir.backend.phases.FunctionInliningLowering
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext

context(WasmBirContext)
fun WasmFunctionInliningLowering() = FunctionInliningLowering(
    inlineFunctionResolver = DefaultInlineFunctionResolver(),
    innerClassesSupport = inlineClassSupport,
    insertAdditionalImplicitCasts = true,
)