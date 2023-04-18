/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.generator

import com.squareup.kotlinpoet.KModifier
import org.jetbrains.kotlin.bir.generator.config.AbstractTreeBuilder
import org.jetbrains.kotlin.bir.generator.config.ElementConfig
import org.jetbrains.kotlin.bir.generator.config.ElementConfig.Category.*
import org.jetbrains.kotlin.bir.generator.config.ListFieldConfig.Mutability.*
import org.jetbrains.kotlin.bir.generator.config.ListFieldConfig.Mutability.Array
import org.jetbrains.kotlin.bir.generator.config.ListFieldConfig.Mutability.List
import org.jetbrains.kotlin.bir.generator.config.SimpleFieldConfig
import org.jetbrains.kotlin.bir.generator.model.Element.Companion.elementName2typeName
import org.jetbrains.kotlin.bir.generator.util.*
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.ValueClassRepresentation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.types.Variance

// Note the style of the DSL to describe BIR elements, which is these things in the following order:
// 1) config (see properties of ElementConfig)
// 2) parents
// 3) fields
object BirTree : AbstractTreeBuilder() {
    private fun descriptor(typeName: String, initializer: SimpleFieldConfig.() -> Unit = {}): SimpleFieldConfig = field(
        "descriptor",
        ClassRef<TypeParameterRef>(TypeKind.Interface, "org.jetbrains.kotlin.descriptors", typeName),
        mutable = false,
        initializer = initializer
    )

    override val rootElement: ElementConfig by element(Other, name = "element") {
        fun offsetField(prefix: String) = field(prefix + "Offset", int, mutable = false) {
            kdoc = """
            The $prefix offset of the syntax node from which this BIR node was generated,
            in number of characters from the start of the source file. If there is no source information for this BIR node,
            the [UNDEFINED_OFFSET] constant is used. In order to get the line number and the column number from this offset,
            [IrFileEntry.getLineNumber] and [IrFileEntry.getColumnNumber] can be used.
            
            @see IrFileEntry.getSourceRangeInfo
            """.trimIndent()
        }

        +offsetField("start")
        +offsetField("end")

        kDoc = "The root interface of the BIR tree. Each BIR node implements this interface."
    }
    val statement: ElementConfig by element(Other)

    val declaration: ElementConfig by element(Declaration) {
        parent(statement)
        parent(annotationContainerElement)
        parent(symbolElement)

        +descriptor("DeclarationDescriptor") {
            generationCallback = {
                addModifiers(KModifier.OVERRIDE)
            }
        }
        +field("origin", type("org.jetbrains.kotlin.ir.declarations", "IrDeclarationOrigin"))
    }

    // todo: probably remove
    val declarationBase: ElementConfig by element(Declaration) {
        typeKind = TypeKind.Class
        parent(declaration)
    }
    val declarationWithVisibility: ElementConfig by element(Declaration) {
        parent(declaration)

        +field("visibility", type(Packages.descriptors, "DescriptorVisibility"))
    }
    val declarationWithName: ElementConfig by element(Declaration) {
        parent(declaration)

        +field("name", type<Name>())
    }
    val possiblyExternalDeclaration: ElementConfig by element(Declaration) {
        parent(declarationWithName)

        +field("isExternal", boolean)
    }
    val overridableMember: ElementConfig by element(Declaration) {
        parent(declaration)
        parent(declarationWithVisibility)
        parent(declarationWithName)
        parent(symbolElement)

        +field("modality", type<Modality>())
    }
    val overridableDeclaration: ElementConfig by element(Declaration) {
        val s = +param("S", symbolType)

        parent(overridableMember)
        parent(symbolType)
        parent(symbolElement)

        +field("isFakeOverride", boolean)
        +listField("overriddenSymbols", s, mutability = Var)
    }
    val memberWithContainerSource: ElementConfig by element(Declaration) {
        parent(declarationWithName)

        +field("containerSource", type<DeserializedContainerSource>(), nullable = true, mutable = false)
    }
    val valueDeclaration: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.value

