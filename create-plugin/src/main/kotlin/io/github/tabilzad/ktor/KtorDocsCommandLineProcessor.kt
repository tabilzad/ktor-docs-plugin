package io.github.tabilzad.ktor

import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_DESCR
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_PATH
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_REQUEST_FEATURE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_TITLE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.ARG_VER
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_DESCR
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_PATH
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_REQUEST_BODY
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_TITLE
import io.github.tabilzad.ktor.SwaggerConfigurationKeys.OPTION_VER
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object SwaggerConfigurationKeys {
    const val OPTION_TITLE = "title"
    const val OPTION_DESCR = "description"
    const val OPTION_VER = "version"
    const val OPTION_PATH = "jsonPath"
    const val OPTION_REQUEST_BODY = "requestFeature"

    val ARG_TITLE = CompilerConfigurationKey.create<String>(OPTION_TITLE)
    val ARG_DESCR = CompilerConfigurationKey.create<String>(OPTION_DESCR)
    val ARG_VER = CompilerConfigurationKey.create<String>(OPTION_VER)
    val ARG_PATH = CompilerConfigurationKey.create<String>(OPTION_PATH)
    val ARG_REQUEST_FEATURE = CompilerConfigurationKey.create<String>(OPTION_REQUEST_BODY)
}

class KtorDocsCommandLineProcessor : CommandLineProcessor {
    companion object {
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
            true
        )
        private val pathOption = CliOption(
            OPTION_PATH,
            "Server version",
            "Ktor Server version",
            false
        )
        private val requestSchema = CliOption(
            OPTION_REQUEST_BODY,
            "Request Schema",
            "Enable request body definitions",
            false
        )
    }

    override val pluginId: String
        get() = "io.github.tabilzad.ktor-docs-plugin-gradle"

    override val pluginOptions: Collection<AbstractCliOption>
        get() = listOf(titleOption, descOption, versionOption, pathOption, requestSchema)

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        when (option) {
            titleOption -> configuration.put(
                ARG_TITLE, value
            )
            descOption -> configuration.put(
                ARG_DESCR, value
            )
            versionOption -> configuration.put(
                ARG_VER, value
            )
            pathOption -> configuration.put(
                ARG_PATH, value
            )
            requestSchema -> configuration.put(
                ARG_REQUEST_FEATURE, value
            )
            else -> throw IllegalArgumentException("Unexpected config option ${option.optionName}")
        }
    }
}