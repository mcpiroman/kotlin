/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirStatement
import org.jetbrains.kotlin.bir.backend.BirLoweringPhase
import org.jetbrains.kotlin.bir.backend.wasm.WasmBirContext
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.BirBody
import org.jetbrains.kotlin.bir.expressions.BirStatementContainer
import org.jetbrains.kotlin.bir.expressions.impl.BirCompositeImpl
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.declarations.*

context (WasmBirContext)
class LocalClassPopupLowering : BirLoweringPhase() {
    override fun invoke(module: BirModuleFragment) {
        getElementsOfClass<BirBody>().forEach {
            popupLocalClasses(it)
        }
    }
}

context (WasmBirContext)
fun popupLocalClasses(
    from: BirElement,
    shouldPopUp: (BirClass) -> Boolean = { it.isLocalNotInner() }
) {
    from.traverseStackBased { element ->
        if (element is BirClass) {
            if (shouldPopUp(element)) {
                var extractedUnder: BirStatement? = element
                var parent = element.parent
                var newContainer: BirDeclarationHost? = null
                while (parent is BirDeclaration) {
                    if (parent is BirDeclarationHost) {
                        newContainer = parent as BirDeclarationHost
                    }
                    if (parent is BirClass || parent is BirScript) {
                        break
                    }
                    extractedUnder = parent
                    parent = parent.parent
                }

                when (newContainer) {
                    is BirStatementContainer -> {
                        // TODO: check if it is the correct behavior
                        if (extractedUnder == element) {
                            extractedUnder = (newContainer.statements.indexOf(extractedUnder) + 1)
                                .takeIf { it > 0 && it < newContainer.statements.size }
                                ?.let { newContainer.statements.elementAt(it) }
                        }
                        extractLocalClass(element, newContainer, extractedUnder)
                    }
                    is BirDeclarationContainer -> extractLocalClass(element, newContainer, extractedUnder)
                    else -> error("Unexpected container type $newContainer")
                }

                element.replaceWith(BirCompositeImpl(element.sourceSpan, birBuiltIns.unitType, null))
            }
        } else {
            element.walkIntoChildren()
        }
    }
}

context (WasmBirContext)
private fun extractLocalClass(local: BirClass, newContainer: BirDeclarationHost, extractedUnder: BirStatement?) {
    when (newContainer) {
        is BirStatementContainer -> {
            val insertIndex = extractedUnder?.let { newContainer.statements.indexOf(it) } ?: -1
            if (insertIndex >= 0) {
                newContainer.statements.add(insertIndex, local)
            } else {
                newContainer.statements.add(local)
            }
        }
        is BirDeclarationContainer -> {
            newContainer.declarations += local
        }
        else -> error("Unexpected container type $newContainer")
    }
}

internal fun BirClass.isLocalNotInner(): Boolean = visibility == DescriptorVisibilities.LOCAL && !isInner