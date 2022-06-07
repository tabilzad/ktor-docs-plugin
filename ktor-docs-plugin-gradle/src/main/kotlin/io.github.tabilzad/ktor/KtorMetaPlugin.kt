package io.github.tabilzad.ktor

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.*

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
        target.extensions.create("swagger", KtorDocsExtension::class.java)
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val ex = project.extensions.findByType(KtorDocsExtension::class.java) ?: KtorDocsExtension()
        kotlinCompilation.dependencies {
            compileOnly("io.github.tabilzad:ktor-docs-plugin:$ktorDocsVersion")
        }

        return project.provider {
            listOf(
                SubpluginOption(
                    key = "title",
                    value = ex.docsTitle
                ), SubpluginOption(
                    key = "description",
                    value = ex.docsDescription
                ),
                SubpluginOption(
                    key = "version",
                    value = ex.docsVersion
                )
            ).apply {
                ex.swaggerJsonPath?.let {
                    plus(
                        SubpluginOption(
                            key = "jsonPath",
                            value = it
                        )
                    )
                }
            }
        }
    }
}

