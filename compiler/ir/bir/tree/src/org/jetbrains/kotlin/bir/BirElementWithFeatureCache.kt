/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.bir

import org.jetbrains.org.objectweb.asm.ClassWriter
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class BirElementsWithFeatureCacheKey<E : BirElement>(
    val includeOtherModules: Boolean, // todo: use it
    val condition: BirElementFeatureCacheCondition,
) {
    internal var index = -1
}

fun interface BirElementFeatureCacheCondition {
    fun matches(element: BirElementBase): Boolean
}

internal fun interface BirElementFeatureSlotSelectionFunction {
    fun select(element: BirElementBase, minimumIndex: Int): Int
}

internal object BirElementFeatureSlotSelectionFunctionManager {
    private val nextSelectingFunctionClassIdx = AtomicInteger(0)
    private val selectingFunctionClassCache = ConcurrentHashMap<List<Class<BirElementFeatureCacheCondition>>, Class<*>>()

    fun createSelectingFunction(matchers: List<BirElementFeatureCacheCondition>): BirElementFeatureSlotSelectionFunction {
        val clazz = getOrCreateSelectingFunctionClass(matchers)
        val array = matchers.toTypedArray()
        val instance = clazz.declaredConstructors.single().newInstance(array)
        return instance as BirElementFeatureSlotSelectionFunction
    }

    private fun getOrCreateSelectingFunctionClass(conditions: List<BirElementFeatureCacheCondition>): Class<*> {
        val key = conditions.map { it.javaClass }
        return selectingFunctionClassCache.computeIfAbsent(key) { _ ->
            val clazzNode = generateSelectingFunctionClass(conditions)
            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            clazzNode.accept(cw)
            val binary = cw.toByteArray()
            val binaryName = clazzNode.name.replace('/', '.')
            ByteArrayFunctionClassLoader.defineClass(binaryName, binary)
        }
    }

    private fun generateSelectingFunctionClass(conditions: List<BirElementFeatureCacheCondition>): ClassNode {
        val clazz = ClassNode().apply {
            version = Opcodes.V1_8
            access = Opcodes.ACC_PUBLIC
            val id = nextSelectingFunctionClassIdx.getAndIncrement()
            name = "org/jetbrains/kotlin/bir/BirElementFeatureSlotSelectionFunctionManager\$BirElementFeatureSlotSelectionFunction$id"
            superName = "java/lang/Object"
            interfaces.add(Type.getInternalName(BirElementFeatureSlotSelectionFunction::class.java))
        }

        val capturedMatcherInstancesCache = generateSelectMethod(clazz, conditions)
        generateConstructor(clazz, capturedMatcherInstancesCache)

        return clazz
    }

    private fun generateSelectMethod(
        clazz: ClassNode,
        conditions: List<BirElementFeatureCacheCondition>,
    ): MutableList<Pair<FieldNode, Int>> {
        val selectMethod = MethodNode().apply {
            access = Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL
            name = "select"
            desc = Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(BirElementBase::class.java), Type.INT_TYPE)
        }

        val il = selectMethod.instructions
        val capturedMatcherInstances = mutableListOf<Pair<FieldNode, Int>>()
        conditions.forEachIndexed { conditionIndex, conditionFunction ->
            val i = conditionIndex + 1

            val conditionFunctionClass = conditionFunction.javaClass
            /*if (conditionFunctionClass.declaredFields.isEmpty()) {
                il.add(
                    FieldInsnNode(
                        Opcodes.GETSTATIC,
                        Type.getInternalName(conditionFunctionClass),
                        "INSTANCE",
                        Type.getDescriptor(conditionFunctionClass)
                    )
                )
            } else*/
            run {
                val fieldIdx = capturedMatcherInstances.size
                val field = FieldNode(
                    Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL,
                    "matcher$fieldIdx",
                    Type.getDescriptor(BirElementFeatureCacheCondition::class.java), //Type.getDescriptor(conditionFunctionClass),
                    null,
                    null,
                )

                clazz.fields.add(field)
                capturedMatcherInstances += field to conditionIndex

                il.add(VarInsnNode(Opcodes.ALOAD, 0))
                il.add(FieldInsnNode(Opcodes.GETFIELD, clazz.name, field.name, field.desc))
            }

            il.add(VarInsnNode(Opcodes.ALOAD, 1))
            il.add(
                MethodInsnNode(
                    Opcodes.INVOKEINTERFACE,
                    Type.getInternalName(BirElementFeatureCacheCondition::class.java), //Type.getInternalName(conditionFunctionClass),
                    "matches",
                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(BirElementBase::class.java))
                )
            )

            val label = LabelNode()
            il.add(JumpInsnNode(Opcodes.IFEQ, label))

            il.add(IntInsnNode(Opcodes.SIPUSH, i))
            il.add(VarInsnNode(Opcodes.ILOAD, 2))
            il.add(JumpInsnNode(Opcodes.IF_ICMPLT, label))

            il.add(IntInsnNode(Opcodes.SIPUSH, i))
            il.add(InsnNode(Opcodes.IRETURN))
            il.add(label)
        }

        il.add(InsnNode(Opcodes.ICONST_0))
        il.add(InsnNode(Opcodes.IRETURN))

        clazz.methods.add(selectMethod)

        return capturedMatcherInstances
    }

    private fun generateConstructor(
        clazz: ClassNode,
        capturedMatcherInstances: List<Pair<FieldNode, Int>>,
    ) {
        val ctor = MethodNode().apply {
            access = Opcodes.ACC_PUBLIC
            name = "<init>"
            desc = "([${Type.getDescriptor(BirElementFeatureCacheCondition::class.java)})V"
        }

        val il = ctor.instructions
        il.add(VarInsnNode(Opcodes.ALOAD, 0))
        il.add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false))
        capturedMatcherInstances.forEach { (field, index) ->
            il.add(VarInsnNode(Opcodes.ALOAD, 0))
            il.add(VarInsnNode(Opcodes.ALOAD, 1))
            il.add(IntInsnNode(Opcodes.SIPUSH, index))
            il.add(InsnNode(Opcodes.AALOAD))
            il.add(FieldInsnNode(Opcodes.PUTFIELD, clazz.name, field.name, field.desc))
        }
        il.add(InsnNode(Opcodes.RETURN))

        clazz.methods.add(ctor)
    }

    private object ByteArrayFunctionClassLoader : ClassLoader() {
        fun defineClass(name: String, binary: ByteArray): Class<*> {
            return defineClass(name, binary, 0, binary.size)
        }
    }
}
