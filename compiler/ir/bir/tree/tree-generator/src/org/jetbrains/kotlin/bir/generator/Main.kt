/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.generator

import org.jetbrains.kotlin.generators.util.GeneratorsFileUtil
import org.jetbrains.kotlin.generators.util.GeneratorsFileUtil.collectPreviouslyGeneratedFiles
import org.jetbrains.kotlin.generators.util.GeneratorsFileUtil.removeExtraFilesFromPreviousGeneration
import org.jetbrains.kotlin.bir.generator.model.config2model
import org.jetbrains.kotlin.bir.generator.print.printElementImpls
import org.jetbrains.kotlin.bir.generator.print.printElements
import java.io.File

const val BASE_PACKAGE = "org.jetbrains.kotlin.bir"

fun main(args: Array<String>) {
    val generationPath = args.firstOrNull()?.let { File(it) }
        ?: File("compiler/ir/bir/tree/gen").canonicalFile

    val config = org.jetbrains.kotlin.bir.generator.BirTree.build()
    val model = config2model(config)

    val previouslyGeneratedFiles = collectPreviouslyGeneratedFiles(generationPath)
    val generatedFiles = sequence {
        yieldAll(printElements(generationPath, model))
        yieldAll(printElementImpls(generationPath, model))
        // IrElementTransformerVoid is too random to autogenerate
    }.map {
        GeneratorsFileUtil.writeFileIfContentChanged(it.file, it.newText, logNotChanged = false)
        it.file
    }.toList()
    removeExtraFilesFromPreviousGeneration(previouslyGeneratedFiles, generatedFiles)
}
