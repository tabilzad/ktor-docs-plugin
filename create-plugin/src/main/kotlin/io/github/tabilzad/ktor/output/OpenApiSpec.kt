package io.github.tabilzad.ktor.output

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.sun.security.ntlm.Server
import io.github.tabilzad.ktor.ContentType
import io.github.tabilzad.ktor.OpenApiSpecParam
import kotlinx.serialization.Serializable
import java.nio.file.Path

internal typealias ContentSchema = Map<String, OpenApiSpec.SchemaType>

internal typealias BodyContent = Map<ContentType, ContentSchema>

data class OpenApiSpec(
    val openapi: String = "3.1.0",
    val info: Info?,
    val servers: List<Server>? = null,
    val paths: Map<String, Map<String, Path>>,
    val components: OpenApiComponents,
    val security: List<Map<String, List<String>>>? = null
) {

    @Serializable
    data class Info(
        val title: String? = null,
        val description: String? = null,
        val version: String? = null,
        val contact: Contact? = null,
        val license: License? = null,
    ) {

        @Serializable
        data class Contact(
            val name: String? = null,
            val email: String? = null,
            val url: String? = null
        )

        @Serializable
        data class License(
            val name: String? = null,
            val url: String? = null
        )
    }

    data class Server(val url: String)

    data class Path(
        val summary: String? = null,
        val description: String? = null,
        val operationId: String? = null,
        val tags: List<String>? = null,
        val responses: Map<String, ResponseDetails>? = null,
        val parameters: List<Parameter>? = null,
        val requestBody: RequestBody? = null,
        val security: List<Map<String, List<String>>>? = null
    )

    @Serializable
    data class SecurityScheme(
        val type: String, // "apiKey", "http", "oauth2", etc.
        val scheme: String? = null, // "basic", "bearer", etc. (for "http")
        val `in`: String? = null, // can be "header", "query" or "cookie"
        val name: String? = null, // name of the header, query parameter or cookie
        val bearerFormat: String? = null, // optional, arbitrary value for documentation purposes, eg. JWT
        val description: String? = null,
        val flows: Map<String, OAuthFlow>? = null, // Used for OAuth flow specs
        val openIdConnectUrl: String? = null,
    )

    @Serializable
    data class OAuthFlow(
        val authorizationUrl: String,
        val tokenUrl: String? = null,
        val refreshUrl: String? = null,
        val scopes: Map<String, String>? = null,
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
        var required: MutableList<String>? = null,
        var format: String? = null
    ) : NamedObject

    data class Parameter(
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
        val schemas: Map<String, ObjectType>,
        val securitySchemes: Map<String, SecurityScheme>? = null
    )

}
