/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fileClasses

import com.intellij.openapi.application.ApplicationManager
import org.jetbrains.kotlin.idea.test.AstAccessControl
import org.jetbrains.kotlin.idea.test.KotlinLightCodeInsightFixtureTestCaseBase
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class JvmFileClassUtilTest : KotlinLightCodeInsightFixtureTestCaseBase() {
    fun testStubAccessWithACorruptedJvmName() {
        doTestJvmNameStubAccess(
            """
            <!ILLEGAL_JVM_NAME!>@JvmName(<!NO_VALUE_FOR_PARAMETER!>)<!><!>
            fun foo() {}
        """.trimIndent(), null
        )
    }

    fun testStubAccessWithAValidJvmName() {
        doTestJvmNameStubAccess(
            """
            @JvmName("bar")
            fun foo() {}
        """.trimIndent(), "bar"
        )
    }

    private fun doTestJvmNameStubAccess(content: String, expected: String?) {
        val ktFile = myFixture.configureByText("jvmName.kt", content) as KtFile
        assertNull("file is parsed from AST", ktFile.stub)

        AstAccessControl.testWithControlledAccessToAst(false, project, testRootDisposable) {
            // clean up AST
            ApplicationManager.getApplication().runWriteAction { ktFile.onContentReload() }
            assertNotNull("file is parsed from AST", ktFile.stub)
            val annotationEntries =
                run {
                    ktFile.stub!!.findChildStubByType(KtStubElementTypes.FUNCTION)
                        .takeIf { it?.findChildStubByType(KtStubElementTypes.MODIFIER_LIST) != null } ?: ktFile.stub!!
                }
                    .findChildStubByType(KtStubElementTypes.MODIFIER_LIST)
                    ?.getChildrenByType(KtStubElementTypes.ANNOTATION_ENTRY, emptyArray<KtAnnotationEntry>())
                    ?: emptyArray()
            assertTrue(annotationEntries.all { it.stub != null })
            assertEquals(1, annotationEntries.size)

            with(JvmFileClassUtil.getLiteralStringFromAnnotation(annotationEntries.first())) {
                expected?.run { assertEquals(expected, this) } ?: run { assertNull(this) }
            }
        }
    }

}