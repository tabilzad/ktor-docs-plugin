package io.github.tabilzad.ktor.k2

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object ClassIds {

    val KTOR_APPLICATION = ClassId(FqName("io.ktor.server.application"), FqName("Application"), false)
    val KTOR_ROUTING = ClassId(FqName("io.ktor.server.routing"), FqName("Routing"), false)
    val KTOR_ROUTE = ClassId(FqName("io.ktor.server.routing"), FqName("Route"), false)
    val KTOR_RECEIVE = FqName("io.ktor.server.request.receive")
    val KTOR_QUERY_PARAM = FqName("io.ktor.server.request.ApplicationRequest.queryParameters")
    val KTOR_RAW_QUERY_PARAM = FqName("io.ktor.server.request.ApplicationRequest.rawQueryParameters")

    val KTOR_TAGS_ANNOTATION = ClassId(FqName("io.github.tabilzad.ktor"), FqName("Tag"), false)
    val KTOR_GENERATE_ANNOTATION = ClassId(FqName("io.github.tabilzad.ktor"), FqName("GenerateOpenApi"), false)
    val KTOR_DOCS_ANNOTATION = ClassId(FqName("io.github.tabilzad.ktor"), FqName("KtorDocs"), false)

    val KTOR_DSL_ANNOTATION = ClassId(FqName("io.ktor.util"), FqName("KtorDsl"), false)
    val TRANSIENT_ANNOTATION = ClassId(FqName("kotlin.jvm"), FqName("Transient"), false)

    val MOSHI_JSON_ANNOTATION_FQ_NAME = FqName("com.squareup.moshi.Json")
    val MOSHI_JSON_ANNOTATION_NAME_ARGUMENT_IDENTIFIER: Name = Name.identifier("name")
}