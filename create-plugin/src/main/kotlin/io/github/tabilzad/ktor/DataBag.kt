package io.github.tabilzad.ktor

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
    var body: OpenApiSpec.ObjectType = OpenApiSpec.ObjectType("object")
) : KtorElement

data class DocRoute(
    override var path: String? = "/",
    val children: MutableList<KtorElement> = mutableListOf()
) : KtorElement

data class OpenApiSpec(
    val swagger: String = "2.0",
    val info: Info,
    val servers: List<Server> = emptyList(),
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
        val responses: List<Response> = emptyList(),
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
        var name: String? = null
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

    data class Response(
        val status: Map<String, ResponseDetails>
    )

    data class ResponseDetails(
        val description: String,
        val content: Map<String, String>
    )
}