/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.utils

import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirTreeContext
import org.jetbrains.kotlin.bir.DummyBirTreeContext
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.symbols.*
import org.jetbrains.kotlin.bir.types.*
import org.jetbrains.kotlin.bir.types.impl.ReturnTypeIsNotInitializedException
import org.jetbrains.kotlin.bir.types.utils.isMarkedNullable
import org.jetbrains.kotlin.bir.types.utils.originalKotlinType
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.types.SimpleTypeNullability
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

fun BirElement.render() = with(DummyBirTreeContext) {
    RenderBirElementVisitor().render(this@render)
}

context(BirTreeContext)
internal class RenderBirElementVisitor(normalizeNames: Boolean = false, private val verboseErrorTypes: Boolean = true) {
    private val variableNameData = VariableNameData(normalizeNames)

    fun renderType(type: BirType) = type.renderTypeWithRenderer(this@RenderBirElementVisitor, verboseErrorTypes)

    fun renderSymbolReference(symbol: BirSymbol) = symbol.renderReference()

    fun renderAsAnnotation(irAnnotation: BirConstructorCall): String =
        StringBuilder().also { it.renderAsAnnotation(irAnnotation, this, verboseErrorTypes) }.toString()

    private fun BirType.render(): String =
        this.renderTypeWithRenderer(this@RenderBirElementVisitor, verboseErrorTypes)

    private fun BirSymbol.renderReference() =
        if (this is BirElement)
            BoundSymbolReferenceRenderer(variableNameData, verboseErrorTypes, this)
        else
            "UNBOUND ${getCorrespondingIrClassName(javaClass)}"

    private fun BoundSymbolReferenceRenderer(
        variableNameData: VariableNameData,
        verboseErrorTypes: Boolean,
        element: BirElement,
    ): String = when (element) {
        is BirTypeParameter -> renderTypeParameter(element, null, verboseErrorTypes)
        is BirClass -> renderClassWithRenderer(element, null, verboseErrorTypes)
        is BirEnumEntry -> renderEnumEntry(element)
        is BirField -> renderField(element, null, verboseErrorTypes)
        is BirVariable -> buildTrimEnd {
            if (element.isVar) append("var ") else append("val ")

            append(element.normalizedName(variableNameData))
            append(": ")
            append(element.type.renderTypeWithRenderer(null, verboseErrorTypes))
            append(' ')

            append(element.renderVariableFlags())

            renderDeclaredIn(element)
        }
        is BirValueParameter -> buildTrimEnd {
            append(element.name.asString())
            append(": ")
            append(element.type.renderTypeWithRenderer(null, verboseErrorTypes))
            append(' ')

            append(element.renderValueParameterFlags())

            renderDeclaredIn(element)
        }
        is BirFunction -> buildTrimEnd {
            append(element.visibility)
            append(' ')

            if (element is BirSimpleFunction) {
                append(element.modality.toString().toLowerCaseAsciiOnly())
                append(' ')
            }

            when (element) {
                is BirSimpleFunction -> append("fun ")
                is BirConstructor -> append("constructor ")
                else -> append("{${getCorrespondingIrClassName(element.javaClass)}}")
            }

            append(element.name.asString())
            append(' ')

            renderTypeParameters(element)

            appendIterableWith(element.valueParameters, "(", ")", ", ") { valueParameter ->
                val varargElementType = valueParameter.varargElementType
                if (varargElementType != null) {
                    append("vararg ")
                    append(valueParameter.name.asString())
                    append(": ")
                    append(varargElementType.renderTypeWithRenderer(null, verboseErrorTypes))
                } else {
                    append(valueParameter.name.asString())
                    append(": ")
                    append(valueParameter.type.renderTypeWithRenderer(null, verboseErrorTypes))
                }
            }

            if (element is BirSimpleFunction) {
                append(": ")
                append(element.renderReturnType(null, verboseErrorTypes))
            }
            append(' ')

            when (element) {
                is BirSimpleFunction -> append(element.renderSimpleFunctionFlags())
                is BirConstructor -> append(element.renderConstructorFlags())
            }

            renderDeclaredIn(element)
        }
        is BirProperty -> buildTrimEnd {
            append(element.visibility)
            append(' ')
            append(element.modality.toString().toLowerCaseAsciiOnly())
            append(' ')

            append(element.name.asString())

            val getter = element.getter
            if (getter != null) {
                append(": ")
                append(getter.renderReturnType(null, verboseErrorTypes))
            } else element.backingField?.type?.let { type ->
                append(": ")
                append(type.renderTypeWithRenderer(null, verboseErrorTypes))
            }

            append(' ')
            append(element.renderPropertyFlags())
        }
        is BirLocalDelegatedProperty -> buildTrimEnd {
            if (element.isVar) append("var ") else append("val ")
            append(element.name.asString())
            append(": ")
            append(element.type.renderTypeWithRenderer(null, verboseErrorTypes))
            append(" by (...)")
        }
        else -> buildTrimEnd {
            append('{')
            append(getCorrespondingIrClassName(element.javaClass))
            append('}')
            if (element is BirDeclaration) {
                if (element is BirDeclarationWithName) {
                    append(element.name)
                    append(' ')
                }
                renderDeclaredIn(element)
            }
        }
    }

