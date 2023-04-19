/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.generator.config

import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.bir.generator.BASE_PACKAGE
import org.jetbrains.kotlin.bir.generator.util.*

class Config(
    val elements: List<ElementConfig>,
    val rootElement: ElementConfig,
)

class ElementConfig(
    val propertyName: String,
    val name: String,
    val category: Category
) : ElementConfigOrRef {
    val params = mutableListOf<TypeVariable>()
    val parents = mutableListOf<TypeRef>()
    val fields = mutableListOf<FieldConfig>()
    var childrenOrderOverride: List<String>? = null

    var typeKind: TypeKind? = null
    var symbol: ClassRef<PositionTypeParameterRef>? = null
    var hasImpl = false // By default, all and only leaf elements have impls

    var generationCallback: (TypeSpec.Builder.() -> Unit)? = null
    var suppressPrint = false
    var kDoc: String? = null

    override val element get() = this
    override val args get() = emptyMap<NamedTypeParameterRef, TypeRef>()
    override val nullable get() = false
    override fun copy(args: Map<NamedTypeParameterRef, TypeRef>) = ElementConfigRef(this, args, false)
    override fun copy(nullable: Boolean) = ElementConfigRef(this, args, nullable)

    operator fun TypeVariable.unaryPlus() = apply {
        params.add(this)
    }

    operator fun FieldConfig.unaryPlus() = apply {
        fields.add(this)
    }

    override fun toString() = element.name

    enum class Category(private val packageDir: String, val defaultVisitorParam: String) {
        Expression("expressions", "expression"),
        Declaration("declarations", "declaration"),
        Other("", "element");

        val packageName: String get() = BASE_PACKAGE + if (packageDir.isNotEmpty()) ".$packageDir" else ""
    }
}

sealed interface ElementConfigOrRef : ParametrizedTypeRef<ElementConfigOrRef, NamedTypeParameterRef>, TypeRefWithNullability {
    val element: ElementConfig
}

class ElementConfigRef(
    override val element: ElementConfig,
    override val args: Map<NamedTypeParameterRef, TypeRef>,
    override val nullable: Boolean
) : ElementConfigOrRef {
    override fun copy(args: Map<NamedTypeParameterRef, TypeRef>) = ElementConfigRef(element, args, nullable)
    override fun copy(nullable: Boolean) = ElementConfigRef(element, args, nullable)

    override fun toString() = element.name
}

sealed class FieldConfig(
    val name: String,
    val isChild: Boolean,
    val trackRef: Boolean,
) {
    var printProperty = true
    var generationCallback: (PropertySpec.Builder.() -> Unit)? = null
    var kdoc: String? = null

    override fun toString() = name
}

class SimpleFieldConfig(
    name: String,
    val type: TypeRef?,
    val nullable: Boolean,
    val mutable: Boolean,
    isChildElement: Boolean,
    trackRef: Boolean,
) : FieldConfig(name, isChildElement, trackRef) {
    var initializeToThis = false
}

class ListFieldConfig(
    name: String,
    val elementType: TypeRef?,
    val nullable: Boolean,
    val mutability: Mutability,
    isChildElement: Boolean,
) : FieldConfig(name, isChildElement, false) {
    enum class Mutability {
        Immutable,
        Var,
        List,
        Array
    }
}
