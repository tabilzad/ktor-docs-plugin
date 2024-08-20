package io.github.tabilzad.ktor

import io.github.tabilzad.ktor.TestSourceUtil.loadSourceAndExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class K2StabilityTest {

    @TempDir
    lateinit var tempDir: Path

    private val testFile get() = File(tempDir.toFile(), "openapi.json")

    @BeforeEach
    fun beforeEach() {
        val existingFile = testFile

        if (existingFile.exists()) {
            // clear file before each test
            existingFile.writeText("")
        }
    }

    @Test
    fun `should generate correct swagger when KtorDocs annotation is applied to Application`() {
        val (source, expected) = loadSourceAndExpected("Paths1")
        generateCompilerTest(testFile, source)

        testFile.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct swagger when KtorDocs annotation is applied to Route`() {
        val (source, expected) = loadSourceAndExpected("Paths2")
        generateCompilerTest(testFile, source)

        testFile.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct swagger when KtorDocs annotation is applied to Application with imported or nested routes`() {
        val (source, expected) = loadSourceAndExpected("Paths3")
        generateCompilerTest(testFile, source)

        testFile.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct swagger when Route definitions have same path but different endpoint method`() {
        val (source, expected) = loadSourceAndExpected("Paths4")
        generateCompilerTest(testFile, source)

        testFile.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct swagger when Route definitions when endpoint http methods don't provide an explicit name`() {
        val (source, expected) = loadSourceAndExpected("Paths5")
        generateCompilerTest(testFile, source)

        testFile.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct post request body`() {
        val (source, expected) = loadSourceAndExpected("RequestBody")
        generateCompilerTest(testFile, source)

        testFile.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct post request body 2`() {
        val (source, expected) = loadSourceAndExpected("RequestBody2")
        generateCompilerTest(testFile, source)

        testFile.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct post request body 3`() {
        val (source, expected) = loadSourceAndExpected("RequestBody3")
        generateCompilerTest(testFile, source)

        testFile.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should include interface fields in request body`() {
        val (source, expected) = loadSourceAndExpected("RequestBody3a")
        generateCompilerTest(testFile, source)

        testFile.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should be able to handle generic type definitions in request body`() {
        val (source, expected) = loadSourceAndExpected("RequestBody4")
        generateCompilerTest(testFile, source)

        testFile.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should be able to handle generic type definitions wrapped in a collection`() {
        val (source, expected) = loadSourceAndExpected("RequestBody5")
        generateCompilerTest(testFile, source)

        testFile.readText().let { generatedSwagger ->
            generatedSwagger.assertWith(expected)
        }
    }

    @Test
    fun `should generate correct endpoint descriptions`() {
        val (source, expected) = loadSourceAndExpected("EndpointDescription")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct body descriptions`() {
        val (source, expected) = loadSourceAndExpected("BodyFieldDescription")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with path parameters`() {
        val (source, expected) = loadSourceAndExpected("PathParameters")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with path parameters 2`() {
        val (source, expected) = loadSourceAndExpected("PathParameters2")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with path parameters and a body`() {
        val (source, expected) = loadSourceAndExpected("ParamsWithBody")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with query parameters`() {
        val (source, expected) = loadSourceAndExpected("QueryParameters")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with query parameters 2`() {
        val (source, expected) = loadSourceAndExpected("QueryParameters2")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should break down endpoints by tag when tags are specified in annotation`() {
        val (source, expected) = loadSourceAndExpected("EndpointDescriptionTags")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should break down endpoints by tag when tags are specified at application or route level`() {
        val (source, expected) = loadSourceAndExpected("Tags")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should break down endpoints by tag when tags are specified at application submodule`() {
        val (source, expected) = loadSourceAndExpected("Tags2")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should break down endpoints by tag when tags are specified at application submodule 2`() {
        val (source, expected) = loadSourceAndExpected("Tags3")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should break down endpoints by tag when tags are appplies to common routes or endpoints`() {
        val (source, expected) = loadSourceAndExpected("Tags4")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should ignore private fields or ones annotated with @Transient`() {
        val (source, expected) = loadSourceAndExpected("PrivateFields")
        generateCompilerTest(testFile, source)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should include private fields or ones annotated with @Transient`() {
        val (source, expected) = loadSourceAndExpected("PrivateFieldsNegation")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should generate response correct response bodies when explicitly specified`() {
        val (source, expected) = loadSourceAndExpected("ResponseBody")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should correctly resolve complex descriptions specified on response annotations`() {
        val (source, expected) = loadSourceAndExpected("ResponseBody2")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should handle abstract or sealed schema definitions`() {
        val (source, expected) = loadSourceAndExpected("Abstractions")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should handle Moshi annotated properties and data class constructor parameters`() {
        val (source, expected) = loadSourceAndExpected("MoshiAnnotated")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should handle kotlinx serialization annotated properties and data class constructor parameters`() {
        val (source, expected) = loadSourceAndExpected("SerializationAnnotated")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should resolve request body schema directly from http method parameter if it's not a resource`() {
        val (source, expected) = loadSourceAndExpected("RequestBodyParam")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should resolve endpoint spec from type-safe ktor resources`() {
        val (source, expected) = loadSourceAndExpected("Resources")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should resolve endpoint spec from type-safe ktor resources with query params`() {
        val (source, expected) = loadSourceAndExpected("ResourcesWithParams")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    @Test
    fun `should resolve described resources`() {
        val (source, expected) = loadSourceAndExpected("DescribedResources")
        generateCompilerTest(testFile, source, hideTransient = false, hidePrivate = false)
        val result = testFile.readText()
        result.assertWith(expected)
    }

    private fun String?.assertWith(expected: String){
        assertThat(this).isNotNull.withFailMessage {
            "swagger file was not generated"
        }
        assertThat(this).isEqualTo(expected.removeTrailingNewLine())
    }
}
