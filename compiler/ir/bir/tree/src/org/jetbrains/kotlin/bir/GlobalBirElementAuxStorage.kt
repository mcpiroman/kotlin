/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.declarations.BirMetadataSourceOwner
import org.jetbrains.kotlin.ir.declarations.MetadataSource

object GlobalBirElementAuxStorage {
    val Metadata = BirElementAuxStorageKey<BirMetadataSourceOwner, MetadataSource>()
}

object GlobalBirElementAuxStorageTokens {
    val manager = BirElementAuxStorageManager()

    val Metadata = manager.registerToken(GlobalBirElementAuxStorage.Metadata)
}