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
    val queryParameters: List<String>?,
    val method: String,
    val body: OpenApiSpec.ObjectType,
    val summary: String?,
    val description: String?
)

interface KtorElement {
    var path: String?
}

enum class ExpType(val labels: List<String>) {
    ROUTE(listOf("route")),
    METHOD(listOf("get", "post", "put", "patch", "delete")),
    RECEIVE(listOf("receive"))
}

data class EndPoint(
    override var path: String?,
    val method: String = "",
    var body: OpenApiSpec.ObjectType = OpenApiSpec.ObjectType(type = "object"),
    var queryParameters: List<String>? = null,
    var description: String? = null,
    var summary: String? = null
    //var responses: OpenApiSpec.Response = OpenApiSpec.Response(emptyMap())
) : KtorElement

data class DocRoute(
    override var path: String? = "/",
    val children: MutableList<KtorElement> = mutableListOf()
) : KtorElement

enum class ContentType{
    @JsonProperty("application/json")
    APPLICATION_JSON;
}

typealias ContentSchema = Map<String, OpenApiSpec.SchemaRef>

typealias RequestBodyContent = Map<ContentType, ContentSchema>
data class OpenApiComponents(
    val schemas: Map<String, OpenApiSpec.ObjectType>
)
data class OpenApiSpec(
    val openapi: String = "3.1.0",
    val info: Info,
    val paths: Map<String, Map<String, Any>>,
    val components: OpenApiComponents
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
        val summary: String? = "",
        val description: String? = "",
        val responses: Map<String, ResponseDetails> = mapOf("200" to ResponseDetails("TBD")),
        val parameters: List<PathParam>? = null,
        val requestBody: RequestBody? = null
    )

    data class RequestBody(
        val required: Boolean,
        val content: RequestBodyContent
    )

    interface AnyObject{
        var fqName: String?
    }
    data class ObjectType(
        var type: String?,
        var properties: MutableMap<String, ObjectType>? = null,
        var items: ObjectType? = null,
        var enum: List<String>? = null,
        @JsonIgnore
        override var fqName: String? = null,
        var description: String? = null,
        @JsonProperty("\$ref")
        var ref: String? = null,

        @JsonIgnore
        var contentBodyRef: String? = null,
        // var discriminator: InternalDiscriminator? = null,
        var additionalProperties: ObjectType? = null
    ): AnyObject

    data class PathParam(
        override val name: String,
        override val `in`: String,
        override val required: Boolean = true,
        val schema: SchemaType,
    ) : OpenApiSpecParam

    data class SchemaRef(
        val `$ref`: String,
    )

    data class SchemaType(
        val type: String
    )

    data class ResponseDetails(
        val description: String
    )
}