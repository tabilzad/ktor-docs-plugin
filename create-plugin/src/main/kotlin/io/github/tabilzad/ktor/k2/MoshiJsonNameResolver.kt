package io.github.tabilzad.ktor.k2

import io.github.tabilzad.ktor.k2.ClassIds.MOSHI_JSON_ANNOTATION_FQ_NAME
import io.github.tabilzad.ktor.k2.ClassIds.MOSHI_JSON_ANNOTATION_NAME_ARGUMENT_IDENTIFIER
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.declarations.primaryConstructorIfAny
import org.jetbrains.kotlin.fir.declarations.utils.isData
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.getContainingClass

object MoshiJsonNameResolver  {

    fun getMoshiJsonName(
        property: FirProperty,
        session: FirSession,
    ): String? {
        return getMoshiNameFromPropertyAnnotation(property, session)
            ?: getMoshiNameFromDataClassConstructorParamAnnotation(property, session)
    }

    private fun getMoshiNameFromPropertyAnnotation(
        property: FirProperty,
        session: FirSession,
    ): String? {
        return property.annotations.getMoshiJsonName(session)
    }

    private fun List<FirAnnotation>?.getMoshiJsonName(session: FirSession): String? {
        return this
            ?.find { annotation -> annotation.fqName(session) == MOSHI_JSON_ANNOTATION_FQ_NAME }
            ?.getStringArgument(MOSHI_JSON_ANNOTATION_NAME_ARGUMENT_IDENTIFIER, session)
    }

    private fun getMoshiNameFromDataClassConstructorParamAnnotation(
        property: FirProperty,
        session: FirSession,
    ): String? {
        return property.getContainingClass(session)
            ?.takeIf { it.isData }
            ?.primaryConstructorIfAny(session)
            ?.valueParameterSymbols
            ?.find { it.name == property.name}
            ?.annotations
            ?.getMoshiJsonName(session)
    }
}
