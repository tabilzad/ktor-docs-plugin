package io.github.tabilzad.ktor.model

import kotlinx.serialization.Serializable

@Serializable
data class ConfigInput(
    val securityConfig: List<Map<String, List<String>>> = emptyList(),
    val securitySchemes: Map<String, SecurityScheme> = emptyMap(),
    val info: Info? = null
)

@Serializable
data class Info(
    val title: String? = null,
    val description: String? = null,
    val version: String? = null,
    val contact: Contact? = null,
    val license: License? = null,
) {

    @Serializable
    data class Contact(
        var name: String? = null,
        var email: String? = null,
        var url: String? = null
    )

    @Serializable
    data class License(
        var name: String? = null,
        var url: String? = null
    )
}

@Serializable
open class SecurityScheme(
    val type: String, // "apiKey", "http", "oauth2", etc.
    val scheme: String? = null, // "basic", "bearer", etc. (for "http")
    val `in`: String? = null, // can be "header", "query" or "cookie"
    val name: String? = null, // name of the header, query parameter or cookie
    val bearerFormat: String? = null, // optional, arbitrary value for documentation purposes, eg. JWT
    val description: String? = null,
    val flows: Map<String, OAuthFlow>? = null, // Used for OAuth flow specs
    val openIdConnectUrl: String? = null,
)

@Serializable
data class OAuthFlow(
    val authorizationUrl: String,
    val tokenUrl: String? = null,
    val refreshUrl: String? = null,
    val scopes: Map<String, String>? = null,
)
