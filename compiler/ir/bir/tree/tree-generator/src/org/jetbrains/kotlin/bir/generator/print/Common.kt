/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.generator.print

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.bir.generator.util.GeneratedFile
import java.io.File

private val COPYRIGHT by lazy { File("license/COPYRIGHT_HEADER.txt").readText() }
private val GENERATED_MESSAGE = """
     // This file was generated automatically. See compiler/ir/bir/tree/tree-generator/ReadMe.md.
     // DO NOT MODIFY IT MANUALLY.
     """.trimIndent()
private val PREFIX by lazy { "$COPYRIGHT\n\n$GENERATED_MESSAGE\n\n" }

fun printTypeCommon(generationPath: File, packageName: String, type: TypeSpec): GeneratedFile {
    val code = FileSpec.builder(packageName, type.name!!)
        .indent("    ")
        .addType(type)
        .build()
        .toString()
        .unbacktickIdentifiers("data", "value", "operator", "constructor", "delegate", "receiver", "field", "impl")
        .replace("public ", "")
        .replace(":\\s*Unit".toRegex(), "")
        .replace("import kotlin\\..*\\n".toRegex(), "")
        .replace("KotlinArray<", "Array<")

    val text = PREFIX + code
    return GeneratedFile(getPathForFile(generationPath, packageName, type.name!!), text)
}

private fun String.unbacktickIdentifiers(vararg identifiers: String): String {
    var result = this
    for (identifier in identifiers) {
        result = result.replace("`$identifier`", identifier)
    }
    return result
}

fun getPathForFile(generationPath: File, packageName: String, typeName: String): File {
    val dir = generationPath.resolve(packageName.replace(".", "/"))
    return File(dir, "$typeName.kt")
}
