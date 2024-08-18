package io.github.tabilzad.ktor.annotations

import kotlin.reflect.KClass

@Deprecated("Please use @GenerateOpenApi")
typealias KtorDocs = GenerateOpenApi

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.EXPRESSION)
annotation class GenerateOpenApi

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.EXPRESSION)
annotation class IgnoreOpenApi

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.EXPRESSION)
annotation class Tag(val tags: Array<String> = [])

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.EXPRESSION)
annotation class KtorResponds(
    val mapping: Array<ResponseEntry>
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.EXPRESSION)
annotation class ResponseEntry(
    val status: String,
    val type: KClass<*>,
    val isCollection: Boolean = false,
    val description: String = ""
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION,
    AnnotationTarget.EXPRESSION)
annotation class KtorDescription(
    val summary: String = "",
    val description: String = "",
    val required: Boolean = false,
    val tags: Array<String> = [])

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class KtorFieldDescription(
    val summary: String = "",
    val description: String = "",
    val required: Boolean = false)
