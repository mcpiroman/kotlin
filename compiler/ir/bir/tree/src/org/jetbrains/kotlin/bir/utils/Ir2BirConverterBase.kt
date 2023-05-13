/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.utils

import com.intellij.util.containers.HashSetInterner
import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirMemberAccessExpression
import org.jetbrains.kotlin.bir.expressions.impl.BirNoExpressionImpl
import org.jetbrains.kotlin.bir.symbols.BirIrSymbolWrapper
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.types.*
import org.jetbrains.kotlin.bir.types.impl.BirCapturedType
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrCapturedType
import org.jetbrains.kotlin.ir.types.impl.IrDelegatedSimpleType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.IrTypeProjectionImpl
import java.util.*

@OptIn(ObsoleteDescriptorBasedAPI::class)
abstract class Ir2BirConverterBase {
    var copyDescriptors = false
    private val collectedBirElementsWithoutParent = mutableListOf<BirElement>()
    private val collectedIrElementsWithoutParent = mutableListOf<IrElement>()
    private var isInsideNestedElementCopy = false

    protected fun <Bir : BirElement, Ir : IrElement> createElementMap(expectedMaxSize: Int = 16): MutableMap<Ir, Bir> =
        IdentityHashMap<Ir, Bir>(expectedMaxSize)

    context(BirTreeContext)
    protected abstract fun <Bir : BirElement> copyElement(old: IrElement): Bir

    fun copyIrTree(treeContext: BirTreeContext, irRootElements: List<IrElement>): List<BirElement> {
        with(treeContext) {
            return irRootElements.map { copyElement(it) }
        }
    }

    fun copyIrTree(treeContext: BirTreeContext, irRootElement: IrElement): BirElement =
        copyIrTree(treeContext, listOf(irRootElement)).single()

    context(BirTreeContext)
    protected fun <Ir : IrElement, Bir : BirElement> copyNotReferencedElement(old: Ir, copy: () -> Bir): Bir {
        return doCopyElement(old, copy)
    }

    context(BirTreeContext)
    protected fun <Ir : IrElement, ME : BirElement, SE : ME> copyReferencedElement(
        old: Ir,
        map: MutableMap<Ir, ME>,
        copy: () -> SE,
        lateInitialize: (SE) -> Unit
    ): SE {
        map[old]?.let {
            return it as SE
        }

        return doCopyElement(old) {
            val new = copy()
            map[old] = new
            lateInitialize(new)
            new
        }
    }

    context(BirTreeContext)
    private fun <Ir : IrElement, Bir : BirElement> doCopyElement(old: Ir, copy: () -> Bir): Bir {
        val wasNested = isInsideNestedElementCopy
        isInsideNestedElementCopy = true
        val lastCollectedElementsWithoutParent = collectedBirElementsWithoutParent.size
        val new = copy()

        if (wasNested) {
            for (i in collectedBirElementsWithoutParent.lastIndex downTo lastCollectedElementsWithoutParent) {
                val bir = collectedBirElementsWithoutParent[i]
                if (bir.parent != null) {
                    collectedBirElementsWithoutParent.removeAt(i)
                    collectedIrElementsWithoutParent.removeAt(i)
                }
            }
        }

        if (new.parent == null) {
            if (old is IrDeclaration && old !is IrModuleFragment && old !is IrExternalPackageFragment || old is IrFile) {
                collectedBirElementsWithoutParent += new
                collectedIrElementsWithoutParent += old
            }
        }

        if (!wasNested) {
            while (true) {
                val bir = collectedBirElementsWithoutParent.removeLastOrNull() ?: break
                val ir = collectedIrElementsWithoutParent.removeLast()
                if (bir.parent == null) {
                    if (ir is IrDeclaration) {
                        remapElement<BirElement>(ir.parent)
                    } else if (ir is IrFile) {
                        remapElement<BirModuleFragment>(ir.module)
                    }
                }
            }
        }

        isInsideNestedElementCopy = wasNested

        return new
    }

    context(BirTreeContext)
    fun <Bir : BirElement> remapElement(old: IrElement): Bir = copyElement(old)

    context(BirTreeContext)
    fun <IrS : IrSymbol, BirS : BirSymbol> remapSymbol(old: IrS): BirS {
        return if (old.isBound) {
            remapElement(old.owner) as BirS
        } else {
            BirIrSymbolWrapper(old) as BirS
        }
    }

    context(BirTreeContext)
    protected fun BirAttributeContainer.copyAttributes(old: IrAttributeContainer) {
        val owner = old.attributeOwnerId
        attributeOwnerId = if (owner === old) this
        else remapElement(owner) as BirAttributeContainer
    }

    context(BirTreeContext)
    protected fun BirElement.copyAuxData(from: IrElement) {
        this as BirElementBase
        if (from is IrMetadataSourceOwner) {
            (this as BirMetadataSourceOwner)[GlobalBirElementAuxStorageTokens.Metadata] = from.metadata
        }

        if (from is IrMemberWithContainerSource) {
            (this as BirMemberWithContainerSource)[GlobalBirElementAuxStorageTokens.ContainerSource] = from.containerSource
        }

        if (from is IrAttributeContainer) {
            (this as BirAttributeContainer)[GlobalBirElementAuxStorageTokens.OriginalBeforeInline] =
                from.originalBeforeInline?.let { remapElement(it) as BirAttributeContainer }
        }

        if (from is IrClass) {
            (this as BirClass)[GlobalBirElementAuxStorageTokens.SealedSubclasses] = from.sealedSubclasses.map { remapSymbol(it) }
        }
    }

