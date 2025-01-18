package io.github.tabilzad.ktor

open class DocumentationOptions(
    var docsTitle: String = "Open API Specification",
    var docsDescription: String = "Generated using Ktor Docs Plugin",
    var docsVersion: String = "1.0.0",
    var generateRequestSchemas: Boolean = true,
    var hideTransientFields: Boolean = true,
    var hidePrivateAndInternalFields: Boolean = true,
    var deriveFieldRequirementFromTypeNullability: Boolean = true,
    var useKDocsForDescriptions: Boolean = true,
    var servers: List<String> = emptyList(),
    var securityConfig: MutableList<Map<String, List<String>>> = mutableListOf(),
    var securitySchemes: Map<String, Scheme>? = emptyMap(),
) {
    fun securityConfig(block: SecurityConfigBuilder.() -> Unit) {
        val builder = SecurityConfigBuilder()
        builder.block()
        builder.build().forEach { authSchemeName, authScopes ->
            securityConfig.add(mapOf(authSchemeName to authScopes))
        }
    }
}

open class PluginOptions(
    var enabled: Boolean = true,
    var saveInBuild: Boolean = true,
    var filePath: String? = null,
    var format: String = "json"
)

open class KtorInspectorGradleConfig(
    var documentation: DocumentationOptions = DocumentationOptions(),
    var pluginOptions: PluginOptions = PluginOptions(),
)
