/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.symbols.BirClassSymbol
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.symbols.maybeAsElement
import org.jetbrains.kotlin.bir.types.BirSimpleType
import org.jetbrains.kotlin.descriptors.InlineClassRepresentation
import org.jetbrains.kotlin.descriptors.MultiFieldValueClassRepresentation
import org.jetbrains.kotlin.ir.types.impl.IrErrorClassImpl.symbol
import org.jetbrains.kotlin.ir.util.hasEqualFqName
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.filterIsInstanceAnd

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


context(BirTreeContext)
val BirClass.defaultType: BirSimpleType
    get() = this.thisReceiver!!.type as BirSimpleType


val BirConstructor.constructedClass
    get() = this.parent as BirClass

private val BirConstructorCall.annotationClass
    get() = this.target.asElement.constructedClass

fun BirConstructorCall.isAnnotationWithEqualFqName(fqName: FqName): Boolean =
    annotationClass.hasEqualFqName(fqName)

val BirClass.packageFqName: FqName?
    get() = signature?.packageFqName() ?: ancestors().firstNotNullOfOrNull { (it as? BirPackageFragment)?.fqName }

fun BirDeclarationWithName.hasEqualFqName(fqName: FqName): Boolean =
    symbol.hasEqualFqName(fqName) || name == fqName.shortName() && when (val parent = parent) {
        is BirPackageFragment -> parent.fqName == fqName.parent()
        is BirDeclarationWithName -> parent.hasEqualFqName(fqName.parent())
        else -> false
    }

@Suppress("RecursivePropertyAccessor")
val BirDeclarationWithName.fqNameWhenAvailable: FqName?
    get() = when (val parent = parent) {
        is BirDeclarationWithName -> parent.fqNameWhenAvailable?.child(name)
        is BirPackageFragment -> parent.fqName.child(name)
        else -> null
    }

@Suppress("RecursivePropertyAccessor")
val BirClass.classId: ClassId?
    get() = when (val parent = this.parent) {
        is BirClass -> parent.classId?.createNestedClassId(this.name)
        is BirPackageFragment -> ClassId.topLevel(parent.fqName.child(this.name))
        else -> null
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