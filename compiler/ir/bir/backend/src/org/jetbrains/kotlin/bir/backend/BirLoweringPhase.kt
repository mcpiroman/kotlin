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

    protected inline fun <reified E : BirElement> registerElementsWithFeatureCacheKey(
        includeOtherModules: Boolean,
        crossinline condition: (E) -> Boolean,
    ): BirElementsWithFeatureCacheKey<E> =
        registerElementsWithFeatureCacheKey<E>(includeOtherModules, { element -> condition(element as E) }, E::class.java)

    protected inline fun <reified E : BirElement> registerElementsWithFeatureCacheKey(includeOtherModules: Boolean): BirElementsWithFeatureCacheKey<E> =
        registerElementsWithFeatureCacheKey<E>(includeOtherModules) { true }

    protected fun <E : BirElement> registerElementsWithFeatureCacheKey(
        includeOtherModules: Boolean,
        condition: BirElementFeatureCacheCondition,
        elementClass: Class<*>,
    ): BirElementsWithFeatureCacheKey<E> {
        val key = BirElementsWithFeatureCacheKey<E>(condition, elementClass, includeOtherModules)
        registerFeatureCacheSlot(key)
        return key
    }
}