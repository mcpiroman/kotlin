/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.type

import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirOptInUsageBaseChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.analysis.diagnostics.reportOn
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.coneTypeSafe
import org.jetbrains.kotlin.resolve.checkers.OptInNames

object FirOptInUsageTypeRefChecker : FirTypeRefChecker() {
    override fun check(typeRef: FirTypeRef, context: CheckerContext, reporter: DiagnosticReporter) {
        val coneType = typeRef.coneTypeSafe<ConeClassLikeType>() ?: return
        val symbol = coneType.lookupTag.toSymbol(context.session) ?: return
        val fqName = symbol.classId.asSingleFqName()
        val lastAnnotationCall = context.qualifiedAccessOrAnnotationCalls.lastOrNull() as? FirAnnotationCall
        if ((lastAnnotationCall == null || lastAnnotationCall.annotationTypeRef !== typeRef) &&
            (fqName in OptInNames.EXPERIMENTAL_FQ_NAMES || fqName in OptInNames.USE_EXPERIMENTAL_FQ_NAMES)
        ) {
            reporter.reportOn(typeRef.source, FirErrors.EXPERIMENTAL_CAN_ONLY_BE_USED_AS_ANNOTATION, context)
        }

        with(FirOptInUsageBaseChecker) {
            val experimentalities = symbol.fir.loadExperimentalities(context) +
                    loadExperimentalitiesFromConeArguments(context, coneType.typeArguments.toList())
            reportNotAcceptedExperimentalities(experimentalities, typeRef, context, reporter)
        }
    }
}