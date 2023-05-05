/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.bir.utils

import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.symbols.BirSymbol
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.traversal.BirTreeStackBasedTraverseScopeWithData
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.ir.util.NaiveSourceBasedFileEntryImpl
import org.jetbrains.kotlin.utils.Printer

fun BirElement.dump(normalizeNames: Boolean = false, stableOrder: Boolean = false): String =
    //try {
    with(DummyBirTreeContext) {
        StringBuilder().also { sb ->
            DumpBirTreeVisitor(sb, normalizeNames, stableOrder).run(this@dump)
        }.toString()
    }
/*} catch (e: Exception) {
    "(Full dump is not available: ${e.message})\n" + render()
}*/

private fun BirFile.shouldSkipDump(): Boolean {
    val entry = fileEntry as? NaiveSourceBasedFileEntryImpl ?: return false
    return entry.lineStartOffsetsAreEmpty
}

context(BirTreeContext)
class DumpBirTreeVisitor(
    out: Appendable,
    normalizeNames: Boolean = false,
    private val stableOrder: Boolean = false
) {
    private val printer = Printer(out, "  ")
    private val elementRenderer = RenderBirElementVisitor(normalizeNames, !stableOrder)
    private fun BirType.render() = elementRenderer.renderType(this)

    private fun Collection<BirDeclaration>.ordered(): List<BirDeclaration> {
        if (!stableOrder) return this.toList()

        val strictOrder = mutableMapOf<BirDeclaration, Int>()

        var idx = 0

        forEach {
            if (it is BirProperty && it.backingField != null && !it.isConst) {
                strictOrder[it] = idx++
            }
            if (it is BirAnonymousInitializer) {
                strictOrder[it] = idx++
            }
        }

        return sortedWith { a, b ->
            val strictA = strictOrder[a] ?: Int.MAX_VALUE
            val strictB = strictOrder[b] ?: Int.MAX_VALUE

            if (strictA == strictB) {
                val rA = a.render()
                val rB = b.render()
                rA.compareTo(rB)
            } else strictA - strictB
        }
    }

    fun run(root: BirElement) {
        root.traverseStackBased("") { element, data ->
            element.accept(data)
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun BirElement.accept(data: String): Unit = when (this) {
        is BirClass -> visitClass(this, data)
        is BirTypeParameter -> visitTypeParameter(this, data)
        is BirConstructor -> visitConstructor(this, data)
        is BirEnumEntry -> visitEnumEntry(this, data)
        is BirField -> visitField(this, data)
        is BirModuleFragment -> visitModuleFragment(this, data)
        is BirProperty -> visitProperty(this, data)
        is BirSimpleFunction -> visitSimpleFunction(this, data)
        is BirTypeAlias -> visitTypeAlias(this, data)
        is BirExternalPackageFragment -> visitExternalPackageFragment(this, data)
        is BirFile -> visitFile(this, data)
        is BirConstructorCall -> visitConstructorCall(this, data)
        is BirBlock -> visitBlock(this, data)
        is BirConstantObject -> visitConstantObject(this, data)
        is BirConstantArray -> visitConstantArray(this, data)
        is BirDynamicOperatorExpression -> visitDynamicOperatorExpression(this, data)
        is BirErrorCallExpression -> visitErrorCallExpression(this, data)
        is BirGetField -> visitGetField(this, data)
        is BirSetField -> visitSetField(this, data)
        is BirWhileLoop -> visitWhileLoop(this, data)
        is BirDoWhileLoop -> visitDoWhileLoop(this, data)
        is BirTry -> visitTry(this, data)
        is BirWhen -> visitWhen(this, data)
        is BirBranch -> visitBranch(this, data)
        is BirTypeOperatorCall -> visitTypeOperator(this, data)
        is BirMemberAccessExpression<*> -> visitMemberAccess(this, data)
        is BirNoExpression -> {
            printer.print("<null>")
            Unit
        }
        else -> visitElement(this, data)
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitElement(element: BirElement, data: String) {
        element.dumpLabeledElementWith(data) {
            if (element is BirAnnotationContainer) {
                dumpAnnotations(element)
            }
            element.recurse("")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitModuleFragment(declaration: BirModuleFragment, data: String) {
        declaration.dumpLabeledElementWith(data) {
            declaration.files.dumpElements()
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitExternalPackageFragment(declaration: BirExternalPackageFragment, data: String) {
        declaration.dumpLabeledElementWith(data) {
            declaration.declarations.ordered().dumpElements()
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitFile(declaration: BirFile, data: String) {
        declaration.dumpLabeledElementWith(data) {
            dumpAnnotations(declaration)
            declaration.declarations.ordered().dumpElements()
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitClass(declaration: BirClass, data: String) {
        declaration.dumpLabeledElementWith(data) {
            dumpAnnotations(declaration)
            declaration[GlobalBirElementAuxStorageTokens.SealedSubclasses].orEmpty().dumpItems("sealedSubclasses") { it.dump() }
            declaration.thisReceiver?.accept("\$this")
            declaration.typeParameters.dumpElements()
            declaration.declarations.ordered().dumpElements()
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitTypeAlias(declaration: BirTypeAlias, data: String) {
        declaration.dumpLabeledElementWith(data) {
            dumpAnnotations(declaration)
            declaration.typeParameters.dumpElements()
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitTypeParameter(declaration: BirTypeParameter, data: String) {
        declaration.dumpLabeledElementWith(data) {
            dumpAnnotations(declaration)
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitSimpleFunction(declaration: BirSimpleFunction, data: String) {
        declaration.dumpLabeledElementWith(data) {
            dumpAnnotations(declaration)
            declaration.correspondingProperty?.dumpInternal("correspondingProperty")
            declaration.overriddenSymbols.dumpItems("overridden") { it.dump() }
            declaration.typeParameters.dumpElements()
            declaration.dispatchReceiverParameter?.accept("\$this")

            val contextReceiverParametersCount = declaration.contextReceiverParametersCount
            if (contextReceiverParametersCount > 0) {
                printer.println("contextReceiverParametersCount: $contextReceiverParametersCount")
            }

            declaration.extensionReceiverParameter?.accept("\$receiver")
            declaration.valueParameters.dumpElements()
            declaration.body?.accept("")
        }
    }

    private fun dumpAnnotations(element: BirAnnotationContainer) {
        element.annotations.dumpItems("annotations") { irAnnotation: BirConstructorCall ->
            printer.println(elementRenderer.renderAsAnnotation(irAnnotation))
        }
    }

    private fun BirSymbol.dump(label: String? = null) =
        printer.println(
            elementRenderer.renderSymbolReference(this).let {
                if (label != null) "$label: $it" else it
            }
        )

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitConstructor(declaration: BirConstructor, data: String) {
        declaration.dumpLabeledElementWith(data) {
            dumpAnnotations(declaration)
            declaration.typeParameters.dumpElements()
            declaration.dispatchReceiverParameter?.accept("\$outer")
            declaration.valueParameters.dumpElements()
            declaration.body?.accept("")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitProperty(declaration: BirProperty, data: String) {
        declaration.dumpLabeledElementWith(data) {
            dumpAnnotations(declaration)
            declaration.overriddenSymbols.dumpItems("overridden") { it.dump() }
            declaration.backingField?.accept("")
            declaration.getter?.accept("")
            declaration.setter?.accept("")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitField(declaration: BirField, data: String) {
        declaration.dumpLabeledElementWith(data) {
            dumpAnnotations(declaration)
            declaration.initializer?.accept("")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun Collection<BirElement>.dumpElements() {
        forEach { it.accept("") }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitErrorCallExpression(expression: BirErrorCallExpression, data: String) {
        expression.dumpLabeledElementWith(data) {
            expression.explicitReceiver?.accept("receiver")
            expression.arguments.dumpElements()
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitEnumEntry(declaration: BirEnumEntry, data: String) {
        declaration.dumpLabeledElementWith(data) {
            dumpAnnotations(declaration)
            declaration.initializerExpression?.accept("init")
            declaration.correspondingClass?.accept("class")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitMemberAccess(expression: BirMemberAccessExpression<*>, data: String) {
        expression.dumpLabeledElementWith(data) {
            dumpTypeArguments(expression)
            expression.dispatchReceiver?.accept("\$this")
            expression.extensionReceiver?.accept("\$receiver")
            val valueParameterNames = expression.getValueParameterNamesForDebug()
            expression.valueArguments.forEachIndexed { index, arg ->
                if (arg !is BirNoExpression) {
                    arg.accept(valueParameterNames[index])
                }
            }
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitConstructorCall(expression: BirConstructorCall, data: String) {
        expression.dumpLabeledElementWith(data) {
            dumpTypeArguments(expression)
            expression.dispatchReceiver?.accept("\$outer")
            dumpConstructorValueArguments(expression)
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun dumpConstructorValueArguments(expression: BirConstructorCall) {
        val valueParameterNames = expression.getValueParameterNamesForDebug()
        expression.valueArguments.forEachIndexed { index, arg ->
            if (arg !is BirNoExpression) {
                arg.accept(valueParameterNames[index])
            }
        }
    }

    private fun dumpTypeArguments(expression: BirMemberAccessExpression<*>) {
        val typeParameterNames = expression.getTypeParameterNames(expression.typeArguments.size)
        expression.typeArguments.forEachIndexed { index, arg ->
            printer.println("<${typeParameterNames[index]}>: ${expression.renderTypeArgument(index)}")
        }
    }

    private fun dumpTypeArguments(expression: BirConstructorCall) {
        val typeParameterNames = expression.getTypeParameterNames(expression.typeArguments.size)
        for (index in 0 until expression.typeArguments.size) {
            val typeParameterName = typeParameterNames[index]
            val parameterLabel =
                if (index < expression.classTypeArgumentsCount)
                    "class: $typeParameterName"
                else
                    typeParameterName
            printer.println("<$parameterLabel>: ${expression.renderTypeArgument(index)}")
        }
    }

    private fun BirMemberAccessExpression<*>.getTypeParameterNames(expectedCount: Int): List<String> =
        (target as? BirElement)?.getTypeParameterNames(expectedCount)
            ?: getPlaceholderParameterNames(expectedCount)

    private fun BirElement.getTypeParameterNames(expectedCount: Int): List<String> =
        if (this is BirTypeParametersContainer) {
            val typeParameters = if (this is BirConstructor) getFullTypeParametersList() else this.typeParameters
            (0 until expectedCount).map {
                if (it < typeParameters.size)
                    typeParameters.elementAt(it).name.asString()
                else
                    "${it + 1}"
            }
        } else {
            getPlaceholderParameterNames(expectedCount)
        }

    private fun BirConstructor.getFullTypeParametersList(): List<BirTypeParameter> {
        // xxx
        val parentClass = try {
            parent as? BirClass ?: return typeParameters.toList()
        } catch (e: Exception) {
            return typeParameters.toList()
        }
        return parentClass.typeParameters + typeParameters
    }

    private fun BirMemberAccessExpression<*>.renderTypeArgument(index: Int): String =
        typeArguments[index]?.render() ?: "<none>"

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitBlock(expression: BirBlock, data: String) {
        if (expression is BirInlinedFunctionBlock) {
            expression.dumpLabeledElementWith(data) {
                expression.inlinedElement.dumpInternal("inlinedElement")
                visitElement(expression, data)
            }
        } else {
            visitElement(expression, data)
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitGetField(expression: BirGetField, data: String) {
        expression.dumpLabeledElementWith(data) {
            expression.receiver?.accept("receiver")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitSetField(expression: BirSetField, data: String) {
        expression.dumpLabeledElementWith(data) {
            expression.receiver?.accept("receiver")
            expression.value.accept("value")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitWhen(expression: BirWhen, data: String) {
        expression.dumpLabeledElementWith(data) {
            expression.branches.dumpElements()
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitBranch(branch: BirBranch, data: String) {
        branch.dumpLabeledElementWith(data) {
            branch.condition.accept("if")
            branch.result.accept("then")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitWhileLoop(loop: BirWhileLoop, data: String) {
        loop.dumpLabeledElementWith(data) {
            loop.condition.accept("condition")
            loop.body?.accept("body")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitDoWhileLoop(loop: BirDoWhileLoop, data: String) {
        loop.dumpLabeledElementWith(data) {
            loop.body?.accept("body")
            loop.condition.accept("condition")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitTry(aTry: BirTry, data: String) {
        aTry.dumpLabeledElementWith(data) {
            aTry.tryResult.accept("try")
            aTry.catches.dumpElements()
            aTry.finallyExpression?.accept("finally")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitTypeOperator(expression: BirTypeOperatorCall, data: String) {
        expression.dumpLabeledElementWith(data) {
            expression.recurse("")
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitDynamicOperatorExpression(expression: BirDynamicOperatorExpression, data: String) {
        expression.dumpLabeledElementWith(data) {
            expression.receiver.accept("receiver")
            for ((i, arg) in expression.arguments.withIndex()) {
                arg.accept(i.toString())
            }
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitConstantArray(expression: BirConstantArray, data: String) {
        expression.dumpLabeledElementWith(data) {
            for ((i, value) in expression.elements.withIndex()) {
                value.accept(i.toString())
            }
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private fun visitConstantObject(expression: BirConstantObject, data: String) {
        expression.dumpLabeledElementWith(data) {
            for ((argument, param) in expression.valueArguments zip expression.constructor.asElement.valueParameters) {
                argument.accept(param.name.toString())
            }
        }
    }

    context(BirTreeStackBasedTraverseScopeWithData<String>)
    private inline fun BirElement.dumpLabeledElementWith(label: String, body: () -> Unit) {
        printer.println(elementRenderer.render(this).withLabel(label))
        indented(body)
    }

    private inline fun <T> Collection<T>.dumpItems(caption: String, renderElement: (T) -> Unit) {
        if (isEmpty()) return
        indented(caption) {
            forEach {
                renderElement(it)
            }
        }
    }

    private fun BirSymbol.dumpInternal(label: String? = null) {
        if (this is BirElement)
            (this as BirElement).dumpInternal(label)
        else
            printer.println("$label: UNBOUND ${javaClass.simpleName}")
    }

    private fun BirElement.dumpInternal(label: String? = null) {
        if (label != null) {
            printer.println("$label: ", elementRenderer.render(this))
        } else {
            printer.println(elementRenderer.render(this))
        }

    }

    private inline fun indented(label: String, body: () -> Unit) {
        printer.println("$label:")
        indented(body)
    }

    private inline fun indented(body: () -> Unit) {
        printer.pushIndent()
        body()
        printer.popIndent()
    }

    private fun String.withLabel(label: String) =
        if (label.isEmpty()) this else "$label: $this"
}

internal fun BirMemberAccessExpression<*>.getValueParameterNamesForDebug(): List<String> {
    val expectedCount = valueArguments.size
    val target = target
    if (target is BirElement) {
        if (target is BirFunction) {
            val valueParameters = target.valueParameters.toList()
            return List(expectedCount) {
                if (it < valueParameters.size)
                    valueParameters[it].name.asString()
                else
                    "${it + 1}"
            }
        }
    }
    return getPlaceholderParameterNames(expectedCount)
}

internal fun getPlaceholderParameterNames(expectedCount: Int) =
    (1..expectedCount).map { "$it" }
