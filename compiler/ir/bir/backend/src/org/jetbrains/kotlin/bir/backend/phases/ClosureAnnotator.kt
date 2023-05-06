/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.backend.phases

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.expressions.*
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.bir.traversal.traverseStackBased
import org.jetbrains.kotlin.bir.types.BirSimpleType
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.bir.types.BirTypeProjection
import org.jetbrains.kotlin.bir.utils.isLocal
import org.jetbrains.kotlin.bir.utils.render
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.declarations.IrFunction

data class Closure(
    val capturedValues: List<BirValueDeclaration>,
    val capturedTypeParameters: List<BirTypeParameter>,
)

context(org.jetbrains.kotlin.bir.BirTreeContext)
class ClosureAnnotator(
    rootElement: BirElement, declaration: BirDeclaration
) {
    private val closureBuilders = mutableMapOf<BirDeclaration, ClosureBuilder>()

    init {
        rootElement.traverseStackBased(declaration.closureBuilderOrNull ?: declaration.parentClosureBuilder) { element, parentClosure ->
            if (element is BirExpression) {
                val typeParameterContainerScopeBuilder = parentClosure?.let {
                    (it.owner as? BirConstructor)?.closureBuilder ?: it
                }
                typeParameterContainerScopeBuilder?.seeType(element.type)
            }

            when (element) {
                is BirClass -> {
                    element.walkIntoChildren(element.closureBuilder)
                }
                is BirFunction -> {
                    val closureBuilder = element.closureBuilder
                    element.walkIntoChildren(closureBuilder)
                    includeInParent(closureBuilder)
                }
                is BirTypeParameter -> {
                    for (superType in element.superTypes) {
                        parentClosure?.seeType(superType)
                    }
                }
                is BirValueAccessExpression -> {
                    parentClosure?.seeVariable(element.target)
                    element.walkIntoChildren(parentClosure)
                }
                is BirVariable -> {
                    parentClosure?.declareVariable(element)
                    element.walkIntoChildren(parentClosure)
                }
                is BirFunctionAccessExpression -> {
                    element.walkIntoChildren(parentClosure)
                    processMemberAccess(element.target as BirFunction, parentClosure)
                }
                is BirFunctionReference -> {
                    element.walkIntoChildren(parentClosure)
                    processMemberAccess(element.target as BirFunction, parentClosure)
                }
                is BirFunctionExpression -> {
                    element.walkIntoChildren(parentClosure)
                    processMemberAccess(element.function, parentClosure)
                }
                is BirPropertyReference -> {
                    element.walkIntoChildren(parentClosure)
                    element.getter?.let { processMemberAccess(it.asElement, parentClosure) }
                    element.setter?.let { processMemberAccess(it.asElement, parentClosure) }
                }
                else -> element.walkIntoChildren(parentClosure)
            }
        }
    }

    private fun processMemberAccess(declaration: BirDeclaration, parentClosure: ClosureBuilder?) {
        if (declaration.isLocal) {
            if (declaration is BirSimpleFunction && declaration.visibility != DescriptorVisibilities.LOCAL) {
                return
            }

            val builder = declaration.closureBuilderOrNull
            builder?.let {
                parentClosure?.include(builder)
            }
        }
    }

    fun getFunctionClosure(declaration: BirFunction) = getClosure(declaration)
    fun getClassClosure(declaration: BirClass) = getClosure(declaration)

    private fun getClosure(declaration: BirDeclaration): Closure {
        return closureBuilders
            .getOrElse(declaration) { throw AssertionError("No closure builder for passed declaration ${declaration.render()}.") }
            .buildClosure()
    }

    private fun includeInParent(builder: ClosureBuilder) {
        // We don't include functions or classes in a parent function when they are declared.
        // Instead, we will include them when are is used (use = call for a function or constructor call for a class).
        val parentBuilder = builder.owner.parentClosureBuilder
        if (parentBuilder != null && parentBuilder.owner !is IrFunction) {
            parentBuilder.include(builder)
        }
    }

    private val BirClass.closureBuilder: ClosureBuilder
        get() = closureBuilders.getOrPut(this) {
            val closureBuilder = ClosureBuilder(this)

            collectPotentiallyCapturedTypeParameters(closureBuilder)

            closureBuilder.declareVariable(this.thisReceiver)
            if (this.isInner) {
                val receiver = when (val parent = this.parent) {
                    is BirClass -> parent.thisReceiver
                    is BirScript -> parent.thisReceiver
                    else -> error("unexpected parent $parent")
                }
                closureBuilder.declareVariable(receiver)
                includeInParent(closureBuilder)
            }

            this.declarations.firstOrNull { it is BirConstructor && it.isPrimary }?.let {
                val constructor = it as BirConstructor
                constructor.valueParameters.forEach { v -> closureBuilder.declareVariable(v) }
            }

            closureBuilder
        }

    private val BirFunction.closureBuilder: ClosureBuilder
        get() = closureBuilders.getOrPut(this) {
            val closureBuilder = ClosureBuilder(this)

            collectPotentiallyCapturedTypeParameters(closureBuilder)

            this.valueParameters.forEach { closureBuilder.declareVariable(it) }
            closureBuilder.declareVariable(this.dispatchReceiverParameter)
            closureBuilder.declareVariable(this.extensionReceiverParameter)
            closureBuilder.seeType(this.returnType)

            if (this is BirConstructor) {
                val constructedClass = (this.parent as BirClass)
                closureBuilder.declareVariable(constructedClass.thisReceiver)

                // Include closure of the class in the constructor closure.
                val classBuilder = constructedClass.closureBuilder
                closureBuilder.include(classBuilder)
            }

            closureBuilder
        }

    private fun collectPotentiallyCapturedTypeParameters(closureBuilder: ClosureBuilder) {
        var current = closureBuilder.owner.parentClosureBuilder
        while (current != null) {
            val container = current.owner

            if (container is BirTypeParametersContainer) {
                for (typeParameter in container.typeParameters) {
                    closureBuilder.addPotentiallyCapturedTypeParameter(typeParameter)
                }
            }

            current = container.parentClosureBuilder
        }
    }

    private val BirDeclaration.parentClosureBuilder: ClosureBuilder?
        get() = when (val p = parent) {
            is BirClass -> p.closureBuilder
            is BirFunction -> p.closureBuilder
            is BirDeclaration -> p.parentClosureBuilder
            else -> null
        }

    private val BirDeclaration.closureBuilderOrNull: ClosureBuilder?
        get() = when (this) {
            is BirClass -> closureBuilder
            is BirFunction -> closureBuilder
            else -> null
        }
}

