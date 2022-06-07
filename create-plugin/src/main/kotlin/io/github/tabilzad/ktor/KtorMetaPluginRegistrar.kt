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
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_REQUEST_FEATURE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_TITLE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_VER
import io.github.tabilzad.ktor.visitors.ExpressionsVisitor
import org.jetbrains.kotlin.com.intellij.psi.PsiDirectory
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import java.io.File

open class KtorMetaPluginRegistrar : Meta {
    override fun intercept(ctx: CompilerContext): List<CliPlugin> = listOf(ktorDocs)
}

val Meta.ktorDocs: CliPlugin
    get() = "ktorDocs" {
        meta(declarationChecker { ktDeclaration: KtDeclaration,
                                  declarationDescriptor: DeclarationDescriptor,
                                  declarationCheckerContext: DeclarationCheckerContext ->
            if (ktDeclaration.hasAnnotation(KtorDocs::class.simpleName!!)) {
                val function = ktDeclaration as KtNamedFunction
                val containingDirectory = function.containingFile.containingDirectory

                listOf(
                    configuration?.get(ARG_TITLE),
                    configuration?.get(ARG_DESCR),
                    configuration?.get(ARG_VER),
                    configuration?.get(ARG_VER),
                    configuration?.get(ARG_REQUEST_FEATURE),
                ).let { (title, description, version, jsonPath, requestBody) ->

                    val routes = resolveSwaggerPaths(
                        ktDeclaration,
                        requestBody,
                        declarationCheckerContext
                    )
                    saveToFile(
                        containingDirectory = containingDirectory,
                        routes = routes,
                        description = description,
                        version = version,
                        title = title,
                        jsonPath = jsonPath
                    )
                }
            }
        }, enableIr())
    }

private fun saveToFile(
    containingDirectory: PsiDirectory,
    routes: DocRoute,
    description: String?,
    version: String?,
    title: String?,
    jsonPath: String?
) {
    val spec = OpenApiSpec(
        info = OpenApiSpec.Info(
            title = title ?: "Server",
            description = description ?: "",
            version = version ?: "1.0"
        ), paths = reduce(routes).convertToSpec()
    )
    val filePath = containingDirectory.virtualFile.path.split("/main").first() + "/main"
    val resourcesDir = File(filePath).listFiles()?.firstNotNullOf {
        if (listOf("res", "resources").contains(it.name)) {
            it.name
        } else {
            null
        }
    } ?: throw IllegalAccessException("error")
    val customDir = jsonPath ?: "raw"
    val dir = File("$filePath/$resourcesDir/$customDir/")
    dir.mkdir()
    val file = File(dir.path + "/swagger.json")

    jacksonObjectMapper().apply {
        enable(SerializationFeature.INDENT_OUTPUT)
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }.let { mapper ->
        val new = try {
            val existingSpec = mapper.readValue<OpenApiSpec>(file)
            existingSpec.copy(
                paths = existingSpec.paths.plus(spec.paths)
            )
        } catch (ex: Exception) {
            spec
        }
        file.writeText(mapper.writeValueAsString(new))
    }
}

private fun resolveSwaggerPaths(
    ktDeclaration: KtDeclaration,
    requestBody: String?,
    declarationCheckerContext: DeclarationCheckerContext
): DocRoute {
    return ktDeclaration.accept(
        ExpressionsVisitor(
            requestBody?.toBoolean() ?: false,
            declarationCheckerContext.trace.bindingContext
        ), null
    ).first() as DocRoute
}

fun KtAnnotated.hasAnnotation(
    vararg annotationNames: String
): Boolean {
    val names = annotationNames.toHashSet()
    val predicate: (KtAnnotationEntry) -> Boolean = {
        it.typeReference?.typeElement?.safeAs<KtUserType>()?.referencedName in names
    }
    return annotationEntries.any(predicate)
}