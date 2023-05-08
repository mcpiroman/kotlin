/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.utils

import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.declarations.impl.BirTypeParameterImpl
import org.jetbrains.kotlin.bir.declarations.impl.BirValueParameterImpl
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.symbols.*
import org.jetbrains.kotlin.bir.types.*
import org.jetbrains.kotlin.bir.types.utils.defaultType
import org.jetbrains.kotlin.bir.types.utils.isNullable
import org.jetbrains.kotlin.bir.types.utils.substitute
import org.jetbrains.kotlin.bir.types.utils.typeOrNull
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.InlineClassRepresentation
import org.jetbrains.kotlin.descriptors.MultiFieldValueClassRepresentation
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance
import org.jetbrains.kotlin.utils.filterIsInstanceAnd
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import org.jetbrains.kotlin.utils.memoryOptimizedMapIndexed
import java.io.File

internal class BirElementAncestorsIterator(
    initial: BirElement?
) : Iterator<BirElement> {
    private var next: BirElement? = initial

    override fun hasNext(): Boolean = next != null

    override fun next(): BirElement {
        val n = next!!
        next = n.parent
        return n
    }
}

internal class BirElementAncestorsSequence(private val element: BirElement?) : Sequence<BirElement> {
    override fun iterator(): Iterator<BirElement> = BirElementAncestorsIterator(element)
}

fun BirElement.ancestors(includeSelf: Boolean = false): Sequence<BirElement> =
    BirElementAncestorsSequence(if (includeSelf) this else parent)

val BirDeclaration.parentAsClass: BirClass
    get() = parent as? BirClass
        ?: error("Parent of this declaration is not a class: ${render()}")


fun BirClass.companionObject(): BirClass? =
    this.declarations.singleOrNull { it is BirClass && it.isCompanion } as BirClass?

context (BirTreeContext)
val BirDeclaration.isGetter
    get() = this is BirSimpleFunction && this == this.correspondingProperty?.maybeAsElement?.getter

context (BirTreeContext)
val BirDeclaration.isSetter
    get() = this is BirSimpleFunction && this == this.correspondingProperty?.maybeAsElement?.setter

context (BirTreeContext)
val BirDeclaration.isAccessor
    get() = this.isGetter || this.isSetter

val BirDeclaration.isPropertyAccessor
    get() =
        this is BirSimpleFunction && this.correspondingProperty != null

val BirDeclaration.isPropertyField
    get() =
        this is BirField && this.correspondingProperty != null

val BirDeclaration.isAnonymousObject get() = this is BirClass && name == SpecialNames.NO_NAME_PROVIDED

val BirDeclaration.isAnonymousFunction get() = this is BirSimpleFunction && name == SpecialNames.NO_NAME_PROVIDED

context (BirTreeContext)
val BirFunction.isStatic: Boolean
    get() = parent is BirClass && dispatchReceiverParameter == null


val BirClass.constructors: Sequence<BirConstructor>
    get() = declarations.asSequence().filterIsInstance<BirConstructor>()

context (BirTreeContext)
val BirClass.defaultConstructor: BirConstructor?
    get() = constructors.firstOrNull { ctor -> ctor.valueParameters.all { it.defaultValue != null } }

val BirClass.fields: Sequence<BirField>
    get() = declarations.asSequence().filterIsInstance<BirField>()

val BirClass.primaryConstructor: BirConstructor?
    get() = this.declarations.singleOrNull { it is BirConstructor && it.isPrimary } as BirConstructor?


val BirClass.isSingleFieldValueClass: Boolean
    get() = valueClassRepresentation is InlineClassRepresentation

val BirClass.isMultiFieldValueClass: Boolean
    get() = valueClassRepresentation is MultiFieldValueClassRepresentation

val BirClass.inlineClassRepresentation: InlineClassRepresentation<BirSimpleType>?
    get() = valueClassRepresentation as? InlineClassRepresentation<BirSimpleType>

