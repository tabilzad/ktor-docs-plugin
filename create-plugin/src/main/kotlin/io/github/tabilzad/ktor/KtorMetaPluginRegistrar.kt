package io.github.tabilzad.ktor

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.phases.CompilerContext
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_DESCR
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_PATH
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_REQUEST_FEATURE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_TITLE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_VER
import io.github.tabilzad.ktor.visitors.ExpressionVisitor
import org.jetbrains.kotlin.com.intellij.psi.PsiDirectory
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

open class KtorMetaPluginRegistrar : Meta() {
    override fun intercept(ctx: CompilerContext): List<CliPlugin> = listOf(ktorDocs)
}

object PluginLifeCycle {
    val started: AtomicBoolean = AtomicBoolean(false)
}

val Meta.ktorDocs: CliPlugin
    get() = "ktorDocs" {
        meta(declarationChecker { ktDeclaration: KtDeclaration,
                                  declarationDescriptor: DeclarationDescriptor,
                                  declarationCheckerContext: DeclarationCheckerContext ->

            if (ktDeclaration.hasAnnotation(KtorDocs::class.simpleName!!)) {
                listOf(
                    configuration?.get(ARG_TITLE),
                    configuration?.get(ARG_DESCR),
                    configuration?.get(ARG_VER),
                    configuration?.get(ARG_PATH),
                    configuration?.get(ARG_REQUEST_FEATURE)
                ).let { (title, description, version, jsonPath, requestBody) ->

                    val containingDirectory = ktDeclaration.containingFile.containingDirectory
                    if (PluginLifeCycle.started.compareAndSet(false, true)) {
                        //clear the existing swagger spec at plugin startup
                        containingDirectory.locateOrCreateSwaggerFile(jsonPath).apply {
                            writeText("")
                        }
                    }

                    val context = declarationCheckerContext.trace.bindingContext

                    val expressionsVisitor = ExpressionVisitor(requestBody?.toBoolean() ?: false, context)
                    val rawRoutes = ktDeclaration.accept(
                        expressionsVisitor, null
                    )

                    val routes: List<DocRoute> = if (rawRoutes.any { it !is DocRoute }) {
                        val p = rawRoutes.partition { it is DocRoute }
                        val docRoutes = p.first as List<DocRoute>
                        docRoutes.plus(DocRoute("/", p.second.toMutableList()))
                    } else {
                        rawRoutes as List<DocRoute>
                    }

                    val components = expressionsVisitor.classNames
                        .associateBy { it.fqName ?: "UNKNOWN" }
                        .mapValues { (k, v) ->
                            val objectDefinition = if (v.properties.isNullOrEmpty()) {
                                v.copy(properties = null)
                            } else {
                                v
                            }
                            objectDefinition
                        }
                    saveToFile(
                        containingDirectory = containingDirectory,
                        routes = routes,
                        description = description,
                        version = version,
                        title = title,
                        jsonPath = jsonPath,
                        components
                    )
                }
            }
        }, enableIr())
    }

private fun saveToFile(
    containingDirectory: PsiDirectory,
    routes: List<DocRoute>,
    description: String?,
    version: String?,
    title: String?,
    jsonPath: String?,
    map: Map<String, OpenApiSpec.ObjectType>
) {
    val reducedRoutes = routes
        .map {
            reduce(it)
                .cleanPaths()
                .convertToSpec()
        }
        .reduce { acc, route ->
            acc.plus(route)
        }

    val spec = OpenApiSpec(
        info = OpenApiSpec.Info(
            title = title ?: "Server",
            description = description ?: "",
            version = version ?: "1.0"
        ), paths = reducedRoutes,
        components = OpenApiComponents(
            schemas = map
        )
    )

// jacksonObjectMapper().apply {
//        enable(SerializationFeature.INDENT_OUTPUT)
//        setSerializationInclusion(JsonInclude.Include.NON_NULL)
//    }.writeValueAsString(spec)
    writeToFile(containingDirectory, jsonPath, spec)
}

fun PsiDirectory.locateOrCreateSwaggerFile(customPath: String?): File {
    return if (customPath != null) {
        File("$customPath/openapi.json")
    } else {
        val filePath = virtualFile.path.split("/main").first() + "/main"
        val resourcesDir = File(filePath).listFiles()?.firstNotNullOf {
            if (listOf("res", "resources").contains(it.name)) {
                it.name
            } else {
                null
            }
        } ?: throw IllegalAccessException("error")
        val dir = File("$filePath/$resourcesDir/raw/")
        dir.mkdir()
        File(dir.path + "/openapi.json")
    }
}

private fun writeToFile(
    containingDirectory: PsiDirectory,
    jsonPath: String?,
    spec: OpenApiSpec
) {
    val file = containingDirectory.locateOrCreateSwaggerFile(jsonPath)
    jacksonObjectMapper().apply {
        enable(SerializationFeature.INDENT_OUTPUT)
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }.let { mapper ->
        val new = try {
            val existingSpec = mapper.readValue<OpenApiSpec>(file)
            existingSpec.copy(
                paths = existingSpec.paths.plus(spec.paths),
                components = OpenApiComponents(existingSpec.components.schemas.plus(spec.components.schemas))
            )
        } catch (ex: Exception) {
            spec
        }
        file.writeText(mapper.writeValueAsString(new))
    }
}

@OptIn(UnsafeCastFunction::class)
fun KtAnnotated.hasAnnotation(
    vararg annotationNames: String
): Boolean {
    val names = annotationNames.toHashSet()
    val predicate: (KtAnnotationEntry) -> Boolean = {
        it.typeReference?.typeElement?.safeAs<KtUserType>()?.referencedName in names
    }
    return annotationEntries.any(predicate)
}

@OptIn(UnsafeCastFunction::class)
fun KtAnnotated.findAnnotation(
    annotationName: String
): KtAnnotationEntry? = annotationEntries.find {
    it.typeReference?.typeElement?.safeAs<KtUserType>()?.referencedName == annotationName
}