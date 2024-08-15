package io.github.tabilzad.ktor

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File



fun OpenApiSpec.serializeAndWriteTo(configuration: PluginConfiguration) {
    val file = File(configuration.filePath)

    getJacksonBy(configuration.format).let { mapper ->
        val new = try {
            val existingSpec = mapper.readValue<OpenApiSpec>(file)
            existingSpec.mergeAndResolveConflicts(this)
        } catch (ex: Exception) {
            this
        }
        val sorted = new.copy(
            components = new.components.copy(
                schemas = new.components.schemas.toSortedMap()
                    .mapValues {
                        it.value.copy(properties = it.value.properties?.toSortedMap())
                    }
            )
        )
        file.writeText(mapper.writeValueAsString(sorted))
    }
}

fun OpenApiSpec.mergeAndResolveConflicts(newSpec: OpenApiSpec): OpenApiSpec {

    val (duplicatePaths, newDistinctPaths) = newSpec.paths.entries.partition { entry ->
        paths.containsKey(entry.key)
    }.let { (first, second) ->
        first.associate { it.key to it.value } to second.associate { it.key to it.value }
    }

    val resolvedConflicts = duplicatePaths.mapValues { (path, endpoint) ->
        endpoint.mapValues { (method, endpointValue) ->
            paths[path]?.get(method)?.let {
                endpointValue.copy(
                    tags = (it.tags merge endpointValue.tags)?.toSet()?.toList()
                )
            } ?: endpointValue
        }
    }
    return copy(
        paths = paths + newDistinctPaths + resolvedConflicts,
        components = OpenApiComponents(components.schemas.plus(newSpec.components.schemas))
    )
}

fun getJacksonBy(format: String): ObjectMapper = when (format) {
    "yaml" -> ObjectMapper(YAMLFactory()).registerKotlinModule()
    else -> jacksonObjectMapper()
}.apply {
    enable(SerializationFeature.INDENT_OUTPUT)
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}
