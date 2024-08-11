package io.github.tabilzad.ktor

import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyPublicApi
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import java.io.OutputStream
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

val transientAnnotation = FqName("kotlin.jvm.Transient")

fun Boolean.byFeatureFlag(flag: Boolean): Boolean = if (flag) {
    this
} else {
    true
}

fun MemberScope.forEachVariable(configuration: PluginConfiguration, predicate: (PropertyDescriptor) -> Unit) {
    getDescriptorsFiltered(DescriptorKindFilter.VARIABLES)
        .asSequence()
        .map { it.original }
        .filterIsInstance<PropertyDescriptor>()
        .filter {
            it.isEffectivelyPublicApi.byFeatureFlag(configuration.hidePrivateFields)
        }
        .filter {
            (!it.annotations.hasAnnotation(transientAnnotation)).byFeatureFlag(configuration.hideTransients)
        }
        .filter {
            (!(it.backingField?.annotations?.hasAnnotation(transientAnnotation) == true
                    || it.delegateField?.annotations?.hasAnnotation(transientAnnotation) == true
                    || it.setter?.annotations?.hasAnnotation(transientAnnotation) == true
                    || it.getter?.annotations?.hasAnnotation(transientAnnotation) == true
                    )

                    ).byFeatureFlag(
                    configuration.hideTransients
                )
        }
        .toList().forEach { predicate(it) }
}

internal val Iterable<OpenApiSpec.ObjectType>.names get() = mapNotNull { it.fqName }

fun String?.addLeadingSlash() = when {
    this == null -> null
    else -> if (this.startsWith("/")) this else "/$this"
}

internal fun reduce(e: DocRoute): List<KtorRouteSpec> = e.children.flatMap { child ->
    when (child) {
        is DocRoute -> {
            reduce(
                child.copy(
                    path = e.path + child.path.addLeadingSlash(),
                    tags = e.tags merge child.tags
                )
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
                    queryParameters = child.queryParameters?.toList(),
                    responses = child.responses,
                    tags = e.tags merge child.tags
                )
            )
        }

        else -> {
            emptyList()
        }
    }
}

internal fun List<KtorRouteSpec>.cleanPaths() = map {
    it.copy(
        path = it.path
            .replace("//", "/")
            .replace("?", "")
    )
}

internal fun List<KtorRouteSpec>.convertToSpec(): Map<String, Map<String, OpenApiSpec.Path>> = groupBy { it ->
    it.path
}.mapValues { (key, value) ->
    value.associate {
        it.method to OpenApiSpec.Path(
            summary = it.summary,
            description = it.description,
            tags = it.tags?.toList()?.sorted(),
            parameters = addPathParams(it) merge addQueryParams(it),
            requestBody = addPostBody(it),
            responses = it.responses
        )
    }
}

infix fun <T> List<T>?.merge(params: List<T>?): List<T>? = this?.plus(params ?: emptyList()) ?: params

infix fun <T> Set<T>?.merge(params: Set<T>?): Set<T>? = this?.plus(params ?: emptyList()) ?: params

private fun addPathParams(it: KtorRouteSpec): List<OpenApiSpec.PathParam>? {
    val params = "\\{([^}]*)}".toRegex().findAll(it.path).toList()
    return if (params.isNotEmpty()) {
        params.mapNotNull {
            val pathParamName = it.groups[1]?.value
            if (pathParamName.isNullOrBlank()) {
                null
            } else {
                OpenApiSpec.PathParam(
                    name = pathParamName.replace("?", ""),
                    `in` = "path",
                    required = !pathParamName.contains("?"),
                    schema = OpenApiSpec.SchemaType("string")
                )
            }
        }
    } else {
        null
    }
}

private fun addQueryParams(it: KtorRouteSpec): List<OpenApiSpec.PathParam>? {
    return it.queryParameters?.map {
        OpenApiSpec.PathParam(
            name = it,
            `in` = "query",
            required = false,
            schema = OpenApiSpec.SchemaType("string")
        )

    }
}

private fun addPostBody(it: KtorRouteSpec): OpenApiSpec.RequestBody? {
    return if (it.method != "get" && it.body.contentBodyRef != null) {
        OpenApiSpec.RequestBody(
            required = true,
            content = mapOf(
                ContentType.APPLICATION_JSON to mapOf(
                    "schema" to OpenApiSpec.SchemaType(
                        `$ref` = "${it.body.contentBodyRef}"
                    )
                )
            )
        )
    } else {
        null
    }
}

fun CompilerConfiguration?.buildPluginConfiguration(): PluginConfiguration = PluginConfiguration(
    isEnabled = this?.get(SwaggerConfigurationKeys.ARG_ENABLED) ?: true,
    saveInBuild = this?.get(SwaggerConfigurationKeys.ARG_SAVE_IN_BUILD) ?: false,
    format = this?.get(SwaggerConfigurationKeys.ARG_FORMAT) ?: "yaml",
    title = this?.get(SwaggerConfigurationKeys.ARG_TITLE) ?: "Open API Specification",
    description = this?.get(SwaggerConfigurationKeys.ARG_DESCR) ?: "",
    version = this?.get(SwaggerConfigurationKeys.ARG_VER) ?: "1.0.0",
    filePath = this?.get(SwaggerConfigurationKeys.ARG_PATH) ?: "openapi.yaml",
    buildPath = this?.get(SwaggerConfigurationKeys.ARG_BUILD_PATH)
        ?: throw CompilationException("Failed resolve the build folder path for the module", null, null),
    modulePath = this?.get(SwaggerConfigurationKeys.ARG_MODULE_PATH)
        ?: throw CompilationException("Failed resolve the build folder path for the module", null, null),
    requestBody = this?.get(SwaggerConfigurationKeys.ARG_REQUEST_FEATURE) ?: true,
    hideTransients = this?.get(SwaggerConfigurationKeys.ARG_HIDE_TRANSIENTS) ?: true,
    hidePrivateFields = this?.get(SwaggerConfigurationKeys.ARG_HIDE_PRIVATE) ?: true,
    deriveFieldRequirementFromTypeNullability = this?.get(SwaggerConfigurationKeys.ARG_DERIVE_PROP_REQ) ?: true
)

internal fun Class<*>.resolveDefinitionTo(obj: OpenApiSpec.ObjectType): OpenApiSpec.ObjectType {
    kotlin.memberProperties.forEach { field ->
        if (field.returnType.jvmErasure.java.isPrimitive) {
            obj.properties?.set(
                field.name, OpenApiSpec.ObjectType(
                    type = field.returnType.jvmErasure.java.simpleName.lowercase(),
                    properties = null
                )
            )
        } else {
            obj.properties?.set(
                field.name, OpenApiSpec.ObjectType(
                    type = "object",
                    properties = mutableMapOf(
                        field.name to field.javaClass.resolveDefinitionTo(
                            OpenApiSpec.ObjectType(
                                "object",
                                mutableMapOf()
                            )
                        )
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