val BirClass.multiFieldValueClassRepresentation: MultiFieldValueClassRepresentation<BirSimpleType>?
    get() = valueClassRepresentation as? MultiFieldValueClassRepresentation<BirSimpleType>


fun BirClass.getProperty(name: String): BirProperty? {
    val properties = declarations.filterIsInstanceAnd<BirProperty> { it.name.asString() == name }
    if (properties.size > 1) error(properties)
    return properties.singleOrNull()
}

fun BirClass.getSimpleFunction(name: String): BirSimpleFunction? =
    findDeclaration<BirSimpleFunction> { it.name.asString() == name }

context(BirTreeContext)
fun BirClass.getPropertyGetter(name: String): BirSimpleFunction? =
    getProperty(name)?.getter
        ?: getSimpleFunction("<get-$name>").also { assert(it?.maybeAsElement?.correspondingProperty?.maybeAsElement?.name?.asString() == name) }

context(BirTreeContext)
fun BirClass.getPropertySetter(name: String): BirSimpleFunction? =
    getProperty(name)?.setter
        ?: getSimpleFunction("<set-$name>").also { assert(it?.maybeAsElement?.correspondingProperty?.maybeAsElement?.name?.asString() == name) }


inline fun <reified T : BirDeclaration> BirDeclarationContainer.findDeclaration(predicate: (T) -> Boolean): T? =
    declarations.find { it is T && predicate(it) } as? T


val BirClass.defaultType: BirSimpleType
    get() = with(DummyBirTreeContext) { thisReceiver!!.type as BirSimpleType }

val BirTypeParameter.defaultType: BirType
    get() = BirSimpleTypeImpl(
        this,
        SimpleTypeNullability.NOT_SPECIFIED,
        arguments = emptyList(),
        annotations = emptyList()
    )

val BirConstructor.constructedClass
    get() = this.parent as BirClass

private val BirConstructorCall.annotationClass
    get() = this.target.asElement.constructedClass

fun BirConstructorCall.isAnnotationWithEqualFqName(fqName: FqName): Boolean =
    annotationClass.hasEqualFqName(fqName)

val BirClass.packageFqName: FqName?
    get() = signature?.packageFqName() ?: ancestors().firstNotNullOfOrNull { (it as? BirPackageFragment)?.fqName }

fun BirDeclarationWithName.hasEqualFqName(fqName: FqName): Boolean {
    if ((this as BirSymbol).hasEqualFqName(fqName)) {
        return true
    }
    if (name != fqName.shortName()) {
        return false
    }

    ancestors().forEach {
        when (it) {
            is BirPackageFragment -> return it.fqName == fqName.parent()
            is BirDeclarationWithName -> return it.hasEqualFqName(fqName.parent())
        }
    }

    return false
}

fun BirSymbol.hasEqualFqName(fqName: FqName): Boolean {
    return /*todo: is public && */ with(signature as? IdSignature.CommonSignature ?: return false) {
        FqName("$packageFqName.$declarationFqName") == fqName
    }
}

@Suppress("RecursivePropertyAccessor")
val BirDeclarationWithName.fqNameWhenAvailable: FqName?
    get() = ancestors().firstNotNullOfOrNull {
        when (it) {
            is BirDeclarationWithName -> it.fqNameWhenAvailable?.child(name)
            is BirPackageFragment -> it.fqName.child(name)
            else -> null
        }
    }

@Suppress("RecursivePropertyAccessor")
val BirClass.classId: ClassId?
    get() = ancestors().firstNotNullOfOrNull {
        when (it) {
            is BirClass -> it.classId?.createNestedClassId(this.name)
            is BirPackageFragment -> ClassId.topLevel(it.fqName.child(this.name))
            else -> null
        }
    }


fun BirConstructorCall.isAnnotation(name: FqName) = target.asElement.parentAsClass.fqNameWhenAvailable == name

fun BirAnnotationContainer.getAnnotation(name: FqName): BirConstructorCall? =
    annotations.find { it.isAnnotation(name) }

fun BirAnnotationContainer.hasAnnotation(name: FqName) =
    annotations.any {
        it.target.asElement.parentAsClass.hasEqualFqName(name)
    }

