package io.github.tabilzad.ktor

open class DocumentationOptions(
    var docsTitle: String = "Open API Specification",
    var docsDescription: String = "Generated using Ktor Docs Plugin",
    var docsVersion: String = "1.0.0",
    var generateRequestSchemas: Boolean = true,
    var hideTransientFields: Boolean = true,
    var hidePrivateAndInternalFields: Boolean = true,
    var deriveFieldRequirementFromTypeNullability: Boolean = true
)

open class PluginOptions(
    var enabled: Boolean = true,
    var saveInBuild: Boolean = true,
    var filePath: String? = null,
    var format: String = "json"
)

open class KtorDocsExtension(
    var documentation: DocumentationOptions = DocumentationOptions(),
    var pluginOptions: PluginOptions = PluginOptions(),
)
