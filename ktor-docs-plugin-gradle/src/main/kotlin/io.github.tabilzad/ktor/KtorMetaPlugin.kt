package io.github.tabilzad.ktor

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

const val PLUGIN_ID = "io.github.tabilzad.ktor-docs-plugin-gradle"

open class KtorMetaPlugin : KotlinCompilerPluginSupportPlugin {

    override fun getCompilerPluginId() = PLUGIN_ID
    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = "io.github.tabilzad",
            artifactId = "ktor-docs-plugin",
            version = ktorDocsVersion
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return kotlinCompilation.target.project.plugins.hasPlugin(KtorMetaPlugin::class.java)
    }

    override fun apply(target: Project) {
        target.extensions.create("swagger", KtorDocsExtension::class.java).apply {
            documentation = target.extensions.create("documentation", DocumentationOptions::class.java)
            pluginOptions = target.extensions.create("pluginOptions", PluginOptions::class.java)
        }
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val swaggerExtension = project.extensions.findByType(KtorDocsExtension::class.java) ?: KtorDocsExtension()

        val buildDir = project.buildDir.absolutePath
        val modulePath = project.projectDir.absolutePath

        kotlinCompilation.dependencies {
            compileOnly("io.github.tabilzad:ktor-docs-plugin:$ktorDocsVersion")
        }

        val userProvidedOptions = listOf(
            SubpluginOption(
                key = "enabled",
                value = swaggerExtension.pluginOptions.enabled.toString()
            ),
            SubpluginOption(
                key = "saveInBuild",
                value = swaggerExtension.pluginOptions.saveInBuild.toString()
            ),
            SubpluginOption(
                key = "title",
                value = swaggerExtension.documentation.docsTitle
            ), SubpluginOption(
                key = "description",
                value = swaggerExtension.documentation.docsDescription
            ),
            SubpluginOption(
                key = "version",
                value = swaggerExtension.documentation.docsVersion
            ),
            SubpluginOption(
                key = "generateRequestSchemas",
                value = swaggerExtension.documentation.generateRequestSchemas.toString()
            ),
            SubpluginOption(
                key = "hideTransientFields",
                value = swaggerExtension.documentation.hideTransientFields.toString()
            ),
            SubpluginOption(
                key = "hidePrivateAndInternalFields",
                value = swaggerExtension.documentation.hidePrivateAndInternalFields.toString()
            ),
            SubpluginOption(
                key = "format",
                value = swaggerExtension.pluginOptions.format
            )
        ).apply {
            swaggerExtension.pluginOptions.filePath?.let {
                plus(
                    SubpluginOption(
                        key = "filePath",
                        value = it
                    )
                )
            }
        }

        val gradleProvidedOptions = listOf(
            SubpluginOption(
                key = "buildPath",
                value = buildDir
            ),
            SubpluginOption(
                key = "modulePath",
                value = modulePath
            )
        )
        return project.provider { gradleProvidedOptions + userProvidedOptions }
    }
}

