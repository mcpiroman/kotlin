/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fileClasses

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.StubElement
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.psi.stubs.KotlinAnnotationEntryStub
import org.jetbrains.kotlin.psi.stubs.elements.KtFileStubBuilder
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.KotlinTestWithEnvironment
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.jetbrains.kotlin.utils.addToStdlib.cast

class JvmFileClassUtilTest : KotlinTestWithEnvironment() {
    fun testCorruptedJvmName() {
        val ktFile = KtTestUtil.loadPsiFile(project, "diagnostics/testsWithStdLib/regression/ea70880_illegalJvmName.kt")
        assertNull("file is parsed from AST", ktFile.stub)

        val annotationEntryStubs = mutableListOf<KotlinAnnotationEntryStub>()
        KtFileStubBuilder().buildStubTree(ktFile).cast<PsiFileStub<*>>().forEachDescendantOfType {
            if (it is KotlinAnnotationEntryStub) {
                annotationEntryStubs.add(it)
            }
        }

        assertEquals(3, annotationEntryStubs.size)
        assertTrue(annotationEntryStubs.all { it.psi.stub != null })
        assertEquals(1, annotationEntryStubs.mapNotNull { JvmFileClassUtil.getLiteralStringFromAnnotation(it.psi) }.size)
    }

    private fun StubElement<*>.forEachDescendantOfType(action: (StubElement<*>) -> Unit) {
        action(this)
        childrenStubs.forEach {
            it.forEachDescendantOfType(action)
        }
    }

    override fun createEnvironment(): KotlinCoreEnvironment {
        return KotlinCoreEnvironment.createForTests(
                testRootDisposable, KotlinTestUtils.newConfiguration(), EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
    }
}