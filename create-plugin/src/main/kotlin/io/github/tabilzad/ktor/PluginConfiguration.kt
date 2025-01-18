package io.github.tabilzad.ktor

import io.github.tabilzad.ktor.KtorDocsCommandLineProcessor.Companion.security
import io.github.tabilzad.ktor.output.OpenApiSpec

// Internal
internal data class PluginConfiguration(
    val isEnabled: Boolean,
    val format: String,
    val title: String,
    val description: String,
    val version: String,
    val filePath: String,
    val requestBody: Boolean,
    val hideTransients: Boolean,
    val hidePrivateFields: Boolean,
    val servers: List<String>,
    val securityConfig: List<Map<String, List<String>>>,
    val securitySchemes: Map<String, OpenApiSpec.SecurityScheme>,
    val deriveFieldRequirementFromTypeNullability: Boolean,
    val useKDocsForDescriptions: Boolean
) {
    companion object {
        fun createDefault(
            isEnabled: Boolean? = null,
            format: String? = null,
            title: String? = null,
            description: String? = null,
            version: String? = null,
            filePath: String? = null,
            requestBody: Boolean? = null,
            hideTransients: Boolean? = null,
            hidePrivateFields: Boolean? = null,
            servers: List<String>? = null,
            securityConfig: List<Map<String, List<String>>>? = null,
            securitySchemes: Map<String, OpenApiSpec.SecurityScheme>? = null,
            deriveFieldRequirementFromTypeNullability: Boolean? = null,
            useKDocsForDescriptions: Boolean? = null
        ): PluginConfiguration = PluginConfiguration(
            isEnabled = isEnabled ?: true,
            format = format ?: "yaml",
            title = title ?: "Open API Specification",
            description = description ?: "",
            version = version ?: "1.0.0",
            filePath = filePath ?: "openapi.yaml",
            requestBody = requestBody ?: true,
            hideTransients = hideTransients ?: true,
            hidePrivateFields = hidePrivateFields ?: true,
            deriveFieldRequirementFromTypeNullability = deriveFieldRequirementFromTypeNullability ?: true,
            servers = servers ?: emptyList(),
            securityConfig = securityConfig ?: emptyList(),
            securitySchemes = securitySchemes ?: emptyMap(),
            useKDocsForDescriptions = useKDocsForDescriptions ?: true
        )
    }
}
