package io.github.tabilzad.ktor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.classgraph.ClassGraph
import org.assertj.core.api.Assertions
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintStream

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

    val loadNativeAnnotations by lazy {
        this.javaClass.getResource("/io/github/tabilzad/ktor/Annotations.kt")
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

fun String.removeTrailingNewLine(): String =
    if (endsWith(System.lineSeparator())) dropLast(System.lineSeparator().length) else this

private fun classpathOf(dependency: String): File {
    val file =
        ClassGraph().classpathFiles.firstOrNull { classpath ->
            dependenciesMatch(classpath, dependency)
        }
    println("classpath: ${ClassGraph().classpathFiles}")
    Assertions.assertThat(file)
        .`as`("$dependency not found in test runtime. Check your build configuration.")
        .isNotNull
    return file!!
}

private fun sanitizeClassPathFileName(dep: String): String =
    buildList {
        var skip = false
        add(dep.first())
        dep.reduce { a, b ->
            if (a == '-' && b.isDigit()) skip = true
            if (!skip) add(b)
            b
        }
        if (skip) removeLast()
    }
        .joinToString("")
        .replace("-jvm.jar", "")
        .replace("-jvm", "")


private fun dependenciesMatch(classpath: File, dependency: String): Boolean {
    val dep = classpath.name
    val dependencyName = sanitizeClassPathFileName(dep)
    val testdep = dependency.substringBefore(":")
    return testdep == dependencyName
}

@OptIn(ExperimentalCompilerApi::class)
fun generateCompilerTest(
    testFile: File,
    testSubjectSource: String,
    hideTransient: Boolean = true,
    hidePrivate: Boolean = true,
) {

    val testFilePath = testFile.path
    val clp = KtorDocsCommandLineProcessor()
    val compilationData = KotlinCompilation().apply {
        val testSources = workingDir.resolve("sources")
        System.setProperty("arrow.meta.generate.source.dir", testSources.absolutePath)
        compilerPluginRegistrars = listOf(KtorMetaPluginRegistrar())
        commandLineProcessors = listOf(clp)
        classpaths = deps.map { classpathOf(it) }
        val loadBaseSources2 = loadBaseSources2(testSubjectSource)
        sources = loadBaseSources2
        kotlincArguments = emptyList()
        jvmTarget = "1.8"
        messageOutputStream =
            object : PrintStream(System.out) {

                private val kotlincErrorRegex = Regex("^e:")

                override fun write(buf: ByteArray, off: Int, len: Int) {
                    val newLine =
                        String(buf, off, len).run { replace(kotlincErrorRegex, "error found:") }.toByteArray()

                    super.write(newLine, off, newLine.size)
                }
            }
        pluginOptions = listOf(
            com.tschuchort.compiletesting.PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.isEnabled.optionName,
                "true"
            ),
            com.tschuchort.compiletesting.PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.requestSchema.optionName,
                "true"
            ),
            com.tschuchort.compiletesting.PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.descOption.optionName,
                "test"
            ),
            com.tschuchort.compiletesting.PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.formatOption.optionName,
                "json"
            ),
            com.tschuchort.compiletesting.PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.pathOption.optionName,
                testFilePath
            ),
            com.tschuchort.compiletesting.PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.buildPath.optionName,
                testFilePath
            ),
            com.tschuchort.compiletesting.PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.modulePath.optionName,
                testFilePath
            ),
            com.tschuchort.compiletesting.PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.hideTransientFields.optionName,
                hideTransient.toString()
            ),
            com.tschuchort.compiletesting.PluginOption(
                clp.pluginId,
                KtorDocsCommandLineProcessor.hidePrivateAndInternalFields.optionName,
                hidePrivate.toString()
            )
        )
    }
    compilationData.compile()
}

fun File.findSwagger() = listFiles()?.find { it.name.contains("openapi.json") }

fun loadBaseSources2(source: String): List<SourceFile> {
    val annotationsFile = TestSourceUtil.loadAnnotations
    val nativeAnnotationsFile = TestSourceUtil.loadNativeAnnotations
    val requestsDefinitions = TestSourceUtil.loadRequests
    return listOf(
        SourceFile.kotlin(annotationsFile.file, annotationsFile.readText().trimMargin()),
        SourceFile.kotlin(nativeAnnotationsFile.file, nativeAnnotationsFile.readText().trimMargin()),
        SourceFile.kotlin(requestsDefinitions.file, requestsDefinitions.readText().trimMargin()),
        SourceFile.kotlin("TestSubject.kt", source)
    )
}

val deps = arrayOf(
    "ktor:2.2.4",
    "ktor-server-core:2.2.4",
    "ktor-utils:2.2.4",
    "ktor-server-netty:2.2.4",
    "ktor-http:2.2.4",
    "kotlinx-coroutines-core:1.6.4")
