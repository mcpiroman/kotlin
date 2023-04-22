/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.kotlin.bir.declarations.*
import org.jetbrains.kotlin.bir.symbols.asElement
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.utils.filterIsInstanceAnd

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

val BirClass.constructors: Sequence<BirConstructor>
    get() = declarations.asSequence().filterIsInstance<BirConstructor>()

context (BirTreeContext)
val BirClass.defaultConstructor: BirConstructor?
    get() = constructors.firstOrNull { ctor -> ctor.valueParameters.all { it.defaultValue != null } }

val BirClass.fields: Sequence<BirField>
    get() = declarations.asSequence().filterIsInstance<BirField>()

val BirClass.primaryConstructor: BirConstructor?
    get() = this.declarations.singleOrNull { it is BirConstructor && it.isPrimary } as BirConstructor?


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
        ?: getSimpleFunction("<get-$name>").also { assert(it?.asElement?.correspondingProperty?.asElement?.name?.asString() == name) }

context(BirTreeContext)
fun BirClass.getPropertySetter(name: String): BirSimpleFunction? =
    getProperty(name)?.setter
        ?: getSimpleFunction("<set-$name>").also { assert(it?.asElement?.correspondingProperty?.asElement?.name?.asString() == name) }


inline fun <reified T : BirDeclaration> BirDeclarationContainer.findDeclaration(predicate: (T) -> Boolean): T? =
    declarations.find { it is T && predicate(it) } as? T


context(BirTreeContext)
val BirClass.defaultType: IrSimpleType
    get() = this.thisReceiver!!.type as IrSimpleType
