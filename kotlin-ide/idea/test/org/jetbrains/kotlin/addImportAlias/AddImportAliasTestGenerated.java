/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.addImportAlias;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/*
 * This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}.
 * DO NOT MODIFY MANUALLY.
 */
@SuppressWarnings("all")
@TestMetadata("idea/testData/addImportAlias")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class AddImportAliasTestGenerated extends AbstractAddImportAliasTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
    }

    public void testAllFilesPresent() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("idea/testData/addImportAlias"), Pattern.compile("^([^.]+)\\.kt$"), null, true);
    }

    @TestMetadata("JavaAlias.kt")
    public void testJavaAlias() throws Exception {
        runTest("idea/testData/addImportAlias/JavaAlias.kt");
    }

    @TestMetadata("KDocAlias.kt")
    public void testKDocAlias() throws Exception {
        runTest("idea/testData/addImportAlias/KDocAlias.kt");
    }

    @TestMetadata("SimpleAlias.kt")
    public void testSimpleAlias() throws Exception {
        runTest("idea/testData/addImportAlias/SimpleAlias.kt");
    }
}
