package io.github.tabilzad.ktor

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

interface OpenApiSpecParam {
    val name: String
    val `in`: String
    val required: Boolean
}

data class KtorRouteSpec(
    val path: String,
    val method: String,
    val body: OpenApiSpec.ObjectType
)

interface KtorElement {
    var path: String?
}

enum class ExpType(val labels: List<String>) {
    ROUTE(listOf("route")),
    METHOD(listOf("get", "post", "put", "patch")),
    RECEIVE(listOf("receive"))
}

data class EndPoint(
    override var path: String?,
    val method: String = "",
    var body: OpenApiSpec.ObjectType = OpenApiSpec.ObjectType("object"),
    //var responses: OpenApiSpec.Response = OpenApiSpec.Response(emptyMap())
) : KtorElement

data class DocRoute(
    override var path: String? = "/",
    val children: MutableList<KtorElement> = mutableListOf()
) : KtorElement

data class OpenApiSpec(
    val swagger: String = "2.0",
    val info: Info,
    val paths: Map<String, Map<String, Any>>,
    val definitions: Map<String, ObjectType>
) {
    data class Info(
        val title: String,
        val description: String,
        val version: String
    )

    data class Server(
        val url: String,
        val description: String
    )

    data class Path(
        val description: String = "",
        val responses: Map<String, ResponseDetails> = mapOf("200" to ResponseDetails("TBD")),
        val consumes: List<String> = listOf("application/json"),
        val produces: List<String> = listOf("application/json")
    )

    data class RequestBody(
        val required: List<String>,
        val properties: Map<String, Schema>
    )

    data class ObjectType(
        var type: String,
        var properties: MutableMap<String, ObjectType>? = null,
        var items: ObjectType? = null,
        var enum: List<String>? = null,
        @JsonIgnore
        var name: String? = null,
        @JsonProperty("\$ref")
        var ref: String? = null,
        var additionalProperties: ObjectType? = null
    )


    data class PathParam(
        override val name: String,
        override val `in`: String,
        override val required: Boolean = true,
        val type: String
    ) : OpenApiSpecParam

    data class BodyParam(
        override val name: String,
        override val `in`: String = "body",
        override val required: Boolean = true,
        val schema: Schema
    ) : OpenApiSpecParam

    data class Schema(
        val type: String,
        val `$ref`: String?,
    )

    data class ResponseDetails(
        val description: String
    )
}