    context(BirTreeContext)
    protected fun <Ir : IrElement, Bir : BirElement> BirChildElementList<Bir>.copyElements(from: List<Ir>) {
        for (ir in from) {
            val bir = copyElement<Bir>(ir)
            this += bir
        }
    }

    context(BirTreeContext)
    protected fun BirMemberAccessExpression<*>.copyIrMemberAccessExpressionValueArguments(from: IrMemberAccessExpression<*>) {
        for (i in 0 until from.valueArgumentsCount) {
            val arg = from.getValueArgument(i)
            if (arg != null) {
                valueArguments += copyElement(arg) as BirExpression
            } else {
                valueArguments += BirNoExpressionImpl(SourceSpan.UNDEFINED, BirUninitializedType)
            }
        }
    }

    protected val IrMemberAccessExpression<*>.typeArguments: List<IrType?>
        get() = List(typeArgumentsCount) { getTypeArgument(it) }

    protected fun <D : DeclarationDescriptor> mapDescriptor(readDescriptor: () -> D): D? {
        return if (copyDescriptors) readDescriptor() else null
    }

    context(BirTreeContext)
    fun remapType(irType: IrType): BirType = when (irType) {
        // for IrDelegatedSimpleType, this egaerly initializes a lazy IrAnnotationType
        is IrSimpleTypeImpl, is IrDelegatedSimpleType -> remapSimpleType(irType as IrSimpleType)
        is IrCapturedType -> remapCapturedType(irType)
        is IrDynamicType -> remapDynamicType(irType)
        is IrErrorType -> remapErrorType(irType)
        else -> TODO(irType.toString())
    }

    private val simpleTypeIntern = HashSetInterner<BirSimpleType>()
    private val typeAbbreviationIntern = HashSetInterner<BirTypeAbbreviation>()
    private val dynamicTypeIntern = HashSetInterner<BirDynamicType>()
    private val capturedTypeIntern = HashSetInterner<BirCapturedType>()
    private val typeProjectionIntern = HashSetInterner<BirTypeProjection>()

    context(BirTreeContext)
    private fun remapSimpleType(irType: IrSimpleType): BirSimpleType {
        val birType = BirSimpleTypeImpl(
            irType.kotlinType,
            remapSymbol(irType.classifier),
            irType.nullability,
            irType.arguments.map { remapTypeArgument(it) },
            irType.annotations.map { remapElement(it) as BirConstructorCall },
            irType.abbreviation?.let { abbreviation ->
                remapTypeAbbreviation(abbreviation)
            },
        )
        return simpleTypeIntern.intern(birType)
    }

    context(BirTreeContext)
    private fun remapTypeAbbreviation(abbreviation: IrTypeAbbreviation): BirTypeAbbreviation {
        val birType = BirTypeAbbreviation(
            remapSymbol(abbreviation.typeAlias),
            abbreviation.hasQuestionMark,
            abbreviation.arguments.map { remapTypeArgument(it) },
            abbreviation.annotations.map { remapElement(it) as BirConstructorCall },
        )
        return typeAbbreviationIntern.intern(birType)
    }

    context(BirTreeContext)
    private fun remapCapturedType(irType: IrCapturedType): BirCapturedType {
        val birType = BirCapturedType(
            irType.captureStatus,
            irType.lowerType?.let { remapType(it) },
            remapTypeArgument(irType.constructor.argument),
            remapElement(irType.constructor.typeParameter) as BirTypeParameter,
        )
        return capturedTypeIntern.intern(birType)
    }

    context(BirTreeContext)
    private fun remapDynamicType(irType: IrDynamicType): BirDynamicType {
        val birType = BirDynamicType(
            irType.kotlinType,
            irType.annotations.map { remapElement(it) as BirConstructorCall },
            irType.variance,
        )
        return dynamicTypeIntern.intern(birType)
    }

    context(BirTreeContext)
    private fun Ir2BirConverterBase.remapErrorType(irType: IrErrorType) =
        BirErrorType(
            irType.kotlinType,
            irType.annotations.map { remapElement(it) as BirConstructorCall },
            irType.variance,
            irType.isMarkedNullable,
        )

    context(BirTreeContext)
    fun remapTypeArgument(irTypeArgument: IrTypeArgument): BirTypeArgument = when (irTypeArgument) {
        is IrStarProjection -> BirStarProjection
        is IrTypeProjectionImpl -> {
            val birType = BirTypeProjectionImpl(remapType(irTypeArgument.type), irTypeArgument.variance)
            typeProjectionIntern.intern(birType)
        }
        is IrType -> remapType(irTypeArgument) as BirTypeArgument
        else -> error(irTypeArgument)
    }

    companion object {
        fun IrElement.convertToBir(treeContext: BirTreeContext): BirElement {
            val converter = Ir2BirConverter()
            return converter.copyIrTree(treeContext, listOf(this)).single()
        }
    }
}