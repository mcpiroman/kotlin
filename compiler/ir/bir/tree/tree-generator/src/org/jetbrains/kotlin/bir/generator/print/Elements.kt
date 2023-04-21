/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.generator.print

import com.squareup.kotlinpoet.*
import org.jetbrains.kotlin.bir.generator.model.Element
import org.jetbrains.kotlin.bir.generator.model.Model
import org.jetbrains.kotlin.bir.generator.model.SingleField
import org.jetbrains.kotlin.bir.generator.treeContext
import org.jetbrains.kotlin.bir.generator.util.TypeKind
import java.io.File

@OptIn(ExperimentalKotlinPoetApi::class)
fun printElements(generationPath: File, model: Model) = sequence {
    for (element in model.elements) {
        if (element == model.rootElement) continue

        val elementName = element.toPoet()
        val elementType = when (element.kind?.typeKind) {
            null -> error("Element's category not configured")
            TypeKind.Class -> TypeSpec.classBuilder(elementName)
            TypeKind.Interface -> TypeSpec.interfaceBuilder(elementName)
        }.apply {
            addModifiers(
                when (element.kind) {
                    Element.Kind.SealedClass -> listOf(KModifier.SEALED)
                    Element.Kind.SealedInterface -> listOf(KModifier.SEALED)
                    Element.Kind.AbstractClass -> listOf(KModifier.ABSTRACT)
                    Element.Kind.FinalClass -> listOf(KModifier.FINAL)
                    Element.Kind.OpenClass -> listOf(KModifier.OPEN)
                    else -> emptyList()
                }
            )
            addTypeVariables(element.params.map { it.toPoet() })

            val (classes, interfaces) = element.allParents.partition { it.typeKind == TypeKind.Class }
            classes.singleOrNull()?.let {
                superclass(it.toPoet())
            }
            addSuperinterfaces(interfaces.map { it.toPoet() })

            element.fields.forEach { field ->
                if (!field.printProperty) return@forEach
                val poetType = field.type.toPoet().copy(nullable = field.nullable)
                addProperty(PropertySpec.builder(field.name, poetType).apply {
                    mutable(field.mutable)
                    if (field.isOverride) {
                        addModifiers(KModifier.OVERRIDE)
                    }
                    addModifiers(KModifier.ABSTRACT)

                    if (field.needsDescriptorApiAnnotation) {
                        addAnnotation(descriptorApiAnnotation)
                    }

                    if (field is SingleField && field.isChild && field.mutable) {
                        addAnnotation(treeContext.toPoet() as ClassName)
                    }

                    field.generationCallback?.invoke(this)
                }.build())
            }

            generateElementKDoc(element)
        }.build()

        yield(printTypeCommon(generationPath, elementName.packageName, elementType))
    }
}

private fun TypeSpec.Builder.generateElementKDoc(element: Element) {
    addKdoc(buildString {
        if (element.kDoc != null) {
            appendLine(element.kDoc)
        } else {
            append("A ")
            append(if (element.isLeaf) "leaf" else "non-leaf")
            appendLine("B IR tree element.")
        }

        append("\nGenerated from: [${element.propertyName}]")
    })
}

private val descriptorApiAnnotation = ClassName("org.jetbrains.kotlin.ir", "ObsoleteDescriptorBasedAPI")
