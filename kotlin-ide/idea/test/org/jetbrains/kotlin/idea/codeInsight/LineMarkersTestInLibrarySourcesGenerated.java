/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.codeInsight;

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
@TestMetadata("idea/testData/codeInsightInLibrary/lineMarker")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class LineMarkersTestInLibrarySourcesGenerated extends AbstractLineMarkersTestInLibrarySources {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTestWithLibrary, this, testDataFilePath);
    }

    public void testAllFilesPresent() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("idea/testData/codeInsightInLibrary/lineMarker"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @TestMetadata("dummy.kt")
    public void testDummy() throws Exception {
        runTest("idea/testData/codeInsightInLibrary/lineMarker/dummy.kt");
    }
}
