package io.github.tabilzad.ktor.input

import io.github.tabilzad.ktor.output.OpenApiSpec
import kotlinx.serialization.Serializable

@Serializable
data class ConfigInput(
    val securityConfig: List<Map<String, List<String>>> = emptyList(),
    val securitySchemes: Map<String, OpenApiSpec.SecurityScheme> = emptyMap(),
    val info: OpenApiSpec.Info? = null
)
