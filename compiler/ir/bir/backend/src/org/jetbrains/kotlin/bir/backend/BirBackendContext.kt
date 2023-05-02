/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend

import org.jetbrains.kotlin.bir.BirBuiltIns
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.types.BirTypeSystemContext
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.name.FqName

abstract class BirBackendContext : BirTreeContext() {
    abstract val builtIns: KotlinBuiltIns
    abstract val birBuiltIns: BirBuiltIns
    abstract val typeSystem: BirTypeSystemContext
    abstract val internalPackageFqn: FqName
    abstract val sharedVariablesManager: SharedVariablesManager

    abstract val configuration: CompilerConfiguration
}