private class ClosureBuilder(val owner: BirDeclaration) {
    private val capturedValues = mutableSetOf<BirValueDeclaration>()
    private val declaredValues = mutableSetOf<BirValueDeclaration>()
    private val includes = mutableSetOf<ClosureBuilder>()

    private val potentiallyCapturedTypeParameters = mutableSetOf<BirTypeParameter>()
    private val capturedTypeParameters = mutableSetOf<BirTypeParameter>()

    private var closure: Closure? = null

    /*
     * This will solve a system of equations for each dependent closure:
     *
     *  closure[V] = captured(V) + { closure[U] | U <- included(V) } - declared(V)
     *
     */
    fun buildClosure(): Closure {
        closure?.let { return it }

        val work = collectConnectedClosures()

        do {
            var changes = false
            for (c in work) {
                if (c.updateFromIncluded()) {
                    changes = true
                }
            }
        } while (changes)

        for (c in work) {
            c.closure = Closure(c.capturedValues.toList(), c.capturedTypeParameters.toList())
        }

        return closure
            ?: throw AssertionError("Closure should have been built for ${owner.render()}")
    }

    private fun collectConnectedClosures(): List<ClosureBuilder> {
        val connected = LinkedHashSet<ClosureBuilder>()
        fun collectRec(current: ClosureBuilder) {
            for (included in current.includes) {
                if (included.closure == null && connected.add(included)) {
                    collectRec(included)
                }
            }
        }
        connected.add(this)
        collectRec(this)
        return connected.toList().asReversed()
    }

    private fun updateFromIncluded(): Boolean {
        if (closure != null)
            throw AssertionError("Closure has already been built for ${owner.render()}")

        val capturedValuesBefore = capturedValues.size
        val capturedTypeParametersBefore = capturedTypeParameters.size
        for (subClosure in includes) {
            subClosure.capturedValues.filterTo(capturedValues) { isExternal(it) }
            subClosure.capturedTypeParameters.filterTo(capturedTypeParameters) { isExternal(it) }
        }

        return capturedValues.size != capturedValuesBefore ||
                capturedTypeParameters.size != capturedTypeParametersBefore
    }


    fun include(includingBuilder: ClosureBuilder) {
        includes.add(includingBuilder)
    }

    fun declareVariable(valueDeclaration: BirValueDeclaration?) {
        if (valueDeclaration != null) {
            declaredValues.add(valueDeclaration)
            seeType(valueDeclaration.type)
        }
    }

    fun seeVariable(value: BirValueDeclaration) {
        if (isExternal(value)) {
            capturedValues.add(value)
        }
    }

    fun isExternal(valueDeclaration: BirValueDeclaration): Boolean {
        return !declaredValues.contains(valueDeclaration)
    }

    fun isExternal(typeParameter: BirTypeParameter): Boolean {
        return potentiallyCapturedTypeParameters.contains(typeParameter)
    }

    fun addPotentiallyCapturedTypeParameter(param: BirTypeParameter) {
        potentiallyCapturedTypeParameters.add(param)
    }

    fun seeType(type: BirType) {
        if (type !is BirSimpleType) return
        val classifier = type.classifier
        if (classifier is BirTypeParameter && isExternal(classifier) && capturedTypeParameters.add(classifier))
            classifier.superTypes.forEach(::seeType)
        type.arguments.forEach {
            (it as? BirTypeProjection)?.type?.let(::seeType)
        }
        type.abbreviation?.arguments?.forEach {
            (it as? BirTypeProjection)?.type?.let(::seeType)
        }
    }
}