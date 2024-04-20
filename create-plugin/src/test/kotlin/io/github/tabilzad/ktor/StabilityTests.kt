package io.github.tabilzad.ktor

import io.github.tabilzad.ktor.TestSourceUtil.loadSourceAndExpected
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString

@OptIn(ExperimentalCompilerApi::class)
class StabilityTests {

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun beforeEach() {
        val existingFile = File(tempDir.toAbsolutePath().pathString + "/openapi.json")

        if (existingFile.exists()) {
            // clear file before each test
            existingFile.writeText("")
        }
    }

    @Test
    fun `should generate correct swagger when KtorDocs annotation is applied to Application`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Paths1")
        generateArrowTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct swagger when KtorDocs annotation is applied to Route`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Paths2")
        generateArrowTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct swagger when KtorDocs annotation is applied to Application with imported or nested routes`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Paths3")
        generateArrowTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct swagger when Route definitions have same path but different endpoint method`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Paths4")
        generateArrowTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct post request body`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("RequestBody")
        generateArrowTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct post request body 2`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("RequestBody2")
        generateArrowTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct endpoint descriptions`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("EndpointDescription")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with path parameters`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("PathParameters")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with path parameters 2`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("PathParameters2")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with path parameters and a body`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("ParamsWithBody")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with query parameters`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("QueryParameters")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with query parameters 2`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("QueryParameters2")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should break down endpoints by tag when tags are specified in annotation`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("EndpointDescriptionTags")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should break down endpoints by tag when tags are specified at application or route level`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Tags")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should ignore private fields or ones annotated with @Transient`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("PrivateFields")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should include private fields or ones annotated with @Transient`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("PrivateFieldsNegation")
        generateArrowTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate response correct response bodies when explicitly specified`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("ResponseBody")
        generateArrowTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    private fun String?.assertWith(expected: String){
        assertThat(this).isNotNull.withFailMessage {
            "swagger file was not generated"
        }
        assertThat(this).isEqualTo(expected.removeTrailingNewLine())
    }
}
