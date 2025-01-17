package io.github.tabilzad.ktor

import io.github.tabilzad.ktor.config.Info
import io.github.tabilzad.ktor.config.InfoConfigBuilder
import io.github.tabilzad.ktor.config.Scheme
import io.github.tabilzad.ktor.config.SecurityConfigBuilder

open class DocumentationOptions(
    var generateRequestSchemas: Boolean = true,
    var hideTransientFields: Boolean = true,
    var hidePrivateAndInternalFields: Boolean = true,
    var deriveFieldRequirementFromTypeNullability: Boolean = true,
    var useKDocsForDescriptions: Boolean = true,
    var servers: List<String> = emptyList(),
) {
    private val securityConfig: MutableList<Map<String, List<String>>> = mutableListOf()
    private val securitySchemes: MutableMap<String, Scheme> = mutableMapOf()
    private var info = Info()

    fun security(block: SecurityConfigBuilder.() -> Unit) {
        val builder = SecurityConfigBuilder()
        builder.block()
        builder.build().let {
            it.scopes.forEach { scope ->
                securityConfig.add(mapOf(scope.toPair()))
            }

            it.schemes.forEach { (schemeKey, scheme) ->
                securitySchemes[schemeKey] = scheme
            }
        }
    }

    fun info(block: InfoConfigBuilder.() -> Unit) {
        val builder = InfoConfigBuilder()
        builder.block()
        info = builder.build()
    }

    internal fun getInfo() = info

    internal fun getSecurityConfig(): List<Map<String, List<String>>> = securityConfig.toList()

    internal fun getSecuritySchemes(): Map<String, Scheme> = securitySchemes.toMap()
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
