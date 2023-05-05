/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.declarations.BirAttributeContainer
import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.declarations.BirMemberWithContainerSource
import org.jetbrains.kotlin.bir.declarations.BirMetadataSourceOwner
import org.jetbrains.kotlin.bir.symbols.BirClassSymbol
import org.jetbrains.kotlin.ir.declarations.MetadataSource
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

object GlobalBirElementAuxStorage {
    val Metadata = BirElementAuxStorageKey<BirMetadataSourceOwner, MetadataSource?>() // probably rename e.g. to 'source'
    val ContainerSource = BirElementAuxStorageKey<BirMemberWithContainerSource, DeserializedContainerSource?>()
    val SealedSubclasses = BirElementAuxStorageKey<BirClass, List<BirClassSymbol>>() // Seems only used in JVM
    val OriginalBeforeInline = BirElementAuxStorageKey<BirAttributeContainer, BirAttributeContainer?>() // Seems only used inside lowering
}

object GlobalBirElementAuxStorageTokens {
    val manager = BirElementAuxStorageManager()

    val Metadata = manager.registerToken(GlobalBirElementAuxStorage.Metadata)
    val ContainerSource = manager.registerToken(GlobalBirElementAuxStorage.ContainerSource)
    val SealedSubclasses = manager.registerToken(GlobalBirElementAuxStorage.SealedSubclasses)
    val OriginalBeforeInline = manager.registerToken(GlobalBirElementAuxStorage.OriginalBeforeInline)
}