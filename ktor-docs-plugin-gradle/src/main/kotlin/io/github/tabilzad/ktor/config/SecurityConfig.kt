package io.github.tabilzad.ktor.config

import io.github.tabilzad.ktor.model.SecurityScheme

data class SecurityConfig(
    val scopes: List<Map<String, List<String>>>,
    val schemes: Map<String, SecurityScheme>,
)

class SecurityBuilder {
    private val requirements = mutableListOf<Map<String, List<String>>>()
    private val schemes = mutableMapOf<String, SecurityScheme>()

    fun scopes(init: ScopeConfigBuilder.() -> Unit) {
        val scopeConfig = ScopeConfigBuilder().apply(init).build()
        requirements.addAll(scopeConfig)
    }

    fun schemes(init: SchemeConfigBuilder.() -> Unit) {
        val schemeConfig = SchemeConfigBuilder().apply(init).build()
        schemes.putAll(schemeConfig)
    }

    fun build(): SecurityConfig = SecurityConfig(requirements, schemes.toMap())
}

class ScopeConfigBuilder {
    private val items = mutableListOf<Map<String, List<String>>>()

    fun or(init: ItemBuilder.() -> Unit) {
        val builder = ItemBuilder()
        builder.init()
        items.add(builder.build())
    }

    fun and(init: ItemBuilder.() -> Unit) {
        val builder = ItemBuilder()
        builder.init()
        items.add(builder.build())
    }

    fun build(): List<Map<String, List<String>>> = items
}

class ItemBuilder {
    private val map = linkedMapOf<String, List<String>>()

    operator fun String.unaryPlus() {
        map[this] = emptyList()
    }

    infix fun String.to(scopes: List<String>) {
        map[this] = scopes
    }

    fun build(): Map<String, List<String>> = map
}

class SchemeConfigBuilder {
    private val configs = mutableMapOf<String, SecurityScheme>()

    infix fun String.to(scheme: SecurityScheme) {
        configs[this] = scheme
    }

    fun build(): Map<String, SecurityScheme> = configs
}
