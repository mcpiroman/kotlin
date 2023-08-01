/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementFeatureCacheCondition
import org.jetbrains.kotlin.bir.BirElementsWithFeatureCacheKey
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
        condition: BirElementFeatureCacheCondition,
    ): BirElementsWithFeatureCacheKey<BirElement> {
        val key = BirElementsWithFeatureCacheKey<BirElement>(includeOtherModules, condition)
        registerFeatureCacheSlot(key)
        return key
    }

    protected inline fun <reified E : BirElement> registerElementsWithFeatureCacheKey(
        includeOtherModules: Boolean,
        crossinline condition: (E) -> Boolean,
    ): BirElementsWithFeatureCacheKey<E> {
        val key = BirElementsWithFeatureCacheKey<E>(includeOtherModules) {
            it is E && condition(it)
        }
        registerFeatureCacheSlot(key)
        return key
    }

    protected inline fun <reified E : BirElement> registerElementsWithFeatureCacheKey(includeOtherModules: Boolean): BirElementsWithFeatureCacheKey<E> =
        registerElementsWithFeatureCacheKey<E>(includeOtherModules) { true }
}