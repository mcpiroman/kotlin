/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.wasm

import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.backend.BirBuiltInSymbols
import org.jetbrains.kotlin.bir.declarations.BirClass
import org.jetbrains.kotlin.bir.declarations.BirProperty
import org.jetbrains.kotlin.bir.declarations.BirSimpleFunction
import org.jetbrains.kotlin.bir.symbols.BirClassifierSymbol
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.bir.utils.*
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.MemberScope

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@OptIn(ObsoleteDescriptorBasedAPI::class)
class BirWasmSymbols(
    birBuiltIns: BirBuiltIns,
    private val symbolTable: SymbolTable,
    birTreeContext: BirTreeContext,
    converter: Ir2BirConverter,
    private val module: ModuleDescriptor
) : BirBuiltInSymbols(birBuiltIns, birTreeContext, converter) {

    private val kotlinTopLevelPackage: PackageViewDescriptor =
        module.getPackage(FqName("kotlin"))
    private val enumsInternalPackage: PackageViewDescriptor =
        module.getPackage(FqName("kotlin.enums"))
    private val wasmInternalPackage: PackageViewDescriptor =
        module.getPackage(FqName("kotlin.wasm.internal"))
    private val kotlinJsPackage: PackageViewDescriptor =
        module.getPackage(FqName("kotlin.js"))
    private val collectionsPackage: PackageViewDescriptor =
        module.getPackage(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME)
    private val builtInsPackage: PackageViewDescriptor =
        module.getPackage(StandardNames.BUILT_INS_PACKAGE_FQ_NAME)
    private val kotlinTestPackage: PackageViewDescriptor =
        module.getPackage(FqName("kotlin.test"))


    internal inner class WasmReflectionSymbols {
        val createKType: BirSimpleFunction = getInternalFunction("createKType")
        val getClassData: BirSimpleFunction = getInternalFunction("wasmGetTypeInfoData")
        val getKClass: BirSimpleFunction = getInternalFunction("getKClass")
        val getKClassFromExpression: BirSimpleFunction = getInternalFunction("getKClassFromExpression")
        val createKTypeParameter: BirSimpleFunction = getInternalFunction("createKTypeParameter")
        val getStarKTypeProjection = getInternalFunction("getStarKTypeProjection")
        val createCovariantKTypeProjection = getInternalFunction("createCovariantKTypeProjection")
        val createInvariantKTypeProjection = getInternalFunction("createInvariantKTypeProjection")
        val createContravariantKTypeProjection = getInternalFunction("createContravariantKTypeProjection")

        val primitiveClassesObject = getInternalClass("PrimitiveClasses")
        val kTypeClass: BirClass = getClass(FqName("kotlin.reflect.KClass"))

        val getTypeInfoTypeDataByPtr: BirSimpleFunction = getInternalFunction("getTypeInfoTypeDataByPtr")
        val wasmTypeInfoData: BirClass = getInternalClass("TypeInfoData")
    }

    internal val reflectionSymbols: WasmReflectionSymbols = WasmReflectionSymbols()

    internal val eagerInitialization: BirClass = getClass(FqName("kotlin.EagerInitialization"))

    internal val isNotFirstWasmExportCall: BirProperty =
        getProperty(FqName.fromSegments(listOf("kotlin", "wasm", "internal", "isNotFirstWasmExportCall")))

    internal val initAssociatedObjects = getInternalFunction("initAssociatedObjects")
    internal val addAssociatedObject = getInternalFunction("addAssociatedObject")

    internal val throwAsJsException: BirSimpleFunction = getInternalFunction("throwAsJsException")

    override val throwNullPointerException = getInternalFunction("THROW_NPE")
    override val throwISE = getInternalFunction("THROW_ISE")
    override val throwTypeCastException = getInternalFunction("THROW_CCE")
    val throwIAE = getInternalFunction("THROW_IAE")
    val throwNoBranchMatchedException =
        getInternalFunction("throwNoBranchMatchedException")
    override val throwUninitializedPropertyAccessException =
        getInternalFunction("throwUninitializedPropertyAccessException")
    override val defaultConstructorMarker =
        getClass(FqName("kotlin.wasm.internal.DefaultConstructorMarker"))
    override val throwKotlinNothingValueException: BirSimpleFunction
        get() = TODO()
    override val stringBuilder =
        getClass(FqName("kotlin.text.StringBuilder"))
    override val getContinuation =
        getInternalFunction("getContinuation")
    override val suspendCoroutineUninterceptedOrReturn =
        getInternalFunction("suspendCoroutineUninterceptedOrReturn")
    override val coroutineGetContext =
        getInternalFunction("getCoroutineContext")
    override val returnIfSuspended =
        getInternalFunction("returnIfSuspended")

    val enumEntries = getClass(FqName.fromSegments(listOf("kotlin", "enums", "EnumEntries")))
    val createEnumEntries = findFunctions(enumsInternalPackage.memberScope, Name.identifier("enumEntries"))
        .find { it.valueParameters.firstOrNull()?.type?.isFunctionType == false }
        .let { symbolTable.referenceSimpleFunction(it!!) }

    val enumValueOfIntrinsic = getInternalFunction("enumValueOfIntrinsic")
    val enumValuesIntrinsic = getInternalFunction("enumValuesIntrinsic")

    val coroutineEmptyContinuation = getProperty(FqName.fromSegments(listOf("kotlin", "wasm", "internal", "EmptyContinuation")))

    override val functionAdapter: BirClass
        get() = TODO()

    val wasmUnreachable = getInternalFunction("wasm_unreachable")

    val voidClass = getClass(FqName("kotlin.wasm.internal.Void"))
    val voidType by lazy { with(birTreeContext) { voidClass.defaultType } }

    private val consumeAnyIntoVoid = getInternalFunction("consumeAnyIntoVoid")
    private val consumePrimitiveIntoVoid = mapOf(
        birBuiltIns.booleanType to getInternalFunction("consumeBooleanIntoVoid"),
        birBuiltIns.byteType to getInternalFunction("consumeByteIntoVoid"),
        birBuiltIns.shortType to getInternalFunction("consumeShortIntoVoid"),
        birBuiltIns.charType to getInternalFunction("consumeCharIntoVoid"),
        birBuiltIns.intType to getInternalFunction("consumeIntIntoVoid"),
        birBuiltIns.longType to getInternalFunction("consumeLongIntoVoid"),
        birBuiltIns.floatType to getInternalFunction("consumeFloatIntoVoid"),
        birBuiltIns.doubleType to getInternalFunction("consumeDoubleIntoVoid")
    )

    fun findVoidConsumer(type: BirType): BirSimpleFunction =
        consumePrimitiveIntoVoid[type] ?: consumeAnyIntoVoid

    val equalityFunctions = mapOf(
        birBuiltIns.booleanType to getInternalFunction("wasm_i32_eq"),
        birBuiltIns.byteType to getInternalFunction("wasm_i32_eq"),
        birBuiltIns.shortType to getInternalFunction("wasm_i32_eq"),
        birBuiltIns.charType to getInternalFunction("wasm_i32_eq"),
        birBuiltIns.intType to getInternalFunction("wasm_i32_eq"),
        birBuiltIns.longType to getInternalFunction("wasm_i64_eq")
    )

    val floatEqualityFunctions = mapOf(
        birBuiltIns.floatType to getInternalFunction("wasm_f32_eq"),
        birBuiltIns.doubleType to getInternalFunction("wasm_f64_eq")
    )

    private fun wasmPrimitiveTypeName(classifier: BirClassifierSymbol): String = with(birBuiltIns) {
        when (classifier) {
            booleanClass, byteClass, shortClass, charClass, intClass -> "i32"
            floatClass -> "f32"
            doubleClass -> "f64"
            longClass -> "i64"
            else -> error("Unknown primitive type")
        }
    }

    val comparisonBuiltInsToWasmIntrinsics = birBuiltIns.run {
        listOf(
            lessFunByOperandType to "lt",
            lessOrEqualFunByOperandType to "le",
            greaterOrEqualFunByOperandType to "ge",
            greaterFunByOperandType to "gt"
        ).map { (typeToBuiltIn, wasmOp) ->
            typeToBuiltIn.map { (type, builtin) ->
                val wasmType = wasmPrimitiveTypeName(type)
                val markSign = if (wasmType == "i32" || wasmType == "i64") "_s" else ""
                builtin to getInternalFunction("wasm_${wasmType}_$wasmOp$markSign")
            }
        }.flatten().toMap()
    }

    val booleanAnd = getInternalFunction("wasm_i32_and")
    val refEq = getInternalFunction("wasm_ref_eq")
    val refIsNull = getInternalFunction("wasm_ref_is_null")
    val externRefIsNull = getInternalFunction("wasm_externref_is_null")
    val refTest = getInternalFunction("wasm_ref_test_deprecated")
    val refCastNull = getInternalFunction("wasm_ref_cast_deprecated")
    val wasmArrayCopy = getInternalFunction("wasm_array_copy")
    val wasmArrayNewData0 = getInternalFunction("array_new_data0")

    val intToLong = getInternalFunction("wasm_i64_extend_i32_s")

    val rangeCheck = getInternalFunction("rangeCheck")
    val assertFuncs =
        findFunctions(kotlinTopLevelPackage.memberScope, Name.identifier("assert")).map { symbolTable.referenceSimpleFunction(it) }

    val boxIntrinsic: BirSimpleFunction = getInternalFunction("boxIntrinsic")
    val unboxIntrinsic: BirSimpleFunction = getInternalFunction("unboxIntrinsic")

    val stringGetLiteral = getFunction(builtInsPackage, "stringLiteral")
    val stringGetPoolSize = getInternalFunction("stringGetPoolSize")

    val testFun = maybeGetFunction(kotlinTestPackage, "test")
    val suiteFun = maybeGetFunction(kotlinTestPackage, "suite")
    val startUnitTests = maybeGetFunction(kotlinTestPackage, "startUnitTests")

    val wasmTypeId = getInternalFunction("wasmTypeId")

    val wasmIsInterface = getInternalFunction("wasmIsInterface")

    val nullableEquals = getInternalFunction("nullableEquals")
    val anyNtoString = getInternalFunction("anyNtoString")

    val nullableFloatIeee754Equals = getInternalFunction("nullableFloatIeee754Equals")
    val nullableDoubleIeee754Equals = getInternalFunction("nullableDoubleIeee754Equals")

    val unsafeGetScratchRawMemory = getInternalFunction("unsafeGetScratchRawMemory")
    val returnArgumentIfItIsKotlinAny = getInternalFunction("returnArgumentIfItIsKotlinAny")

    val newJsArray = getInternalFunction("newJsArray")
    val jsArrayPush = getInternalFunction("jsArrayPush")

    val startCoroutineUninterceptedOrReturnIntrinsics =
        (0..2).map { getInternalFunction("startCoroutineUninterceptedOrReturnIntrinsic$it") }

    // KProperty implementations
    val kLocalDelegatedPropertyImpl: BirClass = getInternalClass("KLocalDelegatedPropertyImpl")
    val kLocalDelegatedMutablePropertyImpl: BirClass = getInternalClass("KLocalDelegatedMutablePropertyImpl")
    val kProperty0Impl: BirClass = getInternalClass("KProperty0Impl")
    val kProperty1Impl: BirClass = getInternalClass("KProperty1Impl")
    val kProperty2Impl: BirClass = getInternalClass("KProperty2Impl")
    val kMutableProperty0Impl: BirClass = getInternalClass("KMutableProperty0Impl")
    val kMutableProperty1Impl: BirClass = getInternalClass("KMutableProperty1Impl")
    val kMutableProperty2Impl: BirClass = getInternalClass("KMutableProperty2Impl")
    val kMutableProperty0: BirClass = getClass(FqName("kotlin.reflect.KMutableProperty0"))
    val kMutableProperty1: BirClass = getClass(FqName("kotlin.reflect.KMutableProperty1"))
    val kMutableProperty2: BirClass = getClass(FqName("kotlin.reflect.KMutableProperty2"))

    val kTypeStub = getInternalFunction("kTypeStub")

    val arraysCopyInto = findFunctions(collectionsPackage.memberScope, Name.identifier("copyInto"))
        .map { symbolTable.referenceSimpleFunction(it) }

    /*private val contentToString: List<BirSimpleFunction> =
        findFunctions(collectionsPackage.memberScope, Name.identifier("contentToString"))
            .map { symbolTable.referenceSimpleFunction(it) }

    private val contentHashCode: List<BirSimpleFunction> =
        findFunctions(collectionsPackage.memberScope, Name.identifier("contentHashCode"))
            .map { symbolTable.referenceSimpleFunction(it) }

    private fun findOverloadForReceiver(arrayType: IrType, overloadsList: List<BirSimpleFunction>): BirSimpleFunction =
        overloadsList.first {
            val receiverType = it.owner.extensionReceiverParameter?.type
            receiverType != null && arrayType.isNullable() == receiverType.isNullable() && arrayType.classOrNull == receiverType.classOrNull
        }

    fun findContentToStringOverload(arrayType: IrType): BirSimpleFunction = findOverloadForReceiver(arrayType, contentToString)

    fun findContentHashCodeOverload(arrayType: IrType): BirSimpleFunction = findOverloadForReceiver(arrayType, contentHashCode)
*/

    private val getProgressionLastElementSymbols =
        birBuiltIns.findFunctions(Name.identifier("getProgressionLastElement"), "kotlin", "internal")

    /*override val getProgressionLastElementByReturnType: Map<BirClassifierSymbol, BirSimpleFunction> by lazy {
        getProgressionLastElementSymbols.associateBy { it.owner.returnType.classifierOrFail }
    }*/

    private val toUIntSymbols = birBuiltIns.findFunctions(Name.identifier("toUInt"), "kotlin")

    /*override val toUIntByExtensionReceiver: Map<BirClassifierSymbol, BirSimpleFunction> by lazy {
        toUIntSymbols.associateBy {
            it.owner.extensionReceiverParameter?.type?.classifierOrFail
                ?: error("Expected extension receiver for ${it.owner.render()}")
        }
    }*/

    private val toULongSymbols = birBuiltIns.findFunctions(Name.identifier("toULong"), "kotlin")

    /*val toULongByExtensionReceiver: Map<BirClassifierSymbol, BirSimpleFunction> by lazy {
        toULongSymbols.associateBy {
            it.owner.extensionReceiverParameter?.type?.classifierOrFail
                ?: error("Expected extension receiver for ${it.owner.render()}")
        }
    }*/

    private val wasmDataRefClass = getClass(FqName("kotlin.wasm.internal.reftypes.dataref"))
    val wasmDataRefType by lazy { with(birTreeContext) { wasmDataRefClass.defaultType } }

    val wasmAnyRefClass = getClass(FqName("kotlin.wasm.internal.reftypes.anyref"))

    private val jsAnyClass = getClass(FqName("kotlin.js.JsAny"))
    val jsAnyType by lazy { with(birTreeContext) { jsAnyClass.defaultType } }

    inner class JsInteropAdapters {
        val kotlinToJsStringAdapter = getInternalFunction("kotlinToJsStringAdapter")
        val kotlinToJsAnyAdapter = getInternalFunction("kotlinToJsAnyAdapter")
        val numberToDoubleAdapter = getInternalFunction("numberToDoubleAdapter")

        val jsCheckIsNullOrUndefinedAdapter = getInternalFunction("jsCheckIsNullOrUndefinedAdapter")

        val jsToKotlinStringAdapter = getInternalFunction("jsToKotlinStringAdapter")
        val jsToKotlinAnyAdapter = getInternalFunction("jsToKotlinAnyAdapter")

        val jsToKotlinByteAdapter = getInternalFunction("jsToKotlinByteAdapter")
        val jsToKotlinShortAdapter = getInternalFunction("jsToKotlinShortAdapter")
        val jsToKotlinCharAdapter = getInternalFunction("jsToKotlinCharAdapter")

        val externRefToKotlinIntAdapter = getInternalFunction("externRefToKotlinIntAdapter")
        val externRefToKotlinBooleanAdapter = getInternalFunction("externRefToKotlinBooleanAdapter")
        val externRefToKotlinLongAdapter = getInternalFunction("externRefToKotlinLongAdapter")
        val externRefToKotlinFloatAdapter = getInternalFunction("externRefToKotlinFloatAdapter")
        val externRefToKotlinDoubleAdapter = getInternalFunction("externRefToKotlinDoubleAdapter")

        val kotlinIntToExternRefAdapter = getInternalFunction("kotlinIntToExternRefAdapter")
        val kotlinBooleanToExternRefAdapter = getInternalFunction("kotlinBooleanToExternRefAdapter")
        val kotlinLongToExternRefAdapter = getInternalFunction("kotlinLongToExternRefAdapter")
        val kotlinFloatToExternRefAdapter = getInternalFunction("kotlinFloatToExternRefAdapter")
        val kotlinDoubleToExternRefAdapter = getInternalFunction("kotlinDoubleToExternRefAdapter")
        val kotlinByteToExternRefAdapter = getInternalFunction("kotlinByteToExternRefAdapter")
        val kotlinShortToExternRefAdapter = getInternalFunction("kotlinShortToExternRefAdapter")
        val kotlinCharToExternRefAdapter = getInternalFunction("kotlinCharToExternRefAdapter")
    }

    val jsInteropAdapters = JsInteropAdapters()

    private val jsExportClass = getClass(FqName("kotlin.js.JsExport"))
    val jsExportConstructor by lazy { jsExportClass.constructors.single() }

    private val jsNameClass = getClass(FqName("kotlin.js.JsName"))
    val jsNameConstructor by lazy { jsNameClass.constructors.single() }

    private val jsFunClass = getClass(FqName("kotlin.JsFun"))
    val jsFunConstructor by lazy { jsFunClass.constructors.single() }

    val jsCode = getFunction(kotlinJsPackage, "js")


    val coroutinePackage = module.getPackage(COROUTINE_PACKAGE_FQNAME)
    val coroutineIntrinsicsPackage = module.getPackage(COROUTINE_INTRINSICS_PACKAGE_FQNAME)

    override val coroutineImpl = getClass(coroutinePackage.memberScope, COROUTINE_IMPL_NAME)

    val coroutineImplLabelPropertyGetter by lazy(LazyThreadSafetyMode.NONE) { with(birTreeContext) { coroutineImpl.getPropertyGetter("state")!! } }
    val coroutineImplLabelPropertySetter by lazy(LazyThreadSafetyMode.NONE) { with(birTreeContext) { coroutineImpl.getPropertySetter("state")!! } }
    val coroutineImplResultSymbolGetter by lazy(LazyThreadSafetyMode.NONE) { with(birTreeContext) { coroutineImpl.getPropertyGetter("result")!! } }
    val coroutineImplResultSymbolSetter by lazy(LazyThreadSafetyMode.NONE) { with(birTreeContext) { coroutineImpl.getPropertySetter("result")!! } }
    val coroutineImplExceptionPropertyGetter by lazy(LazyThreadSafetyMode.NONE) { with(birTreeContext) { coroutineImpl.getPropertyGetter("exception")!! } }
    val coroutineImplExceptionPropertySetter by lazy(LazyThreadSafetyMode.NONE) { with(birTreeContext) { coroutineImpl.getPropertySetter("exception")!! } }
    val coroutineImplExceptionStatePropertyGetter by lazy(LazyThreadSafetyMode.NONE) {
        with(birTreeContext) {
            coroutineImpl.getPropertyGetter(
                "exceptionState"
            )!!
        }
    }
    val coroutineImplExceptionStatePropertySetter by lazy(LazyThreadSafetyMode.NONE) {
        with(birTreeContext) {
            coroutineImpl.getPropertySetter(
                "exceptionState"
            )!!
        }
    }

    override val continuationClass = getClass(coroutinePackage.memberScope, CONTINUATION_NAME)

    override val coroutineSuspendedGetter =
        mapSymbolOwner<_, BirSimpleFunction>(
            symbolTable.referenceSimpleFunction(
                coroutineIntrinsicsPackage.memberScope.getContributedVariables(
                    COROUTINE_SUSPENDED_NAME,
                    NoLookupLocation.FROM_BACKEND
                ).filterNot { it.isExpect }.single().getter!!
            )
        )

    val coroutineContextProperty: BirProperty = getProperty(coroutinePackage.memberScope, COROUTINE_CONTEXT_NAME)
    override val coroutineContextGetter = with(birTreeContext) { coroutineContextProperty.getter!! }

    fun getKFunctionType(type: BirType, list: List<BirType>): BirType {
        return TODO() //birBuiltIns.functionN(list.size).typeWith(list + type)
    }


    private fun findClass(memberScope: MemberScope, name: Name): ClassDescriptor =
        memberScope.getContributedClassifier(name, NoLookupLocation.FROM_BACKEND) as ClassDescriptor

    private fun findFunctions(memberScope: MemberScope, name: Name): List<SimpleFunctionDescriptor> =
        memberScope.getContributedFunctions(name, NoLookupLocation.FROM_BACKEND).toList()

    private fun findProperty(memberScope: MemberScope, name: Name): List<PropertyDescriptor> =
        memberScope.getContributedVariables(name, NoLookupLocation.FROM_BACKEND).toList()


    private fun getClass(memberScope: MemberScope, name: Name): BirClass {
        val descriptor = findClass(memberScope, name)
        val ir = symbolTable.referenceClass(descriptor)
        return mapSymbolOwner(ir)
    }

    private fun getClass(fqName: FqName): BirClass = getClass(module.getPackage(fqName.parent()).memberScope, fqName.shortName())

    private fun getInternalClass(name: String): BirClass = getClass(FqName("kotlin.wasm.internal.$name"))


    private fun getFunction(ownerPackage: PackageViewDescriptor, name: String): BirSimpleFunction =
        maybeGetFunction(ownerPackage, name) ?: throw IllegalArgumentException("Function $name not found")

    private fun maybeGetFunction(ownerPackage: PackageViewDescriptor, name: String): BirSimpleFunction? {
        val descriptor = findFunctions(ownerPackage.memberScope, Name.identifier(name))
        if (descriptor.isEmpty())
            return null
        val ir = symbolTable.referenceSimpleFunction(descriptor.single())
        return mapSymbolOwner(ir)
    }

    private fun getInternalFunction(name: String) = getFunction(wasmInternalPackage, name)


    private fun getProperty(fqName: FqName): BirProperty = getProperty(module.getPackage(fqName.parent()).memberScope, fqName.shortName())

    private fun getProperty(memberScope: MemberScope, name: Name): BirProperty {
        val descriptor = findProperty(memberScope, name).single()
        val ir = symbolTable.referenceProperty(descriptor)
        return mapSymbolOwner(ir)
    }


    private fun <Ir : IrElement, Bir : BirElement> mapElement(element: Ir): Bir {
        with(birTreeContext) {
            return converter.mapIrElement(element) as Bir
        }
    }

    private fun <IrS : IrSymbol, Bir : BirElement> mapSymbolOwner(symbol: IrS): Bir {
        return mapElement(symbol.owner)
    }

    companion object {
        private val INTRINSICS_PACKAGE_NAME = Name.identifier("intrinsics")
        private val COROUTINE_SUSPENDED_NAME = Name.identifier("COROUTINE_SUSPENDED")
        private val COROUTINE_CONTEXT_NAME = Name.identifier("coroutineContext")
        private val COROUTINE_IMPL_NAME = Name.identifier("CoroutineImpl")
        private val CONTINUATION_NAME = Name.identifier("Continuation")
        private val CONTINUATION_CONTEXT_GETTER_NAME = Name.special("<get-context>")
        private val CONTINUATION_CONTEXT_PROPERTY_NAME = Name.identifier("context")
        private val COROUTINE_PACKAGE_FQNAME = FqName.fromSegments(listOf("kotlin", "coroutines"))
        private val COROUTINE_INTRINSICS_PACKAGE_FQNAME = COROUTINE_PACKAGE_FQNAME.child(INTRINSICS_PACKAGE_NAME)
    }
}