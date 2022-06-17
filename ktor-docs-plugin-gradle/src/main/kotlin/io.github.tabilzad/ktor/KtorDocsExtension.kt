package io.github.tabilzad.ktor

open class KtorDocsExtension(
    var title: String = "",
    var description: String = "",
    var version: String = "1.0",
    var requestFeature: Boolean = false,
    var swaggerJsonPath: String? = null
)