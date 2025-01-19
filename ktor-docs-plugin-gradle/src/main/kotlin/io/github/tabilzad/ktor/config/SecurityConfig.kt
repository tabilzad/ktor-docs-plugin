package io.github.tabilzad.ktor.config

import kotlinx.serialization.Serializable


class SecurityConfigBuilder {
    private val scopes = mutableMapOf<String, List<String>>()
    private val schemes = mutableMapOf<String, Scheme>()

    fun scopes(init: ScopeConfigBuilder.() -> Unit) {
        val scopeConfig = ScopeConfigBuilder().apply(init).build()
        scopes.putAll(scopeConfig)
    }

    fun schemes(init: SchemeConfigBuilder.() -> Unit) {
        val schemeConfig = SchemeConfigBuilder().apply(init).build()
        schemes.putAll(schemeConfig)
    }

    fun build(): SecurityConfig = SecurityConfig(scopes, schemes)
}


class InfoConfigBuilder {
    var title: String? = null
    var description: String? = null
    var version: String? = null

    private var contact: Info.Contact? = null
    private var license: Info.License? = null

    fun contact(builder: Info.Contact.() -> Unit) {
        contact = Info.Contact().apply(builder)
    }

    fun license(builder: Info.License.() -> Unit) {
        license = Info.License().apply(builder)
    }

    fun build(): Info = Info(
        title = title,
        description = description,
        version = version,
        contact = contact,
        license = license
    )
}

class ScopeConfigBuilder {
    private val configs = mutableMapOf<String, List<String>>()

    infix fun String.to(scopes: List<String>) {
        configs[this] = scopes
    }

    operator fun String.unaryPlus() {
        configs[this] = emptyList()
    }

    fun build(): Map<String, List<String>> = configs
}

class SchemeConfigBuilder {
    private val configs = mutableMapOf<String, Scheme>()

    infix fun String.to(scheme: Scheme) {
        configs[this] = scheme
    }

    fun build(): Map<String, Scheme> = configs
}

data class SecurityConfig(
    val scopes: Map<String, List<String>>,
    val schemes: Map<String, Scheme>
)

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