fun BirAnnotationContainer.hasAnnotation(symbol: BirClassSymbol) =
    annotations.any {
        it.target.asElement.parentAsClass == symbol
    }

fun BirValueParameter.getIndex(): Int {
    val list = getContainingList()
        ?: return -1
    return list.indexOf(this)
}

fun BirTypeParameter.getIndex(): Int {
    val list = getContainingList()
        ?: return -1
    return list.indexOf(this)
}

fun makeTypeParameterSubstitutionMap(
    original: BirTypeParametersContainer,
    transformed: BirTypeParametersContainer
): Map<BirTypeParameterSymbol, BirType> =
    original.typeParameters
        .zip(transformed.typeParameters.map { it.defaultType })
        .toMap()

context (BirTreeContext)
@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirFunction.copyReceiverParametersFrom(from: BirFunction, substitutionMap: Map<BirTypeParameterSymbol, BirType>) {
    dispatchReceiverParameter = from.dispatchReceiverParameter?.run {
        BirValueParameterImpl(
            sourceSpan = sourceSpan,
            origin = origin,
            name = name,
            type = type.substitute(substitutionMap),
            varargElementType = varargElementType?.substitute(substitutionMap),
            isCrossinline = isCrossinline,
            isNoinline = isNoinline,
            isHidden = isHidden,
            isAssignable = isAssignable,
            _descriptor = null,
            defaultValue = null,
            annotations = emptyList(),
        )
    }
    extensionReceiverParameter = from.extensionReceiverParameter?.copyTo(this)
}

val BirTypeParametersContainer.classIfConstructor get() = if (this is BirConstructor) parentAsClass else this

context (BirTreeContext)
@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirValueParameter.copyTo(
    targetFunction: BirFunction,
    remapTypeMap: Map<BirTypeParameter, BirTypeParameter> = emptyMap(),
    type: BirType = this.type.remapTypeParameters(
        (parent as BirTypeParametersContainer).classIfConstructor,
        targetFunction.classIfConstructor,
        remapTypeMap
    ),
    defaultValue: BirExpressionBody? = this.defaultValue,
): BirValueParameter {
    val defaultValueCopy = defaultValue?.deepCopy()
    return BirValueParameterImpl(
        sourceSpan = sourceSpan,
        annotations = annotations.memoryOptimizedMap { it.deepCopy() },
        _descriptor = _descriptor,
        origin = origin,
        name = name,
        type = type,
        isAssignable = isAssignable,
        varargElementType = varargElementType,
        isCrossinline = isCrossinline,
        isNoinline = isNoinline,
        isHidden = false,
        defaultValue = defaultValueCopy
    )
}

/*context (BirTreeContext)
fun BirAnnotationContainer.copyAnnotationsFrom(source: BirAnnotationContainer) {
    annotations = annotations memoryOptimizedPlus source.copyAnnotations()
}*/

context (BirTreeContext)
fun BirAnnotationContainer.copyAnnotations(): List<BirConstructorCall> {
    return annotations.memoryOptimizedMap { it.deepCopy() }
}

context (BirTreeContext)
fun BirFunction.copyValueParametersFrom(from: BirFunction, substitutionMap: Map<BirTypeParameterSymbol, BirType>) {
    copyReceiverParametersFrom(from, substitutionMap)
    valueParameters += from.valueParameters.map {
        it.copyTo(this, type = it.type.substitute(substitutionMap))
    }
}

context (BirTreeContext)
fun BirFunction.copyValueParametersFrom(from: BirFunction) {
    copyValueParametersFrom(from, makeTypeParameterSubstitutionMap(from, this))
}

