package io.github.tabilzad.ktor

import arrow.meta.plugin.testing.*
import io.github.tabilzad.ktor.TestSourceUtil.loadSourceAndExpected
import org.assertj.core.api.Assertions.*
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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
    fun beforeEach(){
        val existingFile = File(tempDir.toAbsolutePath().pathString+"/openapi.json")

        if(existingFile.exists()){
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
            assertThat(generatedSwagger).isNotNull.withFailMessage {
                "swagger file was not generated"
            }
            assertThat(generatedSwagger).isEqualTo(expected)
        }
    }

    @Test
    fun `should generate correct swagger when KtorDocs annotation is applied to Route`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Paths2")
        generateArrowTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            assertThat(generatedSwagger).isNotNull.withFailMessage {
                "swagger file was not generated"
            }
            assertThat(generatedSwagger).isEqualTo(expected)
        }
    }

    @Test
    fun `should generate correct swagger when KtorDocs annotation is applied to Application with imported or nested routes`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("Paths3")
        generateArrowTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            assertThat(generatedSwagger).isNotNull.withFailMessage {
                "swagger file was not generated"
            }
            assertThat(generatedSwagger).isEqualTo(expected)
        }
    }

    @Test
    fun `should generate correct post request body`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("RequestBody")
        generateArrowTest(testFile, source)

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            assertThat(generatedSwagger).isNotNull.withFailMessage {
                "swagger file was not generated"
            }
            assertThat(generatedSwagger).isEqualTo(expected)
        }
    }

    @Test
    fun `should generate correct endpoint descriptions`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("EndpointDescription")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()

        assertThat(result).isNotNull.withFailMessage {
            "swagger file was not generated"
        }
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @Disabled("not working yet")
    fun `should generate correct post request body with polymorphic types`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("PolymorphicRequest")
        assertThis(
            CompilerTest(
                config = { getTestConfig(testFile.path) },
                assert = { compiles },
                code = { loadBaseSources(source) })
        )

        testFile.findSwagger()?.readText().let { generatedSwagger ->
            assertThat(generatedSwagger).isNotNull.withFailMessage {
                "swagger file was not generated"
            }
            assertThat(generatedSwagger).isEqualTo(expected)
        }
    }
    @Test
    fun `should generate correct swagger definitions for endpoint with path parameters`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("PathParameters")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()

        assertThat(result).isNotNull.withFailMessage {
            "swagger file was not generated"
        }
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `should generate correct swagger definitions for endpoint with query parameters`() {
        val testFile = File(tempDir.toAbsolutePath().pathString)
        val (source, expected) = loadSourceAndExpected("QueryParameters")
        generateArrowTest(testFile, source)
        val result = testFile.findSwagger()?.readText()

        assertThat(result).isNotNull.withFailMessage {
            "swagger file was not generated"
        }
        assertThat(result).isEqualTo(expected)
    }
}