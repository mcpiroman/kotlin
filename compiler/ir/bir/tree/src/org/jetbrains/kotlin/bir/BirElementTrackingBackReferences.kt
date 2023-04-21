/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

//@RequiresOptIn
//annotation class InternalBirApi

interface BirElementTrackingBackReferences : BirElement {
    //@set:InternalBirApi
    var _referencedBy: BirBackReferenceCollectionArrayStyleImpl

    val referencedBy: BirBackReferenceCollection
        get() = BirBackReferenceCollection(this)
}