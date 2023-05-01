/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.bir.utils.Ir2BirConverter
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.builtins.UnsignedType
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.name.Name
import java.util.concurrent.ConcurrentHashMap

context(BirTreeContext)
@OptIn(ObsoleteDescriptorBasedAPI::class)
class BirBuiltIns(
    private val irBuiltIns: IrBuiltIns,
    private val converter: Ir2BirConverter,
) {
    val languageVersionSettings: LanguageVersionSettings = irBuiltIns.languageVersionSettings

    val anyType: BirType = converter.convertType(irBuiltIns.anyType)
    val anyClass: BirClass = mapSymbolOwner(irBuiltIns.anyClass)
    val anyNType: BirType = converter.convertType(irBuiltIns.anyNType)
    val booleanType: BirType = converter.convertType(irBuiltIns.booleanType)
    val booleanClass: BirClass = mapSymbolOwner(irBuiltIns.booleanClass)
    val charType: BirType = converter.convertType(irBuiltIns.charType)
    val charClass: BirClass = mapSymbolOwner(irBuiltIns.charClass)
    val numberType: BirType = converter.convertType(irBuiltIns.numberType)
    val numberClass: BirClass = mapSymbolOwner(irBuiltIns.numberClass)
    val byteType: BirType = converter.convertType(irBuiltIns.byteType)
    val byteClass: BirClass = mapSymbolOwner(irBuiltIns.byteClass)
    val shortType: BirType = converter.convertType(irBuiltIns.shortType)
    val shortClass: BirClass = mapSymbolOwner(irBuiltIns.shortClass)
    val intType: BirType = converter.convertType(irBuiltIns.intType)
    val intClass: BirClass = mapSymbolOwner(irBuiltIns.intClass)
    val longType: BirType = converter.convertType(irBuiltIns.longType)
    val longClass: BirClass = mapSymbolOwner(irBuiltIns.longClass)
    val floatType: BirType = converter.convertType(irBuiltIns.floatType)
    val floatClass: BirClass = mapSymbolOwner(irBuiltIns.floatClass)
    val doubleType: BirType = converter.convertType(irBuiltIns.doubleType)
    val doubleClass: BirClass = mapSymbolOwner(irBuiltIns.doubleClass)
    val nothingType: BirType = converter.convertType(irBuiltIns.nothingType)
    val nothingClass: BirClass = mapSymbolOwner(irBuiltIns.nothingClass)
    val nothingNType: BirType = converter.convertType(irBuiltIns.nothingNType)
    val unitType: BirType = converter.convertType(irBuiltIns.unitType)
    val unitClass: BirClass = mapSymbolOwner(irBuiltIns.unitClass)
    val stringType: BirType = converter.convertType(irBuiltIns.stringType)
    val stringClass: BirClass = mapSymbolOwner(irBuiltIns.stringClass)
    val charSequenceClass: BirClass = mapSymbolOwner(irBuiltIns.charSequenceClass)

    val collectionClass: BirClass = mapSymbolOwner(irBuiltIns.collectionClass)
    val arrayClass: BirClass = mapSymbolOwner(irBuiltIns.arrayClass)
    val setClass: BirClass = mapSymbolOwner(irBuiltIns.setClass)
    val listClass: BirClass = mapSymbolOwner(irBuiltIns.listClass)
    val mapClass: BirClass = mapSymbolOwner(irBuiltIns.mapClass)
    val mapEntryClass: BirClass = mapSymbolOwner(irBuiltIns.mapEntryClass)
    val iterableClass: BirClass = mapSymbolOwner(irBuiltIns.iterableClass)
    val iteratorClass: BirClass = mapSymbolOwner(irBuiltIns.iteratorClass)
    val listIteratorClass: BirClass = mapSymbolOwner(irBuiltIns.listIteratorClass)
    val mutableCollectionClass: BirClass = mapSymbolOwner(irBuiltIns.mutableCollectionClass)
    val mutableSetClass: BirClass = mapSymbolOwner(irBuiltIns.mutableSetClass)
    val mutableListClass: BirClass = mapSymbolOwner(irBuiltIns.mutableListClass)
    val mutableMapClass: BirClass = mapSymbolOwner(irBuiltIns.mutableMapClass)
    val mutableMapEntryClass: BirClass = mapSymbolOwner(irBuiltIns.mutableMapEntryClass)
    val mutableIterableClass: BirClass = mapSymbolOwner(irBuiltIns.mutableIterableClass)
    val mutableIteratorClass: BirClass = mapSymbolOwner(irBuiltIns.mutableIteratorClass)
    val mutableListIteratorClass: BirClass = mapSymbolOwner(irBuiltIns.mutableListIteratorClass)

    val comparableClass: BirClass = mapSymbolOwner(irBuiltIns.comparableClass)
    val throwableType: BirType = converter.convertType(irBuiltIns.throwableType)
    val throwableClass: BirClass = mapSymbolOwner(irBuiltIns.throwableClass)
    val kCallableClass: BirClass = mapSymbolOwner(irBuiltIns.kCallableClass)
    val kPropertyClass: BirClass = mapSymbolOwner(irBuiltIns.kPropertyClass)
    val kClassClass: BirClass = mapSymbolOwner(irBuiltIns.kClassClass)
    val kProperty0Class: BirClass = mapSymbolOwner(irBuiltIns.kProperty0Class)
    val kProperty1Class: BirClass = mapSymbolOwner(irBuiltIns.kProperty1Class)
    val kProperty2Class: BirClass = mapSymbolOwner(irBuiltIns.kProperty2Class)
    val kMutableProperty0Class: BirClass = mapSymbolOwner(irBuiltIns.kMutableProperty0Class)
    val kMutableProperty1Class: BirClass = mapSymbolOwner(irBuiltIns.kMutableProperty1Class)
    val kMutableProperty2Class: BirClass = mapSymbolOwner(irBuiltIns.kMutableProperty2Class)
    val functionClass: BirClass = mapSymbolOwner(irBuiltIns.functionClass)
    val kFunctionClass: BirClass = mapSymbolOwner(irBuiltIns.kFunctionClass)
    val annotationType: BirType = converter.convertType(irBuiltIns.annotationType)
    val annotationClass: BirClass = mapSymbolOwner(irBuiltIns.annotationClass)

    val primitiveBirTypes: List<BirType> = irBuiltIns.primitiveIrTypes.map { converter.convertType(it) }
    val primitiveBirTypesWithComparisons: List<BirType> = irBuiltIns.primitiveIrTypesWithComparisons.map { converter.convertType(it) }
    val primitiveFloatingPointBirTypes: List<BirType> = irBuiltIns.primitiveFloatingPointIrTypes.map { converter.convertType(it) }

    val byteArray: BirClass = mapSymbolOwner(irBuiltIns.byteArray)
    val charArray: BirClass = mapSymbolOwner(irBuiltIns.charArray)
    val shortArray: BirClass = mapSymbolOwner(irBuiltIns.shortArray)
    val intArray: BirClass = mapSymbolOwner(irBuiltIns.intArray)
    val longArray: BirClass = mapSymbolOwner(irBuiltIns.longArray)
    val floatArray: BirClass = mapSymbolOwner(irBuiltIns.floatArray)
    val doubleArray: BirClass = mapSymbolOwner(irBuiltIns.doubleArray)
    val booleanArray: BirClass = mapSymbolOwner(irBuiltIns.booleanArray)

    val primitiveArraysToPrimitiveTypes: Map<BirClass, PrimitiveType> =
        irBuiltIns.primitiveArraysToPrimitiveTypes.mapKeys { mapSymbolOwner(it.key) }
    val primitiveTypesToPrimitiveArrays: Map<PrimitiveType, BirClass> =
        irBuiltIns.primitiveTypesToPrimitiveArrays.mapValues { mapSymbolOwner(it.value) }
    val primitiveArrayElementTypes: Map<BirClass, BirType?> = irBuiltIns.primitiveArrayElementTypes.entries
        .associate { entry -> mapSymbolOwner<_, BirClass>(entry.key) to entry.value?.let { converter.convertType(it) } }
    val primitiveArrayForType: Map<BirType?, BirClass> = irBuiltIns.primitiveArrayForType.entries
        .associate { entry -> entry.key?.let { converter.convertType(it) } to mapSymbolOwner<_, BirClass>(entry.value) }

    val unsignedTypesToUnsignedArrays: Map<UnsignedType, BirClass> =
        irBuiltIns.unsignedTypesToUnsignedArrays.mapValues { mapSymbolOwner(it.value) }
    val unsignedArraysElementTypes: Map<BirClass, BirType?> = irBuiltIns.unsignedArraysElementTypes.entries
        .associate { entry -> mapSymbolOwner<_, BirClass>(entry.key) to entry.value?.let { converter.convertType(it) } }

    val lessFunByOperandType: Map<BirClass, BirSimpleFunction> = irBuiltIns.lessFunByOperandType.entries
        .associate { (key, value) -> mapSymbolOwner<_, BirClass>(key) to mapSymbolOwner(value) }
    val lessOrEqualFunByOperandType: Map<BirClass, BirSimpleFunction> = irBuiltIns.lessOrEqualFunByOperandType.entries
        .associate { (key, value) -> mapSymbolOwner<_, BirClass>(key) to mapSymbolOwner(value) }
    val greaterOrEqualFunByOperandType: Map<BirClass, BirSimpleFunction> = irBuiltIns.greaterOrEqualFunByOperandType.entries
        .associate { (key, value) -> mapSymbolOwner<_, BirClass>(key) to mapSymbolOwner(value) }
    val greaterFunByOperandType: Map<BirClass, BirSimpleFunction> = irBuiltIns.greaterFunByOperandType.entries
        .associate { (key, value) -> mapSymbolOwner<_, BirClass>(key) to mapSymbolOwner(value) }
    val ieee754equalsFunByOperandType: Map<BirClass, BirSimpleFunction> = irBuiltIns.ieee754equalsFunByOperandType.entries
        .associate { (key, value) -> mapSymbolOwner<_, BirClass>(key) to mapSymbolOwner(value) }

    val booleanNotSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.booleanNotSymbol)
    val eqeqeqSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.eqeqeqSymbol)
    val eqeqSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.eqeqSymbol)
    val throwCceSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.throwCceSymbol)
    val throwIseSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.throwIseSymbol)
    val andandSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.andandSymbol)
    val ororSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.ororSymbol)
    val noWhenBranchMatchedExceptionSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.noWhenBranchMatchedExceptionSymbol)
    val illegalArgumentExceptionSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.illegalArgumentExceptionSymbol)
    val checkNotNullSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.checkNotNullSymbol)
    val dataClassArrayMemberHashCodeSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.dataClassArrayMemberHashCodeSymbol)
    val dataClassArrayMemberToStringSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.dataClassArrayMemberToStringSymbol)
    val enumClass: BirClass = mapSymbolOwner(irBuiltIns.enumClass)

    val intPlusSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.intPlusSymbol)
    val intTimesSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.intTimesSymbol)
    val intXorSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.intXorSymbol)

    val extensionToString: BirSimpleFunction = mapSymbolOwner(irBuiltIns.extensionToString)
    val memberToString: BirSimpleFunction = mapSymbolOwner(irBuiltIns.memberToString)

    val extensionStringPlus: BirSimpleFunction = mapSymbolOwner(irBuiltIns.extensionStringPlus)
    val memberStringPlus: BirSimpleFunction = mapSymbolOwner(irBuiltIns.memberStringPlus)

    val arrayOf: BirSimpleFunction = mapSymbolOwner(irBuiltIns.arrayOf)
    val arrayOfNulls: BirSimpleFunction = mapSymbolOwner(irBuiltIns.arrayOfNulls)

    val linkageErrorSymbol: BirSimpleFunction = mapSymbolOwner(irBuiltIns.linkageErrorSymbol)

    private val functionNCache = ConcurrentHashMap<IrClass, BirClass>()
    private val kFunctionNCache = ConcurrentHashMap<IrClass, BirClass>()
    private val suspendFunctionNCache = ConcurrentHashMap<IrClass, BirClass>()
    private val kSuspendFunctionNCache = ConcurrentHashMap<IrClass, BirClass>()
    fun functionN(arity: Int): BirClass = functionNCache.computeIfAbsent(irBuiltIns.functionN(arity)) { mapElement(it) }
    fun kFunctionN(arity: Int): BirClass = kFunctionNCache.computeIfAbsent(irBuiltIns.kFunctionN(arity)) { mapElement(it) }
    fun suspendFunctionN(arity: Int): BirClass =
        suspendFunctionNCache.computeIfAbsent(irBuiltIns.suspendFunctionN(arity)) { mapElement(it) }

    fun kSuspendFunctionN(arity: Int): BirClass =
        kSuspendFunctionNCache.computeIfAbsent(irBuiltIns.kSuspendFunctionN(arity)) { mapElement(it) }

    fun findFunctions(name: Name, vararg packageNameSegments: String = arrayOf("kotlin")): Iterable<BirSimpleFunction> {
        return irBuiltIns.findFunctions(name, *packageNameSegments).map { mapSymbolOwner(it) }
    }

    fun findClass(name: Name, vararg packageNameSegments: String = arrayOf("kotlin")): BirClass? =
        irBuiltIns.findClass(name, *packageNameSegments)?.let { mapSymbolOwner(it) }

    /*private fun <IrS : IrSymbol, BirS : BirSymbol> mapSymbolToOwner(symbol: IrS): BirS {
        return converter.mapSymbolToOwner(symbol.owner, symbol)
    }*/
    private fun <Ir : IrElement, Bir : BirElement> mapElement(element: Ir): Bir {
        return converter.mapIrElement(element) as Bir
    }

    private fun <IrS : IrSymbol, Bir : BirElement> mapSymbolOwner(symbol: IrS): Bir {
        return mapElement(symbol.owner)
    }
}