        parent(declarationWithName)

        +descriptor("ValueDescriptor")
        +field("type", irTypeType)
        +field("isAssignable", boolean, mutable = false)
    }
    val valueParameter: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.valueParameter

        parent(declarationBase)
        parent(valueDeclaration)

        +descriptor("ParameterDescriptor")
        +field("index", int)
        +field("varargElementType", irTypeType, nullable = true)
        +field("isCrossinline", boolean)
        +field("isNoinline", boolean)
        // if true parameter is not included into IdSignature.
        // Skipping hidden params makes IrFunction be look similar to FE.
        // NOTE: it is introduced to fix KT-40980 because more clear solution was not possible to implement.
        // Once we are able to load any top-level declaration from klib this hack should be deprecated and removed.
        +field("isHidden", boolean)
        +field("defaultValue", expressionBody, nullable = true, isChild = true)
    }
    val `class`: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.`class`

        parent(declarationBase)
        parent(possiblyExternalDeclaration)
        parent(declarationWithVisibility)
        parent(typeParametersContainer)
        parent(declarationContainer)
        parent(attributeContainer)

        +descriptor("ClassDescriptor")
        +field("kind", type<ClassKind>())
        +field("modality", type<Modality>())
        +field("isCompanion", boolean)
        +field("isInner", boolean)
        +field("isData", boolean)
        +field("isValue", boolean)
        +field("isExpect", boolean)
        +field("isFun", boolean)
        +field("source", type<SourceElement>(), mutable = false)
        +listField("superTypes", irTypeType, mutability = Var)
        +field("thisReceiver", valueParameter, nullable = true, isChild = true)
        +field(
            "valueClassRepresentation",
            type<ValueClassRepresentation<*>>().withArgs(type("org.jetbrains.kotlin.ir.types", "IrSimpleType")),
            nullable = true,
        )
        +listField("sealedSubclasses", SymbolTypes.`class`, mutability = Var) {
            kdoc = """
            If this is a sealed class or interface, this list contains symbols of all its immediate subclasses.
            Otherwise, this is an empty list.
            
            NOTE: If this [${elementName2typeName(this@element.name)}] was deserialized from a klib, this list will always be empty!
            See [KT-54028](https://youtrack.jetbrains.com/issue/KT-54028).
            """.trimIndent()
        }
    }
    val attributeContainer: ElementConfig by element(Declaration) {
        kDoc = """
            Represents an BIR element that can be copied, but must remember its original element. It is
            useful, for example, to keep track of generated names for anonymous declarations.
            @property attributeOwnerId original element before copying. Always satisfies the following
              invariant: `this.attributeOwnerId == this.attributeOwnerId.attributeOwnerId`.
            @property originalBeforeInline original element before inlining. Useful only with BIR
              inliner. `null` if the element wasn't inlined. Unlike [attributeOwnerId], doesn't have the
              idempotence invariant and can contain a chain of declarations.
        """.trimIndent()

        +field("attributeOwnerId", attributeContainer) {
            initializeToThis = true
        }
        +field("originalBeforeInline", attributeContainer, nullable = true) // null <=> this element wasn't inlined
    }

    // Equivalent of IrMutableAnnotationContainer which is not an IR element (but could be)
    val annotationContainerElement: ElementConfig by element(Declaration) {
        +listField("annotations", constructorCall, mutability = Var)
    }
    val anonymousInitializer: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.anonymousInitializer

        parent(declarationBase)

        +descriptor("ClassDescriptor") // TODO special descriptor for anonymous initializer blocks
        +field("isStatic", boolean)
        +field("body", blockBody, isChild = true)
    }
    val declarationContainer: ElementConfig by element(Declaration) {
        +listField("declarations", declaration, mutability = List, isChild = true)
    }
    val typeParametersContainer: ElementConfig by element(Declaration) {
        parent(declaration)

        +listField("typeParameters", typeParameter, mutability = Var, isChild = true)
    }
    val typeParameter: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.typeParameter

        parent(declarationBase)
        parent(declarationWithName)

        +descriptor("TypeParameterDescriptor")
        +field("variance", type<Variance>())
        +field("index", int)
        +field("isReified", boolean)
        +listField("superTypes", irTypeType, mutability = Var)
    }
    val returnTarget: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.returnTarget

        +descriptor("FunctionDescriptor") {
            generationCallback = {
                addModifiers(KModifier.OVERRIDE)
            }
        }
    }
    val function: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.function

        parent(declarationBase)
        parent(possiblyExternalDeclaration)
        parent(declarationWithVisibility)
        parent(typeParametersContainer)
        parent(returnTarget)
        parent(memberWithContainerSource)

        +descriptor("FunctionDescriptor")
        // NB: there's an inline constructor for Array and each primitive array class.
        +field("isInline", boolean)
        +field("isExpect", boolean)
        +field("returnType", irTypeType)
        +field("dispatchReceiverParameter", valueParameter, nullable = true, isChild = true)
        +field("extensionReceiverParameter", valueParameter, nullable = true, isChild = true)
        +listField("valueParameters", valueParameter, mutability = Var, isChild = true)
        // The first `contextReceiverParametersCount` value parameters are context receivers.
        +field("contextReceiverParametersCount", int)
        +field("body", body, nullable = true, isChild = true)
    }
    val constructor: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.constructor

        parent(function)

        +descriptor("ClassConstructorDescriptor")
        +field("isPrimary", boolean)
    }
    val enumEntry: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.enumEntry

        parent(declarationBase)
        parent(declarationWithName)

        +descriptor("ClassDescriptor")
        +field("initializerExpression", expressionBody, nullable = true, isChild = true)
        +field("correspondingClass", `class`, nullable = true, isChild = true)
    }
    val errorDeclaration: ElementConfig by element(Declaration) {
        parent(declarationBase)
    }
    val functionWithLateBinding: ElementConfig by element(Declaration) {
        parent(simpleFunction)

        +field("isElementBound", boolean, mutable = false) // isBound would collide with IrSymbol.isBound
    }
    val propertyWithLateBinding: ElementConfig by element(Declaration) {
        parent(property)

        +field("isElementBound", boolean, mutable = false) // isBound would collide with IrSymbol.isBound
    }
    val field: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.field

        parent(declarationBase)
        parent(possiblyExternalDeclaration)
        parent(declarationWithVisibility)

        +descriptor("PropertyDescriptor")
        +field("type", irTypeType)
        +field("isFinal", boolean)
        +field("isStatic", boolean)
        +field("initializer", expressionBody, nullable = true, isChild = true)
        +field("correspondingProperty", SymbolTypes.property, nullable = true)
    }
    val localDelegatedProperty: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.localDelegatedProperty

        parent(declarationBase)
        parent(declarationWithName)

        +descriptor("VariableDescriptorWithAccessors")
        +field("type", irTypeType)
        +field("isVar", boolean)
        +field("delegate", variable, isChild = true)
        +field("getter", simpleFunction, isChild = true)
        +field("setter", simpleFunction, nullable = true, isChild = true)
    }
    val moduleFragment: ElementConfig by element(Declaration) {
        +descriptor("ModuleDescriptor")
        +field("name", type<Name>(), mutable = false)
        +field("irBuiltins", type("org.jetbrains.kotlin.ir", "IrBuiltIns"), mutable = false)
        +listField("files", file, mutability = List, isChild = true)
        +field("startOffset", int, mutable = false)
        +field("endOffset", int, mutable = false)
    }
    val property: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.property
        hasImpl = true

        parent(declarationBase)
        parent(possiblyExternalDeclaration)
        parent(overridableDeclaration.withArgs("S" to SymbolTypes.property))
        parent(attributeContainer)
        parent(memberWithContainerSource)

        +descriptor("PropertyDescriptor")
        +field("isVar", boolean)
        +field("isConst", boolean)
        +field("isLateinit", boolean)
        +field("isDelegated", boolean)
        +field("isExpect", boolean)
        +field("isFakeOverride", boolean)
        +field("backingField", field, nullable = true, isChild = true)
        +field("getter", simpleFunction, nullable = true, isChild = true)
        +field("setter", simpleFunction, nullable = true, isChild = true)
        +listField("overriddenSymbols", SymbolTypes.property, mutability = Var)
    }

    //TODO: make BirScript as BirPackageFragment, because script is used as a file, not as a class
    //NOTE: declarations and statements stored separately
    val script: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.script

        parent(declarationBase)
        parent(declarationWithName)
        parent(statementContainer)

        // NOTE: is the result of the FE conversion, because there script interpreted as a class and has receiver
        // TODO: consider removing from here and handle appropriately in the lowering
        +descriptor("ScriptDescriptor")
        +field("thisReceiver", valueParameter, isChild = true, nullable = true) // K1
        +field("baseClass", irTypeType, nullable = true) // K1
        +listField("explicitCallParameters", variable, mutability = Var, isChild = true)
        +listField("implicitReceiversParameters", valueParameter, mutability = Var, isChild = true)
        +listField("providedProperties", SymbolTypes.property, mutability = Var)
        +listField("providedPropertiesParameters", valueParameter, mutability = Var, isChild = true)
        +field("resultProperty", SymbolTypes.property, nullable = true)
        +field("earlierScriptsParameter", valueParameter, nullable = true, isChild = true)
        +listField("earlierScripts", SymbolTypes.script, mutability = Var, nullable = true)
        +field("targetClass", SymbolTypes.`class`, nullable = true)
        +field("constructor", constructor, nullable = true) // K1
    }
    val simpleFunction: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.simpleFunction
        hasImpl = true

        parent(function)
        parent(overridableDeclaration.withArgs("S" to SymbolTypes.simpleFunction))
        parent(attributeContainer)

        +field("isTailrec", boolean)
        +field("isSuspend", boolean)
        +field("isFakeOverride", boolean)
        +field("isOperator", boolean)
        +field("isInfix", boolean)
        +field("correspondingProperty", SymbolTypes.property, nullable = true)
        +listField("overriddenSymbols", SymbolTypes.simpleFunction, mutability = Var)
    }
    val typeAlias: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.typeAlias

        parent(declarationBase)
        parent(declarationWithName)
        parent(declarationWithVisibility)
        parent(typeParametersContainer)

        +descriptor("TypeAliasDescriptor")
        +field("isActual", boolean)
        +field("expandedType", irTypeType)
    }
    val variable: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.variable

        parent(declarationBase)
        parent(valueDeclaration)

        +descriptor("VariableDescriptor")
        +field("isVar", boolean)
        +field("isConst", boolean)
        +field("isLateinit", boolean)
        +field("initializer", expression, nullable = true, isChild = true)
    }
    val packageFragment: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.packageFragment

        parent(declarationContainer)

        +descriptor("PackageFragmentDescriptor") {
            generationCallback = {
                addModifiers(KModifier.OVERRIDE)
            }
        }
        +field("fqName", type<FqName>())
    }
    val externalPackageFragment: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.externalPackageFragment

        parent(packageFragment)

        +field("containerSource", type<DeserializedContainerSource>(), nullable = true, mutable = false)
    }
    val file: ElementConfig by element(Declaration) {
        symbol = SymbolTypes.file

        parent(packageFragment)
        parent(annotationContainerElement)

        +field("module", moduleFragment) // todo: maybe remove and make a extension property that searches parents?
        +field("fileEntry", type("org.jetbrains.kotlin.ir", "IrFileEntry"))
    }

    val expression: ElementConfig by element(Expression) {
        parent(statement)
        parent(varargElement)
        parent(attributeContainer)

        +field("type", irTypeType)
    }
    val statementContainer: ElementConfig by element(Expression) {
        +listField("statements", statement, mutability = List, isChild = true)
    }
    val body: ElementConfig by element(Expression) {
        typeKind = TypeKind.Class
    }
    val expressionBody: ElementConfig by element(Expression) {
        parent(body)

        +field("expression", expression, isChild = true)
    }
    val blockBody: ElementConfig by element(Expression) {
        parent(body)
        parent(statementContainer)
    }
    val declarationReference: ElementConfig by element(Expression) {
        parent(expression)

        +field("target", symbolType, mutable = false)
    }
    val memberAccessExpression: ElementConfig by element(Expression) {
        val s = +param("S", symbolType)

        parent(declarationReference)

        +field("dispatchReceiver", expression, nullable = true, isChild = true)
        +field("extensionReceiver", expression, nullable = true, isChild = true)
        +field("target", s, mutable = false)
        +field("origin", statementOriginType, nullable = true)
        +listField("valueArguments", expression, mutability = Array, isChild = true)
        +listField("typeArguments", irTypeType.copy(nullable = true), mutability = Array)
    }
    val functionAccessExpression: ElementConfig by element(Expression) {
        parent(memberAccessExpression.withArgs("S" to SymbolTypes.function))

        +field("contextReceiversCount", int)
    }
    val constructorCall: ElementConfig by element(Expression) {
        parent(functionAccessExpression)

        +field("target", SymbolTypes.constructor)
        +field("source", type<SourceElement>())
        +field("constructorTypeArgumentsCount", int)
    }
    val getSingletonValue: ElementConfig by element(Expression) {
        parent(declarationReference)
    }
    val getObjectValue: ElementConfig by element(Expression) {
        parent(getSingletonValue)

        +field("target", SymbolTypes.`class`)
    }
    val getEnumValue: ElementConfig by element(Expression) {
        parent(getSingletonValue)

        +field("target", SymbolTypes.enumEntry)
    }

    /**
     * Platform-specific low-level reference to function.
     *
     * On JS platform it represents a plain reference to JavaScript function.
     * On JVM platform it represents a MethodHandle constant.
     */
    val rawFunctionReference: ElementConfig by element(Expression) {
        parent(declarationReference)

        +field("target", SymbolTypes.function)
    }
    val containerExpression: ElementConfig by element(Expression) {
        parent(expression)
        parent(statementContainer)

        +field("origin", statementOriginType, nullable = true)
        +listField("statements", statement, mutability = List, isChild = true)
    }
    val block: ElementConfig by element(Expression) {
        hasImpl = true

        parent(containerExpression)
    }
    val composite: ElementConfig by element(Expression) {
        parent(containerExpression)
    }
    val returnableBlock: ElementConfig by element(Expression) {
        symbol = SymbolTypes.returnableBlock

        parent(block)
        parent(returnTarget)
    }
    val inlinedFunctionBlock: ElementConfig by element(Expression) {
        parent(block)

        +field("inlineCall", functionAccessExpression)
        +field("inlinedElement", rootElement)
    }
    val syntheticBody: ElementConfig by element(Expression) {
        parent(body)

        +field("kind", type("org.jetbrains.kotlin.ir.expressions", "IrSyntheticBodyKind"))
    }
    val breakContinue: ElementConfig by element(Expression) {
        parent(expression)

        +field("loop", loop)
        +field("label", string, nullable = true)
    }
    val `break` by element(Expression) {
        parent(breakContinue)
    }
    val `continue` by element(Expression) {
        parent(breakContinue)
    }
    val call: ElementConfig by element(Expression) {
        parent(functionAccessExpression)

        +field("target", SymbolTypes.simpleFunction)
        +field("superQualifier", SymbolTypes.`class`, nullable = true)
    }
    val callableReference: ElementConfig by element(Expression) {
        val s = +param("S", symbolType)

        parent(memberAccessExpression.withArgs("S" to s))
    }
    val functionReference: ElementConfig by element(Expression) {
        parent(callableReference.withArgs("S" to SymbolTypes.function))

        +field("target", SymbolTypes.function)
        +field("reflectionTarget", SymbolTypes.function, nullable = true)
    }
    val propertyReference: ElementConfig by element(Expression) {
        parent(callableReference.withArgs("S" to SymbolTypes.property))

        +field("target", SymbolTypes.property)
        +field("field", SymbolTypes.field, nullable = true)
        +field("getter", SymbolTypes.simpleFunction, nullable = true)
        +field("setter", SymbolTypes.simpleFunction, nullable = true)
    }
    val localDelegatedPropertyReference: ElementConfig by element(Expression) {
        parent(callableReference.withArgs("S" to SymbolTypes.localDelegatedProperty))

        +field("target", SymbolTypes.localDelegatedProperty)
        +field("delegate", SymbolTypes.variable)
        +field("getter", SymbolTypes.simpleFunction)
        +field("setter", SymbolTypes.simpleFunction, nullable = true)
    }
    val classReference: ElementConfig by element(Expression) {
        parent(declarationReference)

        +field("target", SymbolTypes.classifier)
        +field("classType", irTypeType)
    }
    val const: ElementConfig by element(Expression) {
        val t = +param("T")

        parent(expression)

        +field("kind", type("org.jetbrains.kotlin.ir.expressions", "IrConstKind").withArgs(t))
        +field("value", t)
    }
    val constantValue: ElementConfig by element(Expression) {
        parent(expression)
    }
    val constantPrimitive: ElementConfig by element(Expression) {
        parent(constantValue)

        +field("value", const.withArgs("T" to TypeRef.Star), isChild = true)
    }
    val constantObject: ElementConfig by element(Expression) {
        parent(constantValue)

        +field("constructor", SymbolTypes.constructor)
        +listField("valueArguments", constantValue, mutability = List, isChild = true)
        +listField("typeArguments", irTypeType)
    }
    val constantArray: ElementConfig by element(Expression) {
        parent(constantValue)

        +listField("elements", constantValue, mutability = List, isChild = true)
    }
    val delegatingConstructorCall: ElementConfig by element(Expression) {
        parent(functionAccessExpression)

        +field("target", SymbolTypes.constructor)
    }
    val dynamicExpression: ElementConfig by element(Expression) {
        parent(expression)
    }
    val dynamicOperatorExpression: ElementConfig by element(Expression) {
        parent(dynamicExpression)

        +field("operator", type("org.jetbrains.kotlin.ir.expressions", "IrDynamicOperator"))
        +field("receiver", expression, isChild = true)
        +listField("arguments", expression, mutability = List, isChild = true)
    }
    val dynamicMemberExpression: ElementConfig by element(Expression) {
        parent(dynamicExpression)

        +field("memberName", string)
        +field("receiver", expression, isChild = true)
    }
    val enumConstructorCall: ElementConfig by element(Expression) {
        parent(functionAccessExpression)

        +field("target", SymbolTypes.constructor)
    }
    val errorExpression: ElementConfig by element(Expression) {
        parent(expression)

        +field("description", string)
    }
    val errorCallExpression: ElementConfig by element(Expression) {
        parent(errorExpression)

        +field("explicitReceiver", expression, nullable = true, isChild = true)
        +listField("arguments", expression, mutability = List, isChild = true)
    }
    val fieldAccessExpression: ElementConfig by element(Expression) {
        parent(declarationReference)

        +field("target", SymbolTypes.field)
        +field("superQualifier", SymbolTypes.`class`, nullable = true)
        +field("receiver", expression, nullable = true, isChild = true)
        +field("origin", statementOriginType, nullable = true)
    }
    val getField: ElementConfig by element(Expression) {
        parent(fieldAccessExpression)
    }
    val setField: ElementConfig by element(Expression) {
        parent(fieldAccessExpression)

        +field("value", expression, isChild = true)
    }
    val functionExpression: ElementConfig by element(Expression) {
        parent(expression)

        +field("origin", statementOriginType)
        +field("function", simpleFunction, isChild = true)
    }
    val getClass: ElementConfig by element(Expression) {
        parent(expression)

        +field("argument", expression, isChild = true)
    }
    val instanceInitializerCall: ElementConfig by element(Expression) {
        parent(expression)

        +field("class", SymbolTypes.`class`)
    }
    val loop: ElementConfig by element(Expression) {
        parent(expression)

        +field("origin", statementOriginType, nullable = true)
        +field("body", expression, nullable = true, isChild = true)
        +field("condition", expression, isChild = true)
        +field("label", string, nullable = true)
    }
    val whileLoop: ElementConfig by element(Expression) {
        childrenOrderOverride = listOf("condition", "body")

        parent(loop)
    }
    val doWhileLoop: ElementConfig by element(Expression) {
        parent(loop)
    }
    val `return`: ElementConfig by element(Expression) {
        parent(expression)

        +field("value", expression, isChild = true)
        +field("returnTarget", SymbolTypes.returnTarget)
    }
    val stringConcatenation: ElementConfig by element(Expression) {
        parent(expression)

        +listField("arguments", expression, mutability = List, isChild = true)
    }
    val suspensionPoint: ElementConfig by element(Expression) {
        parent(expression)

        +field("suspensionPointIdParameter", variable, isChild = true)
        +field("result", expression, isChild = true)
        +field("resumeResult", expression, isChild = true)
    }
    val suspendableExpression: ElementConfig by element(Expression) {
        parent(expression)

        +field("suspensionPointId", expression, isChild = true)
        +field("result", expression, isChild = true)
    }
    val `throw`: ElementConfig by element(Expression) {
        parent(expression)

        +field("value", expression, isChild = true)
    }
    val `try`: ElementConfig by element(Expression) {
        parent(expression)

        +field("tryResult", expression, isChild = true)
        +listField("catches", catch, mutability = List, isChild = true)
        +field("finallyExpression", expression, nullable = true, isChild = true)
    }
    val catch: ElementConfig by element(Expression) {
        +field("catchParameter", variable, isChild = true)
        +field("result", expression, isChild = true)
    }
    val typeOperatorCall: ElementConfig by element(Expression) {
        parent(expression)

        +field("operator", type("org.jetbrains.kotlin.ir.expressions", "IrTypeOperator"))
        +field("argument", expression, isChild = true)
        +field("typeOperand", irTypeType)
    }
    val valueAccessExpression: ElementConfig by element(Expression) {
        parent(declarationReference)

        +field("target", SymbolTypes.value)
        +field("origin", statementOriginType, nullable = true)
    }
    val getValue: ElementConfig by element(Expression) {
        parent(valueAccessExpression)
    }
    val setValue: ElementConfig by element(Expression) {
        parent(valueAccessExpression)

        +field("target", SymbolTypes.value)
        +field("value", expression, isChild = true)
    }
    val varargElement: ElementConfig by element(Expression)
    val vararg: ElementConfig by element(Expression) {
        parent(expression)

        +field("varargElementType", irTypeType)
        +listField("elements", varargElement, mutability = List, isChild = true)
    }
    val spreadElement: ElementConfig by element(Expression) {
        parent(varargElement)

        +field("expression", expression, isChild = true)
    }
    val `when`: ElementConfig by element(Expression) {
        parent(expression)

        +field("origin", statementOriginType, nullable = true)
        +listField("branches", branch, mutability = List, isChild = true)
    }
    val branch: ElementConfig by element(Expression) {
        hasImpl = true

        +field("condition", expression, isChild = true)
        +field("result", expression, isChild = true)
    }
    val elseBranch: ElementConfig by element(Expression) {
        parent(branch)
    }
}