context (BirTreeContext)
fun BirTypeParametersContainer.copyTypeParameters(
    srcTypeParameters: Collection<BirTypeParameter>,
    origin: IrDeclarationOrigin? = null,
    parameterMap: Map<BirTypeParameter, BirTypeParameter>? = null
): List<BirTypeParameter> {
    val oldToNewParameterMap = parameterMap.orEmpty().toMutableMap()
    // Any type parameter can figure in a boundary type for any other parameter.
    // Therefore, we first copy the parameters themselves, then set up their supertypes.
    val newTypeParameters = srcTypeParameters.memoryOptimizedMapIndexed { i, sourceParameter ->
        sourceParameter.copyWithoutSuperTypes(origin ?: sourceParameter.origin).also {
            oldToNewParameterMap[sourceParameter] = it
        }
    }
    typeParameters += newTypeParameters
    srcTypeParameters.zip(newTypeParameters).forEach { (srcParameter, dstParameter) ->
        dstParameter.copySuperTypesFrom(srcParameter, oldToNewParameterMap)
    }
    return newTypeParameters
}

context (BirTreeContext)
fun BirTypeParametersContainer.copyTypeParametersFrom(
    source: BirTypeParametersContainer,
    origin: IrDeclarationOrigin? = null,
    parameterMap: Map<BirTypeParameter, BirTypeParameter>? = null
) = copyTypeParameters(source.typeParameters, origin, parameterMap)

