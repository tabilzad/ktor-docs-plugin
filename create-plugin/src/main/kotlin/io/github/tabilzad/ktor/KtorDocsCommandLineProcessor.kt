package io.github.tabilzad.ktor

import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_BUILD_PATH
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_DESCR
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_ENABLED
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_FORMAT
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_HIDE_PRIVATE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_HIDE_TRANSIENTS
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_MODULE_PATH
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_PATH
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_REQUEST_FEATURE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_SAVE_IN_BUILD
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_TITLE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_VER
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_BUILD_PATH
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_DESCR
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_FORMAT
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_HIDE_PRIVATE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_HIDE_TRANSIENT
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_IS_ENABLED
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_MODULE_PATH
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_PATH
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_REQUEST_BODY
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_SAVE_IN_BUILD
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_TITLE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_VER
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey


object SwaggerConfigurationKeys {
    const val OPTION_TITLE = "title"
    const val OPTION_IS_ENABLED = "enabled"
    const val OPTION_SAVE_IN_BUILD = "saveInBuild"
    const val OPTION_MODULE_PATH = "modulePath"
    const val OPTION_BUILD_PATH = "buildPath"
    const val OPTION_DESCR = "description"
    const val OPTION_VER = "version"
    const val OPTION_PATH = "filePath"
    const val OPTION_REQUEST_BODY = "generateRequestSchemas"
    const val OPTION_HIDE_TRANSIENT = "hideTransientFields"
    const val OPTION_HIDE_PRIVATE = "hidePrivateAndInternalFields"
    const val OPTION_FORMAT = "format"

    val ARG_ENABLED = CompilerConfigurationKey.create<Boolean>(OPTION_IS_ENABLED)
    val ARG_MODULE_PATH = CompilerConfigurationKey.create<String>(OPTION_MODULE_PATH)
    val ARG_BUILD_PATH = CompilerConfigurationKey.create<String>(OPTION_BUILD_PATH)
    val ARG_SAVE_IN_BUILD = CompilerConfigurationKey.create<Boolean>(OPTION_SAVE_IN_BUILD)
    val ARG_TITLE = CompilerConfigurationKey.create<String>(OPTION_TITLE)
    val ARG_DESCR = CompilerConfigurationKey.create<String>(OPTION_DESCR)
    val ARG_VER = CompilerConfigurationKey.create<String>(OPTION_VER)
    val ARG_PATH = CompilerConfigurationKey.create<String>(OPTION_PATH)
    val ARG_REQUEST_FEATURE = CompilerConfigurationKey.create<Boolean>(OPTION_REQUEST_BODY)
    val ARG_HIDE_TRANSIENTS = CompilerConfigurationKey.create<Boolean>(OPTION_HIDE_TRANSIENT)
    val ARG_HIDE_PRIVATE = CompilerConfigurationKey.create<Boolean>(OPTION_HIDE_PRIVATE)
    val ARG_FORMAT = CompilerConfigurationKey.create<String>(OPTION_FORMAT)
}

@OptIn(ExperimentalCompilerApi::class)
class KtorDocsCommandLineProcessor : CommandLineProcessor {
    companion object {
        val isEnabled = CliOption(
            OPTION_IS_ENABLED,
            "Should plugin run",
            "Is plugin enabled",
            false
        )

        val saveInBuild = CliOption(
            OPTION_SAVE_IN_BUILD,
            "Should save spec to build directory",
            "Should save generated file to the module's build directory",
            false
        )

        val buildPath = CliOption(
            OPTION_BUILD_PATH,
            "Build directory path of module the plugin is applied to",
            "Provided by gradle",
            false
        )

        val modulePath = CliOption(
            OPTION_MODULE_PATH,
            "The path of module the plugin is applied to",
            "Provided by gradle",
            false
        )
        private val titleOption = CliOption(
            OPTION_TITLE,
            "Server title/name",
            "Ktor Swagger page title",
            false
        )
        val descOption = CliOption(
            OPTION_DESCR,
            "Description of the server",
            "Description of Ktor Server",
            false
        )
        private val versionOption = CliOption(
            OPTION_VER,
            "Server version",
            "Ktor Server version",
            false
        )
        val pathOption = CliOption(
            OPTION_PATH,
            "Custom Absolute Path",
            "Custom absolute path for generated specification",
            false
        )
        val requestSchema = CliOption(
            OPTION_REQUEST_BODY,
            "Request Schema",
            "Enable request body definitions",
            false
        )
        val hideTransientFields = CliOption(
            OPTION_HIDE_TRANSIENT,
            "Hide Transient fields",
            "Hide fields annotated with @Transient in body definitions",
            false
        )
        val hidePrivateAndInternalFields = CliOption(
            OPTION_HIDE_PRIVATE,
            "Hide private or internal fields",
            "Hide private or internal fields in body definitions",
            false
        )
        val formatOption = CliOption(
            OPTION_FORMAT,
            "Specification format",
            "Either yaml or json, json by default",
            false
        )
    }

    override val pluginId: String
        get() = "io.github.tabilzad.ktor-docs-plugin-gradle"

    override val pluginOptions: Collection<AbstractCliOption>
        get() = listOf(
            isEnabled,
            buildPath,
            saveInBuild,
            modulePath,
            titleOption,
            descOption,
            versionOption,
            pathOption,
            requestSchema,
            hideTransientFields,
            hidePrivateAndInternalFields,
            formatOption
        )


    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        when (option) {
            isEnabled -> configuration.put(ARG_ENABLED, value.toBooleanStrictOrNull() ?: true)

            buildPath -> configuration.put(ARG_BUILD_PATH, value)

            saveInBuild -> configuration.put(ARG_SAVE_IN_BUILD, value.toBooleanStrictOrNull() ?: false)

            modulePath -> configuration.put(ARG_MODULE_PATH, value)

            titleOption -> configuration.put(ARG_TITLE, value)

            descOption -> configuration.put(ARG_DESCR, value)

            versionOption -> configuration.put(ARG_VER, value)

            pathOption -> configuration.put(ARG_PATH, value)

            formatOption -> configuration.put(ARG_FORMAT, value)

            requestSchema -> configuration.put(ARG_REQUEST_FEATURE, value.toBooleanStrictOrNull() ?: true)

            hideTransientFields -> configuration.put(ARG_HIDE_TRANSIENTS, value.toBooleanStrictOrNull() ?: true)

            hidePrivateAndInternalFields -> configuration.put(ARG_HIDE_PRIVATE, value.toBooleanStrictOrNull() ?: true)

            else -> throw IllegalArgumentException("Unexpected config option ${option.optionName}")
        }
    }
}