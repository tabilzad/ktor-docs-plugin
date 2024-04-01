package io.github.tabilzad.model

import io.github.tabilzad.ktor.KtorDescription

data class Sample(
    @KtorDescription("Description for field 1")
    val a: Map<String, String>,
    @KtorDescription("Description for field 2")
    val b: NestedSample,
    @KtorDescription("Description for field 3")
    val c: List<NestedSample>,
)


data class ErrorResponseSample(
    @KtorDescription("Description for error code")
    val erroCode: Int,
    @KtorDescription("Description for error message")
    val message: String
)
data class NestedSample(
    @KtorDescription("Description for field 4")
    val d: List<List<String>>
)