    private fun StringBuilder.renderTypeParameters(declaration: BirTypeParametersContainer) {
        if (declaration.typeParameters.isNotEmpty()) {
            appendIterableWith(declaration.typeParameters, "<", ">", ", ") { typeParameter ->
                append(typeParameter.name.asString())
            }
            append(' ')
        }
    }

    private fun StringBuilder.renderDeclaredIn(irDeclaration: BirDeclaration) {
        append("declared in ")
        renderParentOfReferencedDeclaration(irDeclaration)
    }

    private fun StringBuilder.renderParentOfReferencedDeclaration(declaration: BirDeclaration) {
        val parent = declaration.findDeclarationParentOldWay() ?: run {
            append("<no parent>")
            return
        }
        when (parent) {
            is BirPackageFragment -> {
                val fqn = parent.fqName.asString()
                append(fqn.ifEmpty { "<root>" })
            }
            is BirDeclaration -> {
                renderParentOfReferencedDeclaration(parent)
                append('.')
                if (parent is BirDeclarationWithName) {
                    append(parent.name)
                } else {
                    renderElementNameFallback(parent)
                }
            }
            else ->
                renderElementNameFallback(parent)
        }
    }

    private fun StringBuilder.renderElementNameFallback(element: Any) {
        append('{')
        append(getCorrespondingIrClassName(element.javaClass))
        append('}')
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    fun render(element: BirElement): String = when (element) {
        is BirModuleFragment -> "MODULE_FRAGMENT name:${element.name}"
        is BirExternalPackageFragment -> "EXTERNAL_PACKAGE_FRAGMENT fqName:${element.fqName}"
        is BirFile -> "FILE fqName:${element.fqName} fileName:${element.path}"
        is BirScript -> "SCRIPT"
        is BirSimpleFunction -> element.runTrimEnd {
            "FUN ${renderOriginIfNonTrivial()}" +
                    "name:$name visibility:$visibility modality:$modality " +
                    renderTypeParameters() + " " +
                    renderValueParameterTypes() + " " +
                    "returnType:${renderReturnType(this@RenderBirElementVisitor, verboseErrorTypes)} " +
                    renderSimpleFunctionFlags()
        }
        is BirConstructor -> element.runTrimEnd {
            "CONSTRUCTOR ${renderOriginIfNonTrivial()}" +
                    "visibility:$visibility " +
                    renderTypeParameters() + " " +
                    renderValueParameterTypes() + " " +
                    "returnType:${renderReturnType(this@RenderBirElementVisitor, verboseErrorTypes)} " +
                    renderConstructorFlags()
        }
        is BirFunction -> element.runTrimEnd {
            "FUN ${renderOriginIfNonTrivial()}"
        }
        is BirProperty -> element.runTrimEnd {
            "PROPERTY ${renderOriginIfNonTrivial()}" +
                    "name:$name visibility:$visibility modality:$modality " +
                    renderPropertyFlags()
        }
        is BirField -> renderField(element, this, verboseErrorTypes)
        is BirClass -> renderClassWithRenderer(element, this, verboseErrorTypes)
        is BirVariable -> element.runTrimEnd {
            "VAR ${renderOriginIfNonTrivial()}name:${normalizedName(variableNameData)} type:${type.render()} ${renderVariableFlags()}"
        }
        is BirEnumEntry -> renderEnumEntry(element)
        is BirAnonymousInitializer -> "ANONYMOUS_INITIALIZER isStatic=${element.isStatic}"
        is BirValueParameter -> {
            val index = element.getIndex()
            element.runTrimEnd {
                "VALUE_PARAMETER ${renderOriginIfNonTrivial()}" +
                        "name:$name " +
                        (if (index >= 0) "index:$index " else "") +
                        "type:${type.render()} " +
                        (varargElementType?.let { "varargElementType:${it.render()} " } ?: "") +
                        renderValueParameterFlags()
            }
        }
        is BirTypeParameter -> renderTypeParameter(element, this, verboseErrorTypes)
        is BirLocalDelegatedProperty -> element.runTrimEnd {
            "LOCAL_DELEGATED_PROPERTY ${element.renderOriginIfNonTrivial()}" +
                    "name:$name type:${type.render()} flags:${renderLocalDelegatedPropertyFlags()}"
        }
        is BirTypeAlias -> element.run {
            "TYPEALIAS ${element.renderOriginIfNonTrivial()}" +
                    "name:$name visibility:$visibility expandedType:${expandedType.render()}" +
                    renderTypeAliasFlags()
        }
        is BirBlock -> {
            val prefix = when (element) {
                is BirReturnableBlock -> "RETURNABLE_"
                is BirInlinedFunctionBlock -> "INLINED_"
                else -> ""
            }
            "${prefix}BLOCK type=${element.type.render()} origin=${element.origin}"
        }
        is BirExpressionBody -> "EXPRESSION_BODY"
        is BirBlockBody -> "BLOCK_BODY"
        is BirSyntheticBody -> "SYNTHETIC_BODY kind=${element.kind}"
        is BirVararg -> "VARARG type=${element.type.render()} varargElementType=${element.varargElementType.render()}"
        is BirSpreadElement -> "SPREAD_ELEMENT"
        is BirComposite -> "COMPOSITE type=${element.type.render()} origin=${element.origin}"
        is BirReturn -> "RETURN type=${element.type.render()} from='${element.returnTarget.renderReference()}'"
        is BirErrorCallExpression -> "ERROR_CALL '${element.description}' type=${element.type.render()}"
        is BirCall -> "CALL '${element.target.renderReference()}' ${element.renderSuperQualifier()}" +
                "type=${element.type.render()} origin=${element.origin}"
        is BirConstructorCall -> "CONSTRUCTOR_CALL '${element.target.renderReference()}' type=${element.type.render()} origin=${element.origin}"
        is BirDelegatingConstructorCall -> "DELEGATING_CONSTRUCTOR_CALL '${element.target.renderReference()}'"
        is BirEnumConstructorCall -> "ENUM_CONSTRUCTOR_CALL '${element.target.renderReference()}'"
        is BirInstanceInitializerCall -> "INSTANCE_INITIALIZER_CALL classDescriptor='${element.`class`.renderReference()}'"
        is BirGetValue -> "GET_VAR '${element.target.renderReference()}' type=${element.type.render()} origin=${element.origin}"
        is BirSetValue -> "SET_VAR '${element.target.renderReference()}' type=${element.type.render()} origin=${element.origin}"
        is BirGetField -> "GET_FIELD '${element.target.renderReference()}' type=${element.type.render()} origin=${element.origin}"
        is BirSetField -> "SET_FIELD '${element.target.renderReference()}' type=${element.type.render()} origin=${element.origin}"
        is BirGetObjectValue -> "GET_OBJECT '${element.target.renderReference()}' type=${element.type.render()}"
        is BirGetEnumValue -> "GET_ENUM '${element.target.renderReference()}' type=${element.type.render()}"
        is BirStringConcatenation -> "STRING_CONCATENATION type=${element.type.render()}"
        is BirTypeOperatorCall -> "TYPE_OP type=${element.type.render()} origin=${element.operator} typeOperand=${element.typeOperand.render()}"
        is BirWhen -> "WHEN type=${element.type.render()} origin=${element.origin}"
        is BirBranch -> "BRANCH"
        is BirWhileLoop -> "WHILE label=${element.label} origin=${element.origin}"
        is BirDoWhileLoop -> "DO_WHILE label=${element.label} origin=${element.origin}"
        is BirBreak -> "BREAK label=${element.label} loop.label=${element.loop.label}"
        is BirContinue -> "CONTINUE label=${element.label} loop.label=${element.loop.label}"
        is BirThrow -> "THROW type=${element.type.render()}"
        is BirFunctionReference -> "FUNCTION_REFERENCE '${element.target.renderReference()}' " +
                "type=${element.type.render()} origin=${element.origin} " +
                "reflectionTarget=${renderReflectionTarget(element)}"
        is BirRawFunctionReference -> "RAW_FUNCTION_REFERENCE '${element.target.renderReference()}' type=${element.type.render()}"
        is BirPropertyReference -> buildTrimEnd {
            append("PROPERTY_REFERENCE ")
            append("'${element.target.renderReference()}' ")
            appendNullableAttribute("field=", element.field) { "'${it.renderReference()}'" }
            appendNullableAttribute("getter=", element.getter) { "'${it.renderReference()}'" }
            appendNullableAttribute("setter=", element.setter) { "'${it.renderReference()}'" }
            append("type=${element.type.render()} ")
            append("origin=${element.origin}")
        }
        is BirLocalDelegatedPropertyReference -> buildTrimEnd {
            append("LOCAL_DELEGATED_PROPERTY_REFERENCE ")
            append("'${element.target.renderReference()}' ")
            append("delegate='${element.delegate.renderReference()}' ")
            append("getter='${element.getter.renderReference()}' ")
            appendNullableAttribute("setter=", element.setter) { "'${it.renderReference()}'" }
            append("type=${element.type.render()} ")
            append("origin=${element.origin}")
        }
        is BirFunctionExpression -> buildTrimEnd {
            append("FUN_EXPR type=${element.type.render()} origin=${element.origin}")
        }
        is BirClassReference -> "CLASS_REFERENCE '${element.target.renderReference()}' type=${element.type.render()}"
        is BirGetClass -> "GET_CLASS type=${element.type.render()}"
        is BirTry -> "TRY type=${element.type.render()}"
        is BirCatch -> "CATCH parameter=${element.catchParameter.renderReference()}"
        is BirConst<*> -> "CONST ${element.kind} type=${element.type.render()} value=${element.value?.escapeIfRequired()}"
        is BirDynamicOperatorExpression -> "DYN_OP operator=${element.operator} type=${element.type.render()}"
        is BirDynamicMemberExpression -> "DYN_MEMBER memberName='${element.memberName}' type=${element.type.render()}"
        is BirErrorDeclaration -> "ERROR_DECL ${element._descriptor!!::class.java.simpleName} " +
                descriptorRendererForErrorDeclarations.renderDescriptor(element._descriptor!!.original)
        is BirErrorExpression -> "ERROR_EXPR '${element.description}' type=${element.type.render()}"
        is BirConstantArray -> "CONSTANT_ARRAY type=${element.type.render()}"
        is BirConstantObject -> "CONSTANT_OBJECT type=${element.type.render()} constructor=${element.constructor.renderReference()}"
        is BirConstantPrimitive -> "CONSTANT_PRIMITIVE type=${element.type.render()}"
        is BirDeclaration -> "?DECLARATION? ${element::class.java.simpleName} $element"
        is BirExpression -> "? ${element::class.java.simpleName} type=${element.type.render()}"
        else -> "?ELEMENT? ${element::class.java.simpleName} $element"
    }

    private fun BirFunction.renderValueParameterTypes()
            : String =
        ArrayList<String>().apply {
            addIfNotNull(dispatchReceiverParameter?.run { "\$this:${type.render()}" })
            addIfNotNull(extensionReceiverParameter?.run { "\$receiver:${type.render()}" })
            valueParameters.mapTo(this) { "${it.name}:${it.type.render()}" }
        }.joinToString(separator = ", ", prefix = "(", postfix = ")")

    private fun Any.escapeIfRequired() =
        when (this) {
            is String -> "\"${StringUtil.escapeStringCharacters(this)}\""
            is Char -> "'${StringUtil.escapeStringCharacters(this.toString())}'"
            else -> this
        }

    private fun renderReflectionTarget(expression: BirFunctionReference) =
        if (expression.target == expression.reflectionTarget)
            "<same>"
        else
            expression.reflectionTarget?.renderReference()

    private inline fun <T : Any> StringBuilder.appendNullableAttribute(prefix: String, value: T?, toString: (T) -> String) {
        append(prefix)
        if (value != null) {
            append(toString(value))
        } else {
            append("null")
        }
        append(" ")
    }

    private fun BirCall.renderSuperQualifier()
            : String =
        superQualifier?.let { "superQualifier='${it.renderReference()}' " } ?: ""


    private val descriptorRendererForErrorDeclarations = DescriptorRenderer.ONLY_NAMES_WITH_SHORT_TYPES
}

internal fun DescriptorRenderer.renderDescriptor(descriptor: DeclarationDescriptor): String =
    if (descriptor is ReceiverParameterDescriptor)
        "this@${descriptor.containingDeclaration.name}: ${descriptor.type}"
    else
        render(descriptor)

internal fun BirDeclaration.renderOriginIfNonTrivial(): String =
    if (origin != IrDeclarationOrigin.DEFINED) "$origin " else ""

internal fun BirClassifierSymbol.renderClassifierFqn(): String =
    if (this is BirElement)
        when (this) {
            is BirClass -> this.renderClassFqn()
            is BirScript -> this.renderScriptFqn()
            is BirTypeParameter -> this.renderTypeParameterFqn()
            else -> "`unexpected classifier: ${this.render()}`"
        }
    else
        "<unbound ${getCorrespondingIrClassName(this.javaClass)}>"

internal fun BirTypeAliasSymbol.renderTypeAliasFqn(): String =
    if (this is BirDeclaration)
        StringBuilder().also { renderDeclarationFqn(it) }.toString()
    else
        "<unbound $this>"

internal fun BirClass.renderClassFqn(): String =
    StringBuilder().also { renderDeclarationFqn(it) }.toString()

internal fun BirScript.renderScriptFqn(): String =
    StringBuilder().also { renderDeclarationFqn(it) }.toString()

internal fun BirTypeParameter.renderTypeParameterFqn(): String =
    StringBuilder().also { sb ->
        sb.append(name.asString())
        sb.append(" of ")
        renderDeclarationParentFqn(sb)
    }.toString()

private fun BirDeclaration.renderDeclarationFqn(sb: StringBuilder) {
    renderDeclarationParentFqn(sb)
    sb.append('.')
    if (this is BirDeclarationWithName) {
        sb.append(name.asString())
    } else {
        sb.append(this)
    }
}

private fun BirDeclaration.renderDeclarationParentFqn(sb: StringBuilder) {
    try {
        val parent = this.findDeclarationParentOldWay()
        if (parent is BirDeclaration) {
            parent.renderDeclarationFqn(sb)
        } else if (parent is BirPackageFragment) {
            sb.append(parent.fqName.toString())
        }
    } catch (e: UninitializedPropertyAccessException) {
        sb.append("<uninitialized parent>")
    }
}

fun BirType.render(): String = with(DummyBirTreeContext) {
    renderTypeWithRenderer(RenderBirElementVisitor(), true)
}

fun BirSimpleType.render(): String = (this as BirType).render()

fun BirTypeArgument.render(): String =
    when (this) {
        is BirStarProjection -> "*"
        is BirTypeProjection -> "$variance ${type.render()}"
    }

internal inline fun <T, Buffer : Appendable> Buffer.appendIterableWith(
    iterable: Iterable<T>,
    prefix: String,
    postfix: String,
    separator: String,
    renderItem: Buffer.(T) -> Unit
) {
    append(prefix)
    var isFirst = true
    for (item in iterable) {
        if (!isFirst) append(separator)
        renderItem(item)
        isFirst = false
    }
    append(postfix)
}

private inline fun buildTrimEnd(fn: StringBuilder.() -> Unit): String =
    buildString(fn).trimEnd()

private inline fun <T> T.runTrimEnd(fn: T.() -> String): String =
    run(fn).trimEnd()

private fun renderFlagsList(vararg flags: String?) =
    flags.filterNotNull().run {
        if (isNotEmpty())
            joinToString(prefix = "[", postfix = "] ", separator = ",")
        else
            ""
    }

private fun BirClass.renderClassFlags() =
    renderFlagsList(
        "companion".takeIf { isCompanion },
        "inner".takeIf { isInner },
        "data".takeIf { isData },
        "external".takeIf { isExternal },
        "value".takeIf { isValue },
        "expect".takeIf { isExpect },
        "fun".takeIf { isFun }
    )

private fun BirField.renderFieldFlags() =
    renderFlagsList(
        "final".takeIf { isFinal },
        "external".takeIf { isExternal },
        "static".takeIf { isStatic },
    )

private fun BirSimpleFunction.renderSimpleFunctionFlags(): String =
    renderFlagsList(
        "tailrec".takeIf { isTailrec },
        "inline".takeIf { isInline },
        "external".takeIf { isExternal },
        "suspend".takeIf { isSuspend },
        "expect".takeIf { isExpect },
        "fake_override".takeIf { isFakeOverride },
        "operator".takeIf { isOperator },
        "infix".takeIf { isInfix }
    )

private fun BirConstructor.renderConstructorFlags() =
    renderFlagsList(
        "inline".takeIf { isInline },
        "external".takeIf { isExternal },
        "primary".takeIf { isPrimary },
        "expect".takeIf { isExpect }
    )

private fun BirProperty.renderPropertyFlags() =
    renderFlagsList(
        "external".takeIf { isExternal },
        "const".takeIf { isConst },
        "lateinit".takeIf { isLateinit },
        "delegated".takeIf { isDelegated },
        "expect".takeIf { isExpect },
        "fake_override".takeIf { isFakeOverride },
        if (isVar) "var" else "val"
    )

private fun BirVariable.renderVariableFlags(): String =
    renderFlagsList(
        "const".takeIf { isConst },
        "lateinit".takeIf { isLateinit },
        if (isVar) "var" else "val"
    )

private fun BirValueParameter.renderValueParameterFlags(): String =
    renderFlagsList(
        "vararg".takeIf { varargElementType != null },
        "crossinline".takeIf { isCrossinline },
        "noinline".takeIf { isNoinline },
        "assignable".takeIf { isAssignable }
    )

private fun BirTypeAlias.renderTypeAliasFlags(): String =
    renderFlagsList(
        "actual".takeIf { isActual }
    )

private fun BirFunction.renderTypeParameters(): String =
    typeParameters.joinToString(separator = ", ", prefix = "<", postfix = ">") { it.name.toString() }

private val BirFunction.safeReturnType: BirType?
    get() = try {
        returnType
    } catch (e: ReturnTypeIsNotInitializedException) {
        null
    }

private fun BirLocalDelegatedProperty.renderLocalDelegatedPropertyFlags() =
    if (isVar) "var" else "val"

private class VariableNameData(val normalizeNames: Boolean) {
    val nameMap: MutableMap<BirVariableSymbol, String> = mutableMapOf()
    var temporaryIndex: Int = 0
}

private fun BirVariable.normalizedName(data: VariableNameData): String {
    if (data.normalizeNames && (origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE || origin == IrDeclarationOrigin.FOR_LOOP_ITERATOR)) {
        return data.nameMap.getOrPut(this) { "tmp_${data.temporaryIndex++}" }
    }
    return name.asString()
}

private fun BirFunction.renderReturnType(renderer: RenderBirElementVisitor?, verboseErrorTypes: Boolean): String =
    safeReturnType?.renderTypeWithRenderer(renderer, verboseErrorTypes) ?: "<Uninitialized>"

private fun BirType.renderTypeWithRenderer(renderer: RenderBirElementVisitor?, verboseErrorTypes: Boolean): String =
    "${renderTypeAnnotations(annotations, renderer, verboseErrorTypes)}${renderTypeInner(renderer, verboseErrorTypes)}"

private fun BirType.renderTypeInner(renderer: RenderBirElementVisitor?, verboseErrorTypes: Boolean) =
    when (this) {
        is BirDynamicType -> "dynamic"

        is BirErrorType -> "BirErrorType(${verboseErrorTypes.ifTrue { originalKotlinType }})"

        is BirSimpleType -> buildTrimEnd {
            val isDefinitelyNotNullType =
                classifier is BirTypeParameterSymbol && nullability == SimpleTypeNullability.DEFINITELY_NOT_NULL
            if (isDefinitelyNotNullType) append("{")
            append(classifier.renderClassifierFqn())
            if (arguments.isNotEmpty()) {
                append(
                    arguments.joinToString(prefix = "<", postfix = ">", separator = ", ") {
                        it.renderTypeArgument(renderer, verboseErrorTypes)
                    }
                )
            }
            if (isDefinitelyNotNullType) {
                append(" & Any}")
            } else if (isMarkedNullable()) {
                append('?')
            }
            abbreviation?.let {
                append(it.renderTypeAbbreviation(renderer, verboseErrorTypes))
            }
        }

        else -> "{${getCorrespondingIrClassName(javaClass)} $this}"
    }

private fun BirTypeAbbreviation.renderTypeAbbreviation(renderer: RenderBirElementVisitor?, verboseErrorTypes: Boolean): String =
    buildString {
        append("{ ")
        append(renderTypeAnnotations(annotations, renderer, verboseErrorTypes))
        append(typeAlias.renderTypeAliasFqn())
        if (arguments.isNotEmpty()) {
            append(
                arguments.joinToString(prefix = "<", postfix = ">", separator = ", ") {
                    it.renderTypeArgument(renderer, verboseErrorTypes)
                }
            )
        }
        if (hasQuestionMark) {
            append('?')
        }
        append(" }")
    }

private fun BirTypeArgument.renderTypeArgument(renderer: RenderBirElementVisitor?, verboseErrorTypes: Boolean): String =
    when (this) {
        is BirStarProjection -> "*"

        is BirTypeProjection -> buildTrimEnd {
            append(variance.label)
            if (variance != Variance.INVARIANT) append(' ')
            append(type.renderTypeWithRenderer(renderer, verboseErrorTypes))
        }
    }

private fun renderTypeAnnotations(annotations: List<BirConstructorCall>, renderer: RenderBirElementVisitor?, verboseErrorTypes: Boolean) =
    if (annotations.isEmpty())
        ""
    else
        buildString {
            appendIterableWith(annotations, prefix = "", postfix = " ", separator = " ") {
                append("@[")
                renderAsAnnotation(it, renderer, verboseErrorTypes)
                append("]")
            }
        }

private fun StringBuilder.renderAsAnnotation(
    irAnnotation: BirConstructorCall,
    renderer: RenderBirElementVisitor?,
    verboseErrorTypes: Boolean,
) {
    val annotationClassName = irAnnotation.target.maybeAsElement?.parentAsClass?.name?.asString() ?: "<unbound>"
    append(annotationClassName)

    if (irAnnotation.typeArguments.size != 0) {
        irAnnotation.typeArguments.joinTo(this, ", ", "<", ">") { arg ->
            arg?.renderTypeWithRenderer(renderer, verboseErrorTypes) ?: "null"
        }
    }

    if (irAnnotation.valueArguments.size == 0) return

    val valueParameterNames = irAnnotation.getValueParameterNamesForDebug()
    appendIterableWith(irAnnotation.valueArguments.withIndex(), separator = ", ", prefix = "(", postfix = ")") { (idx, arg) ->
        append(valueParameterNames[idx])
        append(" = ")
        renderAsAnnotationArgument(arg, renderer, verboseErrorTypes)
    }
}

private fun StringBuilder.renderAsAnnotationArgument(
    irElement: BirElement?,
    renderer: RenderBirElementVisitor?,
    verboseErrorTypes: Boolean
) {
    when (irElement) {
        null, is BirNoExpression -> append("<null>")
        is BirConstructorCall -> renderAsAnnotation(irElement, renderer, verboseErrorTypes)
        is BirConst<*> -> {
            append('\'')
            append(irElement.value.toString())
            append('\'')
        }
        is BirVararg -> {
            appendIterableWith(irElement.elements, prefix = "[", postfix = "]", separator = ", ") {
                renderAsAnnotationArgument(it, renderer, verboseErrorTypes)
            }
        }
        else -> if (renderer != null) {
            append(renderer.render(irElement))
        } else {
            append("...")
        }
    }
}

private fun renderClassWithRenderer(declaration: BirClass, renderer: RenderBirElementVisitor?, verboseErrorTypes: Boolean) =
    declaration.runTrimEnd {
        "CLASS ${renderOriginIfNonTrivial()}" +
                "$kind name:$name modality:$modality visibility:$visibility " +
                renderClassFlags() +
                "superTypes:[${superTypes.joinToString(separator = "; ") { it.renderTypeWithRenderer(renderer, verboseErrorTypes) }}]"
    }

private fun renderEnumEntry(declaration: BirEnumEntry) = declaration.runTrimEnd {
    "ENUM_ENTRY ${renderOriginIfNonTrivial()}name:$name"
}

private fun renderField(declaration: BirField, renderer: RenderBirElementVisitor?, verboseErrorTypes: Boolean) = declaration.runTrimEnd {
    "FIELD ${renderOriginIfNonTrivial()}name:$name type:${
        type.renderTypeWithRenderer(
            renderer,
            verboseErrorTypes
        )
    } visibility:$visibility ${renderFieldFlags()}"
}

private fun renderTypeParameter(declaration: BirTypeParameter, renderer: RenderBirElementVisitor?, verboseErrorTypes: Boolean): String =
    declaration.runTrimEnd {
        "TYPE_PARAMETER ${renderOriginIfNonTrivial()}" +
                "name:$name index:${getIndex()} variance:$variance " +
                "superTypes:[${
                    superTypes.joinToString(separator = "; ") {
                        it.renderTypeWithRenderer(
                            renderer, verboseErrorTypes
                        )
                    }
                }] " +
                "reified:$isReified"
    }

private fun getCorrespondingIrClassName(klass: Class<Any>) = klass.simpleName.replaceFirst("Bir", "Ir")

private fun BirDeclaration.findDeclarationParentOldWay() =
    ancestors().firstOrNull { it is BirDeclarationContainer || it is BirTypeParametersContainer || it is BirField || it is BirScript }
        ?: parent