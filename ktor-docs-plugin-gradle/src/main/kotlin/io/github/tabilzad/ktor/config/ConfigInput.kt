package io.github.tabilzad.ktor.config

import kotlinx.serialization.Serializable

@Serializable
data class ConfigInput(
    val securityConfig: List<Map<String, List<String>>>,
    val securitySchemes: Map<String, Scheme>,
    val info: Info
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