package io.github.tabilzad.ktor

import io.github.tabilzad.ktor.OpenApiSpec.*
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import java.io.OutputStream
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

fun MemberScope.forEachVariable(predicate: (PropertyDescriptor) -> Unit) {
    getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
        .map { it.original }
        .filterIsInstance<PropertyDescriptorImpl>()
        .filter {
            it.backingField?.annotations?.hasAnnotation(FqName("kotlin.jvm.Transient"))?.let { exists ->
                !exists
            } ?: true
        }.forEach {
            predicate(it)
        }
}

val Iterable<OpenApiSpec.ObjectType>.names get() = mapNotNull { it.fqName }

fun String?.addLeadingSlash() = when {
    this == null -> null
    else -> if (this.startsWith("/")) this else "/$this"
}

fun reduce(e: DocRoute): List<KtorRouteSpec> = e.children.flatMap { child ->
    when (child) {
        is DocRoute -> {
            reduce(
                child.copy(path = e.path + child.path.addLeadingSlash())
            )
        }

        is EndPoint -> {
            listOf(
                KtorRouteSpec(
                    path = e.path + (child.path.addLeadingSlash() ?: ""),
                    method = child.method,
                    body = child.body,
                    summary = child.summary,
                    description = child.description,
                    queryParameters = child.queryParameters
                )
            )
        }

        else -> {
            emptyList()
        }
    }
}

fun List<KtorRouteSpec>.cleanPaths() = map {
    it.copy(
        path = it.path
            .replace("//", "/")
            .replace("?", "")
    )
}

fun List<KtorRouteSpec>.convertToSpec() = associate {
    it.path to mapOf(
        it.method to Path(
            summary = it.summary,
            description = it.description,
            parameters = addPathParams(it) merge addQueryParams(it),
            requestBody = addPostBody(it)
        )
    )
}

infix fun <T> List<T>?.merge(params: List<T>?): List<T>? = this?.plus(params ?: emptyList()) ?: params
//fun compute(
//    vararg modifiers: () -> List<OpenApiSpecParam>
//) = modifiers.flatMap { it() }

private fun addPathParams(it: KtorRouteSpec): List<PathParam>? {
    val params = "\\{([^}]*)}".toRegex().findAll(it.path).toList()
    return if (params.isNotEmpty()) {
        params.mapNotNull {
            val pathParamName = it.groups[1]?.value
            if (pathParamName.isNullOrBlank()) {
                null
            } else {
                PathParam(
                    name = pathParamName.replace("?", ""),
                    `in` = "path",
                    required = true,
                    schema = SchemaType("string")
                )
            }
        }
    } else {
        null
    }
}

private fun addQueryParams(it: KtorRouteSpec): List<PathParam>? {
    return it.queryParameters?.map {
        PathParam(
            name = it,
            `in` = "query",
            required = false,
            schema = SchemaType("string")
        )

    }
}

private fun addPostBody(it: KtorRouteSpec): RequestBody? {
    return if (it.method == "post" && it.body.contentBodyRef != null) {

        //val ref = it.body.name
        RequestBody(
            required = true,
            content = mapOf(
                ContentType.APPLICATION_JSON to mapOf(
                    "schema" to SchemaRef(
                        "${it.body.contentBodyRef}"
                    )
                )
            )
        )
    } else {
        null
    }
}

fun Class<*>.resolveDefinitionTo(obj: ObjectType): ObjectType {
    kotlin.memberProperties.forEach { field ->
        if (field.returnType.jvmErasure.java.isPrimitive) {
            obj.properties?.set(
                field.name, ObjectType(
                    type = field.returnType.jvmErasure.java.simpleName.lowercase(),
                    properties = null
                )
            )
        } else {
            obj.properties?.set(
                field.name, ObjectType(
                    type = "object",
                    properties = mutableMapOf(
                        field.name to field.javaClass.resolveDefinitionTo(ObjectType("object", mutableMapOf()))
                    )
                )
            )
        }
    }
    return obj
}

operator fun OutputStream.plusAssign(str: String) {
    this.write(str.toByteArray())
}

object HttpCodeResolver {
    fun resolve(code: String?): String = codes[code] ?: "200"
    private val codes = mapOf(
        "Continue" to "100",
        "SwitchingProtocols" to "101",
        "Processing" to "102",
        "OK" to "200",
        "Created" to "201",
        "Accepted" to "202",
        "NonAuthoritativeInformation" to "203",
        "NoContent" to "204",
        "ResetContent" to "205",
        "PartialContent" to "206",
        "MultiStatus" to "207",
        "MultipleChoices" to "300",
        "MovedPermanently" to "301",
        "Found" to "302",
        "SeeOther" to "303",
        "NotModified" to "304",
        "UseProxy" to "305",
        "SwitchProxy" to "306",
        "TemporaryRedirect" to "307",
        "PermanentRedirect" to "308",
        "BadRequest" to "400",
        "Unauthorized" to "401",
        "PaymentRequired" to "402",
        "Forbidden" to "403",
        "NotFound" to "404",
        "MethodNotAllowed" to "405",
        "NotAcceptable" to "406",
        "ProxyAuthenticationRequired" to "407",
        "RequestTimeout" to "408",
        "Conflict" to "409",
        "Gone" to "410",
        "LengthRequired" to "411",
        "PreconditionFailed" to "412",
        "PayloadTooLarge" to "413",
        "RequestURITooLong" to "414",
        "UnsupportedMediaType" to "415",
        "RequestedRangeNotSatisfiable" to "416",
        "ExpectationFailed" to "417",
        "UnprocessableEntity" to "422",
        "Locked" to "423",
        "FailedDependency" to "424",
        "UpgradeRequired" to "426",
        "TooManyRequests" to "429",
        "RequestHeaderFieldTooLarge" to "431",
        "InternalServerError" to "500",
        "NotImplemented" to "501",
        "BadGateway" to "502",
        "ServiceUnavailable" to "503",
        "GatewayTimeout" to "504",
        "VersionNotSupported" to "505",
        "VariantAlsoNegotiates" to "506",
        "InsufficientStorage" to "507"
    )

}
