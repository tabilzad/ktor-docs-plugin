package io.github.tabilzad.ktor.output

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.tabilzad.ktor.ContentType
import io.github.tabilzad.ktor.OpenApiSpecParam

internal typealias ContentSchema = Map<String, OpenApiSpec.SchemaType>

internal typealias BodyContent = Map<ContentType, ContentSchema>
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

    interface NamedObject {
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
        override val description: String? = null,
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

    data class OpenApiComponents(
        val schemas: Map<String, ObjectType>
    )

}
