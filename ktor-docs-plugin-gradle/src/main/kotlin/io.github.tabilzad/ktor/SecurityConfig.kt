package io.github.tabilzad.ktor

import kotlinx.serialization.Serializable


class SecurityConfigBuilder {
    private val configs = mutableMapOf<String, List<String>>()

    infix fun String.to(scopes: List<String>) {
        configs[this] = scopes
    }

    operator fun String.unaryPlus() {
        configs[this] = emptyList()
    }

    fun build(): Map<String, List<String>> = configs
}

@Serializable
open class Scheme(
    val type: String, // "apiKey", "http", "oauth2", etc.
    val scheme: String? = null, // "basic", "bearer", etc. (for "http")
    val `in`: String? = null, // can be "header", "query" or "cookie"
    val name: String? = null, // name of the header, query parameter or cookie
    val bearerFormat: String? = null, // optional, arbitrary value for documentation purposes, eg. JWT
    val description: String? = null,
    val flows: Map<String, OAuthFlowConfig>? = null, // Used for OAuth flow specs
    val openIdConnectUrl: String? = null,
)

@Serializable
open class OAuthFlowConfig(
    val authorizationUrl: String,
    val tokenUrl: String? = null,
    val refreshUrl: String? = null,
    val scopes: Map<String, String>? = null,
)