/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.ElementFeatureCacheCondition
import org.jetbrains.kotlin.bir.ElementsWithFeatureCacheKey
import org.jetbrains.kotlin.bir.declarations.BirModuleFragment

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

context(BirBackendContext)
abstract class BirLoweringPhase {
    abstract operator fun invoke(module: BirModuleFragment)

    protected fun registerElementsWithFeatureCacheKey(
        includeOtherModules: Boolean,
        condition: ElementFeatureCacheCondition,
    ): ElementsWithFeatureCacheKey<BirElement> {
        val key = ElementsWithFeatureCacheKey<BirElement>(includeOtherModules, condition)
        registerFeatureCacheSlot(key)
        return key
    }

    protected inline fun <reified E : BirElement> registerElementsWithFeatureCacheKey(
        includeOtherModules: Boolean,
        crossinline condition: (E) -> Boolean,
    ): ElementsWithFeatureCacheKey<E> {
        val key = ElementsWithFeatureCacheKey<E>(includeOtherModules) {
            it is E && condition(it)
        }
        registerFeatureCacheSlot(key)
        return key
    }

    protected inline fun <reified E : BirElement> registerElementsWithFeatureCacheKey(includeOtherModules: Boolean): ElementsWithFeatureCacheKey<E> =
        registerElementsWithFeatureCacheKey<E>(includeOtherModules) { true }
}