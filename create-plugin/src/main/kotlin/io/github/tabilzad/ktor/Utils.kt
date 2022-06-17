package io.github.tabilzad.ktor

import io.github.tabilzad.ktor.OpenApiSpec.*
import java.io.OutputStream
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

val Iterable<ObjectType>.names get() = mapNotNull { it.name }
fun reduce(e: DocRoute): List<KtorRouteSpec> = e.children.flatMap { child ->
    when (child) {
        is DocRoute -> {
            reduce(
                child.copy(path = e.path + child.path)
            )
        }
        is EndPoint -> {
            listOf(
                KtorRouteSpec(
                    path = e.path + (child.path ?: ""),
                    method = child.method,
                    body = child.body
                )
            )
        }
        else -> {
            emptyList()
        }
    }
}

fun List<KtorRouteSpec>.convertToSpec() = associate {
    it.path to mapOf(
        it.method to Path()
    ).run {
        compute(
            { addPathParams(it) },
            { addPostBody(it) }
        )
    }
}

fun Map<String, Path>.compute(
    vararg modifiers: () -> List<OpenApiSpecParam>
) = modifiers.flatMap { it() }.let {
    this.plus(mapOf("parameters" to it))
}

private fun addPathParams(it: KtorRouteSpec): List<PathParam> {
    val params = "\\{([^}]*)}".toRegex().findAll(it.path).toList()
    return if (params.isNotEmpty()) {
        params.map {
            PathParam(
                name = it.groups[1]?.value ?: "",
                `in` = "path",
                required = true,
                type = "string"
            )
        }
    } else {
        emptyList()
    }
}

private fun addPostBody(it: KtorRouteSpec): List<BodyParam> {
    return if (it.method == "post") {
        listOf(
            BodyParam(
                name = "request",
                `in` = "body",
                schema = Schema("#/components/schemas/${it.body.name?.split(".")?.last()}")
            )
        )
    } else {
        emptyList()
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