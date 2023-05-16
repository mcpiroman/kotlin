/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.symbols

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.util.IdSignature

interface BirSymbolElement : BirLLPossiblyElementSymbol, BirElement {
    override val signature: IdSignature?
        get() = null

    @ObsoleteDescriptorBasedAPI
    val _descriptor: DeclarationDescriptor?
}