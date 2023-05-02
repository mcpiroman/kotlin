/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.phases.wasm

import org.jetbrains.kotlin.bir.BirLoweringPhase
import org.jetbrains.kotlin.bir.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.utils.hasAnnotation
import org.jetbrains.kotlin.name.FqName

context(WasmBirContext)
class ExcludeDeclarationsFromCodegen : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        for (file in module.files) {
            val it = file.declarations.mutableIterator()
            while (it.hasNext()) {
                val d = it.next() as? BirDeclarationWithName ?: continue
                if (isExcluded(d)) {
                    it.remove()
                    // Move to "excluded" package fragment preserving fq-name
                    getExcludedPackageFragment(file.fqName).declarations += d
                }
            }
        }
    }

    private fun isExcluded(declaration: BirDeclaration): Boolean {
        // Annotation can be applied to top-level declarations ...
        if (declaration.hasExcludedFromCodegenAnnotation())
            return true

        // ... or files as a whole
        val parentFile = declaration.parent as? BirFile
        return parentFile?.hasExcludedFromCodegenAnnotation() == true
    }

    private fun BirAnnotationContainerElement.hasExcludedFromCodegenAnnotation(): Boolean =
        hasAnnotation(FqName("kotlin.wasm.internal.ExcludedFromCodegen"))
}