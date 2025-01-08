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
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.EXPRESSION)
annotation class KtorDescription(
    val summary: String = "",
    val description: String = "",
    val operationId: String = "",
    val tags: Array<String> = []
)

/**
 * Annotation to describe fields in generated OpenAPI schemas.
 *
 * @property summary A description that will be added to 'description' property of a corresponding schema field in OpenAPI.
 * @property description Optional and the same as summary. Can be used as an alternative description in code.
 * @property required Optional parameter that indicates whether the field is required. Overrides the automatic detection based on field nullability.
 * @property explicitType An optional explicit field type in OpenAPI. Can be used to override the automatic field type definition.
 *      Note: Automatic schema generation will NOT run for this field if the explicitType is not empty.
 * @property format An optional format of the data type (e.g., "date-time", "int32").
 *
 * Example usage:
 * ```
 * data class UserResponse(
 *     @KtorFieldDescription("Example field description")
 *     val id: String,
 *     val name: String,
 *     @KtorFieldDescription(
 *         summary = "Example registration date",
 *         type = "string",
 *         format = "iso8601"
 *     )
 *     val registrationDate: Instant
 * )
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class KtorFieldDescription(
    val summary: String = "",
    val description: String = "",
    val required: Boolean = false,
    val explicitType: String = "",
    val format: String = ""
)
