/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

class ElementsWithFeatureCacheKey<E : BirElement>(
    val includeOtherModules: Boolean, // todo: use it
    val condition: ElementFeatureCacheCondition,
) {
    internal var index = -1
}


fun interface ElementFeatureCacheCondition {
    fun matches(element: BirElement): Boolean
}
