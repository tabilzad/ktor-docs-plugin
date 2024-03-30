package io.github.tabilzad.ktor

import arrow.meta.plugin.testing.*
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import java.io.FileNotFoundException

object TestSourceUtil {
    fun loadSourceCodeFrom(fileName: String): String =
        this.javaClass.getResource("/sources/$fileName.kt")?.readText()
            ?: throw FileNotFoundException("$fileName does not exist")


    fun loadSourceAndExpected(fileName: String): Pair<String, String> =
        loadSourceCodeFrom(fileName) to loadExpectation("${fileName}-expected")


    val loadAnnotations by lazy {
        this.javaClass.getResource("/sources/annotations/TestAnnotations.kt")
            ?: throw FileNotFoundException("annotations don't exist")
    }

    val loadRequests by lazy {
        this.javaClass.getResource("/sources/requests/RequestDataClasses.kt")
            ?: throw FileNotFoundException("annotations don't exist")
    }

    private fun loadExpectation(fileName: String): String =
        this.javaClass.getResource("/expected/$fileName.json")?.readText()
            ?: throw FileNotFoundException("$fileName does not exist")
}

@OptIn(ExperimentalCompilerApi::class)
fun generateArrowTest(
    testFile: File,
    source: String,
    hideTransient: Boolean = true,
    hidePrivate: Boolean = true,
) {
    assertThis(
        CompilerTest(
            config = { getTestConfig(testFile.path, hideTransient, hidePrivate) },
            assert = { compiles },
            code = { loadBaseSources(source) })
    )
}

fun File.findSwagger() = listFiles()?.find { it.name.contains("openapi.json") }
fun CompilerTest.Companion.loadBaseSources(source: String): Code {
    val annotationsFile = TestSourceUtil.loadAnnotations
    val requestsDefinitions = TestSourceUtil.loadRequests
    return sources(
        Code.Source(annotationsFile.file, annotationsFile.readText()),
        Code.Source(requestsDefinitions.file, requestsDefinitions.readText()),
        Code.Source("TestSubject.kt", source)
    )
}

@ExperimentalCompilerApi
fun getTestConfig(
    testFilePath: String,
    hideTransient: Boolean = true,
    hidePrivate: Boolean = true,
): List<Config> {
    val clp = KtorDocsCommandLineProcessor()
    return listOf(
        CompilerTest.addMetaPlugins(KtorMetaPluginRegistrar()),
        CompilerTest.addCommandLineProcessors(clp),
        CompilerTest.addDependencies(
            Dependency("ktor:2.2.4"),
            Dependency("ktor-server-core:2.2.4"),
            Dependency("ktor-utils:2.2.4"),
            Dependency("ktor-server-netty:2.2.4"),
            Dependency("ktor-http:2.2.4"),
            Dependency("kotlinx-coroutines-core:1.6.4")
        ),
        CompilerTest.addPluginOptions(
            PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.isEnabled.optionName,
                "true"
            ),
            PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.requestSchema.optionName,
                "true"
            ),
            PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.descOption.optionName,
                "test"
            ),
            PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.formatOption.optionName,
                "json"
            ),
            PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.pathOption.optionName,
                testFilePath
            ),
            PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.buildPath.optionName,
                testFilePath
            ),
            PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.modulePath.optionName,
                testFilePath
            ),
            PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.hideTransientFields.optionName,
                hideTransient.toString()
            ),
            PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.hidePrivateAndInternalFields.optionName,
                hidePrivate.toString()
            )
        )
    )
}
