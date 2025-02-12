/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.resolve.extensions

import com.intellij.openapi.util.ModificationTracker
import org.jetbrains.kotlin.name.FqName

/**
 * Provides a list of Kotlin files which provides additional, generated declarations for resolution.
 *
 * Provided by the [KtResolveExtensionProvider].
 *
 * All member implementations should:
 * - consider caching the results for subsequent invocations.
 * - be lightweight and not build the whole file structure inside.
 * - not use the Kotlin resolve inside, as this function is called during session initialization, so Analysis API access is forbidden.
 *
 * @see KtResolveExtensionFile
 * @see KtResolveExtensionProvider
 */
public abstract class KtResolveExtension {
    /**
     * Get the list of files that should be generated for the module.
     *
     * The content of those files should remain valid until the tracker [getModificationTracker] is modified.
     *
     * Returned files should contain a valid Kotlin code.
     *
     * @see KtResolveExtensionFile
     * @see KtResolveExtension
     */
    public abstract fun getKtFiles(): List<KtResolveExtensionFile>

    /**
     * Returns a [ModificationTracker], which controls the validity lifecycle of the files provided by [getKtFiles].
     *
     * All files generated by [getKtFiles] should be valid if the [ModificationTracker] did not change.
     * If files become invalid (e.g., the in-source declarations they were based on changed) the [ModificationTracker] should be incremented.
     *
     * @see KtResolveExtension
     */
    public abstract fun getModificationTracker(): ModificationTracker

    /**
     * Returns the set of packages that are contained in the files provided by [getKtFiles].
     *
     * The returned package set should be a strict set of all file packages,
     * so `for-all pckg: pckg in getContainedPackages() <=> exists file: file in getKtFiles() && file.getFilePackageName() == pckg`
     *
     * @see KtResolveExtension
     */
    public abstract fun getContainedPackages(): Set<FqName>
}