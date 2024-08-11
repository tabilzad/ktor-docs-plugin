package io.github.tabilzad.ktor

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

// Internal
data class PluginConfiguration(
    val isEnabled: Boolean,
    val buildPath: String,
    val saveInBuild: Boolean,
    val modulePath: String,
    val format: String,
    val title: String,
    val description: String,
    val version: String,
    val filePath: String,
    val requestBody: Boolean,
    val hideTransients: Boolean,
    val hidePrivateFields: Boolean,
    val deriveFieldRequirementFromTypeNullability: Boolean
)
interface OpenApiSpecParam {
    val name: String
    val `in`: String
    val required: Boolean
}

internal data class KtorRouteSpec(
    val path: String,
    val queryParameters: List<String>?,
    val method: String,
    val body: OpenApiSpec.ObjectType,
    val summary: String?,
    val description: String?,
    val tags: Set<String>?,
    val responses: Map<String, OpenApiSpec.ResponseDetails>?
)

sealed class KtorElement {
    abstract var path: String?
    abstract var tags: Set<String>?

    abstract fun newInstance(tags: Set<String>?): KtorElement
}

enum class ExpType(val labels: List<String>) {
    ROUTE(listOf("route")),
    METHOD(listOf("get", "post", "put", "patch", "delete")),
    RECEIVE(listOf("receive"))
}

internal data class EndPoint(
    override var path: String?,
    val method: String = "",
    var body: OpenApiSpec.ObjectType = OpenApiSpec.ObjectType(type = "object"),
    var queryParameters: Set<String>? = null,
    var description: String? = null,
    var summary: String? = null,
    override var tags: Set<String>? = null,
    var responses: Map<String, OpenApiSpec.ResponseDetails>? = null
) : KtorElement() {
    override fun newInstance(tags: Set<String>?): EndPoint {
        return copy(tags = tags)
    }
}

data class DocRoute(
    override var path: String? = "/",
    val children: MutableList<KtorElement> = mutableListOf(),
    override var tags: Set<String>? = null
) : KtorElement() {
    override fun newInstance(tags: Set<String>?): DocRoute {
        return copy(tags = tags)
    }
}

enum class ContentType {
    @JsonProperty("application/json")
    APPLICATION_JSON;
}

internal typealias ContentSchema = Map<String, OpenApiSpec.SchemaType>

internal typealias BodyContent = Map<ContentType, ContentSchema>

data class OpenApiComponents(
    val schemas: Map<String, OpenApiSpec.ObjectType>
)

// External
data class OpenSpecPath(
    val path: String,
)
data class OpenApiSpec(
    val openapi: String = "3.1.0",
    val info: Info,
    val paths: Map<String, Map<String, Path>>,
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
        val summary: String? = null,
        val description: String? = null,
        val tags: List<String>? = null,
        val responses: Map<String, ResponseDetails>? = null,
        val parameters: List<PathParam>? = null,
        val requestBody: RequestBody? = null
    )

    data class RequestBody(
        val required: Boolean,
        val content: BodyContent
    )

    interface NamedObject{
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
        var additionalProperties: ObjectType? = null,
        var oneOf: List<SchemaRef>? = null,
        var required: MutableList<String>? = null
    ) : NamedObject

    data class PathParam(
        override val name: String,
        override val `in`: String,
        override val required: Boolean = true,
        val schema: SchemaType,
    ) : OpenApiSpecParam


    data class SchemaRef(
        val `$ref`: String? = null,
        val type: String? = null
    )

    data class SchemaType(
        val type: String? = null,
        val items: SchemaRef? = null,
        val `$ref`: String? = null,
    )

    data class ResponseDetails(
        val description: String,
        val content: BodyContent
    )
}