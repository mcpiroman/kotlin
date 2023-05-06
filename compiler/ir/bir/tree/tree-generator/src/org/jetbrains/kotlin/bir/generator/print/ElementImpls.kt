/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.generator.print

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.kotlin.bir.generator.*
import org.jetbrains.kotlin.bir.generator.model.*
import org.jetbrains.kotlin.bir.generator.util.ClassRef
import org.jetbrains.kotlin.bir.generator.util.PositionTypeParameterRef
import org.jetbrains.kotlin.bir.generator.util.TypeRef
import java.io.File

@OptIn(ExperimentalKotlinPoetApi::class)
fun printElementImpls(generationPath: File, model: Model) = sequence {
    for (element in model.elements.filter { it.hasImpl }) {
        val elementType = TypeSpec.classBuilder(element.elementImplName).apply {
            addTypeVariables(element.params.map { it.toPoet() })

            if (element.kind == Element.Kind.Interface || element.kind == Element.Kind.SealedInterface) {
                superclass(org.jetbrains.kotlin.bir.generator.elementBaseType.toPoet())
                addSuperinterface(element.toPoetSelfParameterized())
            } else {
                superclass(element.toPoetSelfParameterized())
            }

            if (element.hasTrackedBackReferences) {
                val type = ClassName(Packages.tree, "BirBackReferenceCollectionArrayStyleImpl")
                addProperty(
                    PropertySpec.builder("_referencedBy", type)
                        .mutable(true)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer(type.simpleName + "()")
                        .build()
                )
            }

            val ctor = FunSpec.constructorBuilder()

            val allFields = element.allFields
            allFields.forEachIndexed { fieldIndex, field ->
                val poetType = field.type.toPoet().copy(nullable = field.nullable)

                if (field.passViaConstructorParameter) {
                    ctor.addParameter(field.name, poetType)
                }

                addProperty(PropertySpec.builder(field.name, poetType).apply {
                    mutable(field.mutable)
                    addModifiers(KModifier.OVERRIDE)

                    if (field.needsDescriptorApiAnnotation) {
                        addAnnotation(
                            AnnotationSpec
                                .builder(descriptorApiAnnotation)
                                .useSiteTarget(AnnotationSpec.UseSiteTarget.PROPERTY)
                                .build()
                        )
                    }

                    if (field.defaultToThis) {
                        initializer("this")
                    } else if (field.passViaConstructorParameter) {
                        if (field is SingleField && field.isChild && field.mutable) {
                            addProperty(
                                PropertySpec.builder(field.backingFieldName, poetType)
                                    .mutable(true)
                                    .addModifiers(KModifier.PRIVATE)
                                    .initializer(field.name)
                                    .build()
                            )
                            getter(
                                FunSpec.getterBuilder()
                                    .addCode("return ${field.backingFieldName}")
                                    .build()
                            )
                            contextReceivers(treeContext.toPoet())
                        } else {
                            initializer(field.name)
                        }
                    } else if (field is ListField && field.isChild) {
                        initializer("BirChildElementList(this)")
                    }

                    if (field is SingleField && field.isChild && field.mutable) {
                        val prevChildSelectCode = codeToSelectFirstChild(allFields.subList(0, fieldIndex).asReversed(), false, true)
                        setter(
                            FunSpec.setterBuilder()
                                .addParameter(ParameterSpec("value", poetType))
                                .addCode("setChildField(${field.backingFieldName}, value, $prevChildSelectCode)\n")
                                .addCode("${field.backingFieldName} = value")
                                .build()
                        )
                    } else if (field.trackRef) {
                        setter(
                            FunSpec.setterBuilder()
                                .addParameter(ParameterSpec("value", poetType))
                                .addCode("setTrackedElementReference(field, value, %L)\n", field.trackedRefIndex)
                                .addCode("field = value")
                                .build()
                        )
                    }
                }.build())
            }


            if (allFields.any { it.needsDescriptorApiAnnotation }) {
                ctor.addAnnotation(descriptorApiAnnotation)
            }

            val allChildren = allFields.filter { it.isChild }
            allChildren.forEachIndexed { fieldIndex, child ->
                if (child is SingleField) {
                    val prevChildSelectCode = codeToSelectFirstChild(allChildren.subList(0, fieldIndex).asReversed(), false, false)
                    ctor.addCode("initChildField(${child.backingFieldName}, $prevChildSelectCode)\n")
                }
            }

            primaryConstructor(ctor.build())

            codeToSelectFirstChild(allChildren, true, false).takeUnless { it == "null" }?.let { code ->
                addFunction(
                    FunSpec
                        .builder("getFirstChild")
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode("return $code")
                        .returns(org.jetbrains.kotlin.bir.generator.rootElement.copy(nullable = true).toPoet())
                        .build()
                )
            }

            if (allChildren.isNotEmpty()) {
                addFunction(
                    FunSpec
                        .builder("getChildren")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter(
                            "children",
                            ARRAY.parameterizedBy(org.jetbrains.kotlin.bir.generator.elementOrList.copy(nullable = true).toPoet())
                        )
                        .apply {
                            allChildren.forEachIndexed { index, child ->
                                addCode("children[%L] = this.${if (child is SingleField) child.backingFieldName else child.name}\n", index)
                            }
                            addCode("return %L", allChildren.size)
                        }
                        .returns(INT)
                        .build()
                )

                addFunction(
                    FunSpec
                        .builder("acceptChildren")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("visitor", org.jetbrains.kotlin.bir.generator.elementVisitor.toPoet())
                        .apply {
                            allChildren.forEach { child ->
                                addCode("this.${if (child is SingleField) child.backingFieldName else child.name}")
                                when (child) {
                                    is SingleField -> {
                                        if (child.nullable) addCode("?")
                                        addCode(".%M(visitor)\n", elementAccept)
                                    }
                                    is ListField -> {
                                        addCode(".acceptChildren(visitor)\n")
                                    }
                                }
                            }
                        }
                        .build()
                )

                addFunction(
                    FunSpec
                        .builder("replaceChildProperty")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("old", rootElement.toPoet())
                        .addParameter("new", rootElement.toPoet().copy(nullable = true))
                        .contextReceivers(treeContext.toPoet())
                        .apply {
                            addCode("when {\n")
                            allChildren.forEach { field ->
                                if (field is SingleField) {
                                    addCode(
                                        "   this.%N === old -> this.%N = new as %T\n",
                                        field.backingFieldName, field.name, field.type.toPoet()
                                    )
                                }
                            }
                            addCode("   else -> throwChildForReplacementNotFound(old)\n")
                            addCode("}\n")
                        }
                        .build()
                )
            }

            val symbolFields = allFields.mapNotNull { field ->
                fun typeIfSymbol(type: TypeRef) =
                    if (type is ClassRef<*> && type.simpleName.startsWith("Bir") && type.simpleName.endsWith("Symbol")) type else null

                val type = field.type
                @Suppress("UNCHECKED_CAST")
                when (field) {
                    is SingleField -> typeIfSymbol(type)
                    is ListField -> typeIfSymbol((type as ClassRef<PositionTypeParameterRef>).args[PositionTypeParameterRef(0)]!!)
                }?.let { field to it }
            }
            if (symbolFields.isNotEmpty()) {
                addFunction(
                    FunSpec
                        .builder("replaceSymbolProperty")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("old", symbolType.toPoet())
                        .addParameter("new", symbolType.toPoet())
                        .apply {
                            symbolFields.forEach { (field, symbolType) ->
                                when (field) {
                                    is SingleField -> {
                                        addCode(
                                            "if(this.%N === old) this.%N = new as %T\n",
                                            field.name, field.name, symbolType.toPoet()
                                        )
                                    }
                                    is ListField -> {
                                        addCode("this.%N = this.%N", field.name, field.name)
                                        if (field.nullable) addCode("?")
                                        addCode(".map { if(it === old) new as %T else it }\n", symbolType.toPoet())
                                    }
                                }
                            }
                        }
                        .build()
                )
            }

            val trackedRefs = allFields.filter { it.trackRef }
            if (trackedRefs.isNotEmpty()) {
                addFunction(
                    FunSpec
                        .builder("registerTrackedBackReferences")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("unregisterFrom", elementBaseType.toPoet().copy(nullable = true))
                        .apply {
                            trackedRefs.forEach { field ->
                                addCode("registerTrackedBackReferenceTo(${field.name}, %L, unregisterFrom)\n", field.trackedRefIndex)
                            }
                        }
                        .build()
                )
            }
        }.build()

        yield(printTypeCommon(generationPath, element.elementImplName.packageName, elementType))
    }
}

private fun codeToSelectFirstChild(fields: List<Field>, selectFirstFromList: Boolean, prefixFieldAccessWithThis: Boolean): String {
    val prevChildren = buildList {
        for (f in fields) {
            if (f.isChild) {
                add(f)
                if (f is SingleField && !f.nullable || f is ListField && !selectFirstFromList) break
            }
        }
    }
    return if (prevChildren.isEmpty()) "null"
    else prevChildren.joinToString(" ?: ") {
        (if (prefixFieldAccessWithThis) "this." else "") +
                when (it) {
                    is SingleField -> it.backingFieldName
                    is ListField -> {
                        if (selectFirstFromList) "${it.name}.firstOrNull()"
                        else it.name
                    }
                }
    }
}

private val descriptorApiAnnotation = ClassName("org.jetbrains.kotlin.ir", "ObsoleteDescriptorBasedAPI")
private val elementAccept = MemberName(Packages.tree + ".traversal", "accept", true)