private fun BirTypeParameter.copySuperTypesFrom(source: BirTypeParameter, srcToDstParameterMap: Map<BirTypeParameter, BirTypeParameter>) {
    val target = this
    val sourceParent = source.parent as BirTypeParametersContainer
    val targetParent = target.parent as BirTypeParametersContainer
    target.superTypes = source.superTypes.memoryOptimizedMap {
        it.remapTypeParameters(sourceParent, targetParent, srcToDstParameterMap)
    }
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun BirTypeParameter.copyWithoutSuperTypes(
    origin: IrDeclarationOrigin = this.origin
): BirTypeParameter = BirTypeParameterImpl(
    sourceSpan = sourceSpan,
    origin = origin,
    name = name,
    variance = variance,
    isReified = isReified,
    annotations = emptyList(),
    _descriptor = null,
    superTypes = emptyList(),
)

/**
 * Perform a substitution of type parameters occurring in [this]. In order of
 * precedence, parameter `P` is substituted with...
 *
 *   1) `T`, if `srcToDstParameterMap.get(P) == T`
 *   2) `T`, if `source.typeParameters[i] == P` and
 *      `target.typeParameters[i] == T`
 *   3) `P`
 *
 *  If [srcToDstParameterMap] is total on the domain of type parameters in
 *  [this], this effectively performs a substitution according to that map.
 */
fun BirType.remapTypeParameters(
    source: BirTypeParametersContainer,
    target: BirTypeParametersContainer,
    srcToDstParameterMap: Map<BirTypeParameter, BirTypeParameter>? = null
): BirType = when (this) {
    is BirSimpleType -> {
        when (val classifier = classifier) {
            is BirTypeParameter -> {
                val newClassifier = srcToDstParameterMap?.get(classifier)
                    ?: if (classifier.parent == source)
                        target.typeParameters.elementAt(classifier.getIndex())
                    else
                        classifier
                BirSimpleTypeImpl(newClassifier.asElement, nullability, arguments, annotations)
            }
            is BirClass -> BirSimpleTypeImpl(
                classifier.asElement,
                nullability,
                arguments.memoryOptimizedMap {
                    when (it) {
                        is BirTypeProjection -> makeTypeProjection(
                            it.type.remapTypeParameters(source, target, srcToDstParameterMap),
                            it.variance
                        )
                        is BirStarProjection -> it
                    }
                },
                annotations
            )
            else -> this
        }
    }
    else -> this
}

val BirDeclaration.isFileClass: Boolean
    get() = origin == IrDeclarationOrigin.FILE_CLASS ||
            origin == IrDeclarationOrigin.SYNTHETIC_FILE_CLASS ||
            origin == IrDeclarationOrigin.JVM_MULTIFILE_CLASS

val BirDeclaration.isTopLevel: Boolean
    get() {
        if (parent is BirPackageFragment) return true
        val parentClass = parent as? BirClass
        return parentClass?.isFileClass == true && parentClass.parent is BirPackageFragment
    }

fun BirValueParameter.isInlineParameter(type: BirType = this.type) =
    !isNoinline && !type.isNullable() && (type.isFunction() || type.isSuspendFunction())

val BirConstructorCall.classTypeArgumentsCount: Int
    get() = typeArguments.size - constructorTypeArgumentsCount

val BirFile.path: String get() = fileEntry.name
val BirFile.name: String get() = File(path).name


fun BirExpression.isAdaptedFunctionReference() =
    this is BirBlock && this.origin == IrStatementOrigin.ADAPTED_FUNCTION_REFERENCE

val BirDeclaration.isLocal: Boolean
    get() {
        return ancestors().any {
            it is BirDeclarationWithVisibility && it.visibility == DescriptorVisibilities.LOCAL
                    || it is BirDeclaration && it.isAnonymousObject
                    || it is BirScript
                    || it is BirClass && it.origin == IrDeclarationOrigin.SCRIPT_CLASS
        }
    }

fun BirInlinedFunctionBlock.isFunctionInlining(): Boolean {
    return this.inlinedElement is BirFunction
}

fun BirInlinedFunctionBlock.isLambdaInlining(): Boolean {
    return !isFunctionInlining()
}

fun BirStatement.isPartialLinkageRuntimeError(): Boolean {
    return when (this) {
        is BirCall -> origin == IrStatementOrigin.PARTIAL_LINKAGE_RUNTIME_ERROR //|| symbol == builtIns.linkageErrorSymbol
        is BirContainerExpression -> origin == IrStatementOrigin.PARTIAL_LINKAGE_RUNTIME_ERROR || statements.any { it.isPartialLinkageRuntimeError() }
        else -> false
    }
}

fun BirAttributeContainer.copyAttributes(other: BirAttributeContainer) {
    attributeOwnerId = other.attributeOwnerId
    //originalBeforeInline = other.originalBeforeInline
}

val BirFunction.allTypeParameters: Collection<BirTypeParameter>
    get() = if (this is BirConstructor)
        parentAsClass.typeParameters + typeParameters
    else
        typeParameters


fun BirMemberAccessExpression<*>.getTypeSubstitutionMap(function: BirFunction): Map<BirTypeParameterSymbol, BirType> =
    with(DummyBirTreeContext) {
        val typeParameters = function.allTypeParameters

        val superQualifier = (this as? BirCall)?.superQualifier

        val receiverType =
            if (superQualifier != null) superQualifier.defaultType as? BirSimpleType
            else dispatchReceiver?.type as? BirSimpleType

        val dispatchReceiverTypeArguments = receiverType?.arguments ?: emptyList()

        if (typeParameters.isEmpty() && dispatchReceiverTypeArguments.isEmpty()) {
            return emptyMap()
        }

        val result = mutableMapOf<BirTypeParameterSymbol, BirType>()
        if (dispatchReceiverTypeArguments.isNotEmpty()) {
            val parentTypeParameters =
                if (function is BirConstructor) {
                    val constructedClass = function.parentAsClass
                    if (!constructedClass.isInner && dispatchReceiver != null) {
                        throw AssertionError("Non-inner class constructor reference with dispatch receiver:\n${this@getTypeSubstitutionMap.render()}")
                    }
                    extractTypeParameters(constructedClass.parent as BirClass)
                } else {
                    extractTypeParameters(function.ancestors().firstIsInstance<BirClass>())
                }
            for ((index, typeParam) in parentTypeParameters.withIndex()) {
                dispatchReceiverTypeArguments[index].typeOrNull?.let {
                    result[typeParam] = it
                }
            }
        }
        return (typeParameters zip typeArguments.requireNoNulls()).toMap() + result
    }

val BirFunctionReference.typeSubstitutionMap: Map<BirTypeParameterSymbol, BirType>
    get() = getTypeSubstitutionMap(target as BirFunction)

val BirFunctionAccessExpression.typeSubstitutionMap: Map<BirTypeParameterSymbol, BirType>
    get() = getTypeSubstitutionMap(target as BirFunction)