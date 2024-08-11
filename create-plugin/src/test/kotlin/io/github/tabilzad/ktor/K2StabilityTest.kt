package io.github.tabilzad.ktor

import io.github.tabilzad.ktor.TestSourceUtil.loadSourceAndExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString

class K2StabilityTest {

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
        generateCompilerTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct swagger when KtorDocs annotation is applied to Route`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Paths2")
        generateCompilerTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct swagger when KtorDocs annotation is applied to Application with imported or nested routes`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Paths3")
        generateCompilerTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct swagger when Route definitions have same path but different endpoint method`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Paths4")
        generateCompilerTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct swagger when Route definitions when endpoint http methods don't provide an explicit name`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Paths5")
        generateCompilerTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct post request body`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("RequestBody")
        generateCompilerTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct post request body 2`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("RequestBody2")
        generateCompilerTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct post request body 3`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("RequestBody3")
        generateCompilerTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should include interface fields in request body`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("RequestBody3a")
        generateCompilerTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should be able to handle generic type definitions in request body`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("RequestBody4")
        generateCompilerTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should be able to handle generic type definitions wrapped in a collection`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("RequestBody5")
        generateCompilerTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct endpoint descriptions`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("EndpointDescription")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct body descriptions`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("BodyFieldDescription")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with path parameters`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("PathParameters")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with path parameters 2`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("PathParameters2")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with path parameters and a body`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("ParamsWithBody")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with query parameters`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("QueryParameters")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with query parameters 2`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("QueryParameters2")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should break down endpoints by tag when tags are specified in annotation`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("EndpointDescriptionTags")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should break down endpoints by tag when tags are specified at application or route level`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Tags")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should break down endpoints by tag when tags are specified at application submodule`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Tags2")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should break down endpoints by tag when tags are specified at application submodule 2`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Tags3")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should ignore private fields or ones annotated with @Transient`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("PrivateFields")
        generateCompilerTest(testFile, source)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should include private fields or ones annotated with @Transient`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("PrivateFieldsNegation")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate response correct response bodies when explicitly specified`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("ResponseBody")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should correctly resolve complex descriptions specified on response annotations`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("ResponseBody2")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.findSwagger()?.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should handle abstract or sealed schema definitions`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Abstractions")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
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
