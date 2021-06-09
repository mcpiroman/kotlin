/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js

import org.gradle.api.Named
import org.gradle.api.attributes.*
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import java.io.Serializable

// For Gradle attributes
@Suppress("EnumEntryName")
enum class KotlinJsCompilerAttribute : Named, Serializable {
    legacy,
    ir,
    both;

    override fun getName(): String =
        name

    override fun toString(): String =
        getName()

    companion object {
        val jsCompilerAttribute = Attribute.of(
            "org.jetbrains.kotlin.js.compiler",
            KotlinJsCompilerAttribute::class.java
        )

        fun setupAttributesMatchingStrategy(attributesSchema: AttributesSchema) {
            attributesSchema.attribute(jsCompilerAttribute).run {
                disambiguationRules.add(KotlinJsCompilerDisambiguation::class.java)
            }
        }
    }
}

class KotlinJsCompilerDisambiguation : AttributeDisambiguationRule<KotlinJsCompilerAttribute> {
    override fun execute(details: MultipleCandidatesDetails<KotlinJsCompilerAttribute?>) = with(details) {
        if (candidateValues == setOf(KotlinJsCompilerAttribute.legacy, KotlinJsCompilerAttribute.ir)) {
            closestMatch(KotlinJsCompilerAttribute.ir)
        }
    }
}