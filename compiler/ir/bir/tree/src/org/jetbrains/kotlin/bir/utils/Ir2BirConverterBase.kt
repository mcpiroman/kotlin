/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.utils

import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirMemberAccessExpression
import org.jetbrains.kotlin.bir.expressions.impl.BirNoExpressionImpl
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.symbols.ExternalBirSymbol
import org.jetbrains.kotlin.bir.types.*
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.*
import java.util.*

@OptIn(ObsoleteDescriptorBasedAPI::class)
abstract class Ir2BirConverterBase {
    var copyDescriptors = false
    private val collectedBirElementsWithoutParent = mutableListOf<BirElement>()
    private val collectedIrElementsWithoutParent = mutableListOf<IrElement>()
    private var isInsideNestedElementCopy = false

    protected fun <Bir : BirElement, Ir : IrElement> createElementMap(expectedMaxSize: Int = 8): MutableMap<Ir, Bir> =
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
            val signature = IrErrorClassImpl.symbol.signature
            when (IrErrorClassImpl.symbol) {
                is IrFileSymbol -> ExternalBirSymbol.FileSymbol(signature)
                is IrExternalPackageFragmentSymbol -> ExternalBirSymbol.ExternalPackageFragmentSymbol(signature)
                is IrAnonymousInitializerSymbol -> ExternalBirSymbol.AnonymousInitializerSymbol(signature)
                is IrEnumEntrySymbol -> ExternalBirSymbol.EnumEntrySymbol(signature)
                is IrFieldSymbol -> ExternalBirSymbol.FieldSymbol(signature)
                is IrClassSymbol -> ExternalBirSymbol.ClassSymbol(signature)
                is IrScriptSymbol -> ExternalBirSymbol.ScriptSymbol(signature)
                is IrTypeParameterSymbol -> ExternalBirSymbol.TypeParameterSymbol(signature)
                is IrValueParameterSymbol -> ExternalBirSymbol.ValueParameterSymbol(signature)
                is IrVariableSymbol -> ExternalBirSymbol.VariableSymbol(signature)
                is IrConstructorSymbol -> ExternalBirSymbol.ConstructorSymbol(signature)
                is IrSimpleFunctionSymbol -> ExternalBirSymbol.SimpleFunctionSymbol(signature)
                is IrReturnableBlockSymbol -> ExternalBirSymbol.ReturnableBlockSymbol(signature)
                is IrPropertySymbol -> ExternalBirSymbol.PropertySymbol(signature)
                is IrLocalDelegatedPropertySymbol -> ExternalBirSymbol.LocalDelegatedPropertySymbol(signature)
                is IrTypeAliasSymbol -> ExternalBirSymbol.TypeAliasSymbol(signature)
                else -> error(IrErrorClassImpl.symbol)
            } as BirS
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

    private class SimpleTypeCacheKey(val type: IrSimpleType) {
        override fun equals(other: Any?): Boolean {
            other as SimpleTypeCacheKey
            return typesAreEqual(type, other.type)
        }

        private fun typesAreEqual(a: IrType, b: IrType): Boolean {
            if (a === b) return true
            if (a !is IrSimpleType || b !is IrSimpleType) return false

            if (a.nullability != b.nullability) return false

            val classifier = a.classifier
            val otherClassifier = b.classifier
            if (classifier.isBound != otherClassifier.isBound) return false
            if (classifier.isBound) {
                if (classifier.owner !== otherClassifier.owner) return false
            } else {
                if (classifier.signature == null) return false
                if (classifier.signature == otherClassifier.signature) return false
            }

            if (a.arguments.size != b.arguments.size) return false
            if (a.abbreviation !== b.abbreviation) return false
            if (a.annotations != b.annotations) return false
            repeat(a.arguments.size) { i ->
                if (!typeArgumentsAreEqual(a.arguments[i], b.arguments[i])) return false
            }

            return true
        }

        private fun typeArgumentsAreEqual(a: IrTypeArgument, b: IrTypeArgument): Boolean = when (a) {
            is IrStarProjection -> b is IrStarProjection
            is IrType -> b is IrType && typesAreEqual(a, b)
            is IrTypeProjection -> b is IrTypeProjection && a.variance == b.variance && typesAreEqual(a.type, b.type)
            else -> error(a)
        }

        override fun hashCode(): Int = type.computeHashCode()

        private fun IrType.computeHashCode() = if (this is IrSimpleType)
            this.computeHashCode()
        else
            this.hashCode()

        private fun IrSimpleType.computeHashCode(): Int {
            var h = if (classifier.isBound) classifier.owner.hashCode()
            else classifier.signature.hashCode()
            h = h * 31 + nullability.hashCode()
            arguments.forEach {
                h = h * 31 + it.computeHashCode()
            }
            h = h * 31 + annotations.size
            h = h * 31 + if (abbreviation == null) 0 else 1
            return h
        }

        private fun IrTypeArgument.computeHashCode(): Int = when (this) {
            is IrStarProjection -> IrStarProjectionImpl.hashCode()
            is IrType -> (this as IrType).computeHashCode()
            is IrTypeProjection -> type.computeHashCode() + variance.hashCode() * 31
            else -> this.hashCode()
        }
    }

    private val simpleTypesCache = hashMapOf<SimpleTypeCacheKey, BirSimpleType>()

    context(BirTreeContext)
    private fun remapSimpleType(irType: IrSimpleType): BirSimpleType {
        val key = SimpleTypeCacheKey(irType)
        simpleTypesCache[key]?.let {
            return it
        }

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

        return simpleTypesCache.putIfAbsent(key, birType) ?: birType
    }

    context(BirTreeContext)
    private fun remapTypeAbbreviation(abbreviation: IrTypeAbbreviation): BirTypeAbbreviation {
        return BirTypeAbbreviation(
            remapSymbol(abbreviation.typeAlias),
            abbreviation.hasQuestionMark,
            abbreviation.arguments.map { remapTypeArgument(it) },
            abbreviation.annotations.map { remapElement(it) as BirConstructorCall },
        )
    }

    context(BirTreeContext)
    private fun remapCapturedType(irType: IrCapturedType): BirCapturedType {
        return BirCapturedType(
            irType.captureStatus,
            irType.lowerType?.let { remapType(it) },
            remapTypeArgument(irType.constructor.argument),
            remapElement(irType.constructor.typeParameter) as BirTypeParameter,
        )
    }

    context(BirTreeContext)
    private fun remapDynamicType(irType: IrDynamicType): BirDynamicType {
        return BirDynamicType(
            irType.kotlinType,
            irType.annotations.map { remapElement(it) as BirConstructorCall },
            irType.variance,
        )
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
        is IrType -> remapType(irTypeArgument) as BirTypeArgument
        is IrTypeProjectionImpl -> makeTypeProjection(remapType(irTypeArgument.type), irTypeArgument.variance)
        else -> error(irTypeArgument)
    }

    companion object {
        fun IrElement.convertToBir(treeContext: BirTreeContext): BirElement {
            val converter = Ir2BirConverter()
            return converter.copyIrTree(treeContext, listOf(this)).single()
        }
    }
}