/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.fir.low.level.api

import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.LightProjectDescriptor
import org.jetbrains.kotlin.fir.FirRenderer
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.idea.caches.project.ModuleSourceInfo
import org.jetbrains.kotlin.idea.caches.project.getModuleInfo
import org.jetbrains.kotlin.idea.fir.low.level.api.api.withFirDeclaration
import org.jetbrains.kotlin.idea.fir.low.level.api.lazy.resolve.FirLazyDeclarationResolver
import org.jetbrains.kotlin.idea.fir.low.level.api.lazy.resolve.ResolveType
import org.jetbrains.kotlin.idea.fir.low.level.api.lazy.resolve.declarationCanBeLazilyResolved
import org.jetbrains.kotlin.idea.test.KotlinLightCodeInsightFixtureTestCase
import org.jetbrains.kotlin.idea.test.KotlinWithJdkAndRuntimeLightProjectDescriptor
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.test.KotlinTestUtils
import java.io.File

/**
 * Test that we do not resolve declarations we do not need & do not build bodies for them
 */
abstract class AbstractFirLazyDeclarationResolveTest : KotlinLightCodeInsightFixtureTestCase() {
    override fun isFirPlugin(): Boolean = true

    fun doTest(path: String) {
        val testDataFile = File(path)
        val ktFile = myFixture.configureByText(testDataFile.name, FileUtil.loadFile(testDataFile)) as KtFile
        val lazyDeclarations = ktFile.collectDescendantsOfType<KtDeclaration> { ktDeclaration ->
            declarationCanBeLazilyResolved(ktDeclaration)
        }

        val resultBuilder = StringBuilder()
        val declarationToResolve = lazyDeclarations.firstOrNull { it.name?.lowercase() == "resolveme" }
            ?: error("declaration with name `resolveMe` was not found")

        resolveWithClearCaches(ktFile) { firModuleResolveState ->
            check(firModuleResolveState is FirModuleResolveStateImpl)
            for (currentPhase in FirResolvePhase.values()) {
                if (currentPhase.pluginPhase || currentPhase == FirResolvePhase.SEALED_CLASS_INHERITORS) continue
                declarationToResolve.withFirDeclaration(firModuleResolveState, currentPhase) {
                    val firFile = firModuleResolveState.getOrBuildFirFile(ktFile)
                    resultBuilder.append("\n${currentPhase.name}:\n")
                    resultBuilder.append(firFile.render(FirRenderer.RenderMode.WithDeclarationAttributes))
                }
            }
        }

        for (resolveType in ResolveType.values()) {
            when (resolveType) {
                ResolveType.CallableReturnType,
                ResolveType.CallableBodyResolve,
                ResolveType.CallableContracts -> if (declarationToResolve !is KtCallableDeclaration) continue
                ResolveType.ClassSuperTypes -> if (declarationToResolve !is KtClassOrObject) continue
                else -> {
                }
            }

            resolveWithClearCaches(ktFile) { firModuleResolveState ->
                check(firModuleResolveState is FirModuleResolveStateImpl)
                declarationToResolve.withFirDeclaration(firModuleResolveState, resolveType) {
                    val firFile = firModuleResolveState.getOrBuildFirFile(ktFile)
                    resultBuilder.append("\n${resolveType.name}:\n")
                    resultBuilder.append(firFile.render(FirRenderer.RenderMode.WithDeclarationAttributes))
                }
            }
        }

        resolveWithClearCaches(ktFile) { firModuleResolveState ->
            check(firModuleResolveState is FirModuleResolveStateImpl)
            val firFile = firModuleResolveState.getOrBuildFirFile(ktFile)
            firFile.withFirDeclaration(firModuleResolveState, FirResolvePhase.BODY_RESOLVE) {
                resultBuilder.append("\nFILE RAW TO BODY:\n")
                resultBuilder.append(firFile.render(FirRenderer.RenderMode.WithDeclarationAttributes))
            }
        }

        val expectedFileName = testDataFile.name.replace(".kt", ".txt")
        KotlinTestUtils.assertEqualsToFile(testDataFile.parentFile.resolve(expectedFileName), resultBuilder.toString())
    }

    override fun getProjectDescriptor(): LightProjectDescriptor = KotlinWithJdkAndRuntimeLightProjectDescriptor.INSTANCE
}