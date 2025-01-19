package io.github.tabilzad.ktor.k2

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object ClassIds {

    val KTOR_ROUTING_PACKAGE = FqName("io.ktor.server.routing")
    val KTOR_RECEIVE = FqName("io.ktor.server.request.receive")

    val KTOR_APPLICATION = ClassId(FqName("io.ktor.server.application"), FqName("Application"), false)

    val KTOR_ROUTE = ClassId(KTOR_ROUTING_PACKAGE, FqName("Route"), false)

    val KTOR_ROUTING = ClassId(KTOR_ROUTING_PACKAGE, FqName("Routing"), false)
    val KTOR_RESOURCES = FqName("io.ktor.server.resources")

    val KTOR_QUERY_PARAM = FqName("io.ktor.server.request.ApplicationRequest.queryParameters")
    val KTOR_RAW_QUERY_PARAM = FqName("io.ktor.server.request.ApplicationRequest.rawQueryParameters")

    val KTOR_HEADER_PARAM = FqName("io.ktor.server.request.ApplicationRequest.headers")
    val KTOR_HEADER_ACCESSOR = FqName("io.ktor.server.request.header")

    val KTOR_TAGS_ANNOTATION = ClassId(FqName("io.github.tabilzad.ktor.annotations"), FqName("Tag"), false)
    val KTOR_GENERATE_ANNOTATION = ClassId(FqName("io.github.tabilzad.ktor.annotations"), FqName("GenerateOpenApi"), false)
    val KTOR_DOCS_ANNOTATION = ClassId(FqName("io.github.tabilzad.ktor.annotations"), FqName("KtorDocs"), false)
    val KTOR_RESOURCE_ANNOTATION = ClassId(FqName("io.ktor.resources"), FqName("Resource"), false)

    val KTOR_DSL_ANNOTATION = ClassId(FqName("io.ktor.util"), FqName("KtorDsl"), false)
    val TRANSIENT_ANNOTATION = ClassId(FqName("kotlin.jvm"), FqName("Transient"), false)

    val TRANSIENT_ANNOTATION_FQ = FqName("kotlin.jvm.Transient")

    val KTOR_FIELD_DESCRIPTION = FqName("io.github.tabilzad.ktor.annotations.KtorFieldDescription")
}

enum class SerializationFramework(val fqName: FqName, val identifier: Name) {
    MOSHI(FqName("com.squareup.moshi.Json"), Name.identifier("name")),
    KOTLINX(FqName("kotlinx.serialization.SerialName"), Name.identifier("value"))
}
