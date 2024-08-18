package io.github.tabilzad.ktor.k2

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.expressions.FirAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitor

internal class ResourceAnnotationVisitor : FirDefaultVisitor<String?, String?>() {

    override fun visitElement(element: FirElement, data: String?): String? = data

    override fun visitAnnotationCall(annotationCall: FirAnnotationCall, data: String?): String? =
        annotationCall.argumentMapping.accept(this, data)

    override fun visitAnnotationArgumentMapping(
        annotationArgumentMapping: FirAnnotationArgumentMapping,
        data: String?
    ): String? {
        return annotationArgumentMapping.mapping.entries.firstOrNull()?.let { (_, value) ->
            value.accept(StringResolutionVisitor(), "")
        } ?: data
    }
}
