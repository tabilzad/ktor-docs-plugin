package io.github.tabilzad.ktor

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.EXPRESSION)
annotation class KtorDocs()

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.EXPRESSION)
annotation class KtorDescription(val summary: String = "", val description: String = "")


@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class KtorDiscriminator