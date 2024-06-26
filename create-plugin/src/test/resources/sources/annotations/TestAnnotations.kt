package sources.annotations

import kotlin.reflect.KClass


@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.EXPRESSION)
annotation class KtorDocs(val tags: Array<String> = [])

@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.EXPRESSION,
    AnnotationTarget.FIELD,
    AnnotationTarget.CLASS
)
annotation class KtorDescription(val summary: String = "", val description: String = "", val tags: Array<String> = [])

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class KtorDiscriminator(val name: String = "")

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