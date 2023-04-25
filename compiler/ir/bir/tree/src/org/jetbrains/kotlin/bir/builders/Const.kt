/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir.builders

import org.jetbrains.kotlin.bir.SourceSpan
import org.jetbrains.kotlin.bir.expressions.BirConst
import org.jetbrains.kotlin.bir.expressions.impl.BirConstImpl
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.bir.types.utils.getPrimitiveType
import org.jetbrains.kotlin.bir.types.utils.isMarkedNullable
import org.jetbrains.kotlin.bir.types.utils.makeNullable
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.ir.expressions.IrConstKind

// todo: default `type` parameter to the birBuiltIns.booleanType/birBuiltIns.intType etc.

fun BirConst.Companion.constNull(sourceSpan: SourceSpan, type: BirType): BirConstImpl<Nothing?> =
    BirConstImpl(sourceSpan, type, IrConstKind.Null, null)

fun BirConst.Companion.boolean(sourceSpan: SourceSpan, type: BirType, value: Boolean): BirConstImpl<Boolean> =
    BirConstImpl(sourceSpan, type, IrConstKind.Boolean, value)

fun BirConst.Companion.constTrue(sourceSpan: SourceSpan, type: BirType): BirConstImpl<Boolean> =
    boolean(sourceSpan, type, true)

fun BirConst.Companion.constFalse(sourceSpan: SourceSpan, type: BirType): BirConstImpl<Boolean> =
    boolean(sourceSpan, type, false)

fun BirConst.Companion.byte(sourceSpan: SourceSpan, type: BirType, value: Byte): BirConstImpl<Byte> =
    BirConstImpl(sourceSpan, type, IrConstKind.Byte, value)

fun BirConst.Companion.short(sourceSpan: SourceSpan, type: BirType, value: Short): BirConstImpl<Short> =
    BirConstImpl(sourceSpan, type, IrConstKind.Short, value)

fun BirConst.Companion.int(sourceSpan: SourceSpan, type: BirType, value: Int): BirConstImpl<Int> =
    BirConstImpl(sourceSpan, type, IrConstKind.Int, value)

fun BirConst.Companion.long(sourceSpan: SourceSpan, type: BirType, value: Long): BirConstImpl<Long> =
    BirConstImpl(sourceSpan, type, IrConstKind.Long, value)

fun BirConst.Companion.float(sourceSpan: SourceSpan, type: BirType, value: Float): BirConstImpl<Float> =
    BirConstImpl(sourceSpan, type, IrConstKind.Float, value)

fun BirConst.Companion.double(sourceSpan: SourceSpan, type: BirType, value: Double): BirConstImpl<Double> =
    BirConstImpl(sourceSpan, type, IrConstKind.Double, value)

fun BirConst.Companion.char(sourceSpan: SourceSpan, type: BirType, value: Char): BirConstImpl<Char> =
    BirConstImpl(sourceSpan, type, IrConstKind.Char, value)

fun BirConst.Companion.string(sourceSpan: SourceSpan, type: BirType, value: String): BirConstImpl<String> =
    BirConstImpl(sourceSpan, type, IrConstKind.String, value)

fun BirConst.Companion.defaultValueForType(sourceSpan: SourceSpan, type: BirType): BirConstImpl<*> {
    if (type.isMarkedNullable()) return BirConst.constNull(sourceSpan, type)
    return when (type.getPrimitiveType()) {
        PrimitiveType.BOOLEAN -> BirConst.boolean(sourceSpan, type, false)
        PrimitiveType.CHAR -> BirConst.char(sourceSpan, type, 0.toChar())
        PrimitiveType.BYTE -> BirConst.byte(sourceSpan, type, 0)
        PrimitiveType.SHORT -> BirConst.short(sourceSpan, type, 0)
        PrimitiveType.INT -> BirConst.int(sourceSpan, type, 0)
        PrimitiveType.FLOAT -> BirConst.float(sourceSpan, type, 0.0F)
        PrimitiveType.LONG -> BirConst.long(sourceSpan, type, 0)
        PrimitiveType.DOUBLE -> BirConst.double(sourceSpan, type, 0.0)
        else -> BirConst.constNull(sourceSpan, type.makeNullable())
    }
}