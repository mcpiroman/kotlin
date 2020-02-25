/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package com.jetbrains.mobile.execution

import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectModelBuildableElement
import com.intellij.task.ProjectTaskContext
import com.jetbrains.cidr.execution.build.CidrBuildTargetAction
import com.jetbrains.mobile.MobileBundle

class MobileBuildAction : CidrBuildTargetAction(true, MobileBundle.message("build"), null, AllIcons.Actions.Compile) {
    private fun selectedRunConfiguration(project: Project) =
        RunManager.getInstance(project).selectedConfiguration?.configuration as? MobileRunConfiguration

    override fun isEnabled(project: Project): Boolean =
        selectedRunConfiguration(project) != null

    override fun createContext(dataContext: DataContext): ProjectTaskContext {
        val project = dataContext.getData(CommonDataKeys.PROJECT)!!
        return MobileProjectTaskRunner.Context(
            dataContext,
            selectedRunConfiguration(project)!!,
            ExecutionTargetManager.getActiveTarget(project) as Device
        )
    }

    override fun getBuildableElements(project: Project): List<ProjectModelBuildableElement> {
        if (selectedRunConfiguration(project) == null) return emptyList()
        return listOf(MobileProjectTaskRunner.BuildableElement())
    }

    override fun buildText(configuration: RunConfiguration?): String =
        if (configuration != null)
            MobileBundle.message("build.something", configuration.name)
        else
            MobileBundle.message("build")
}