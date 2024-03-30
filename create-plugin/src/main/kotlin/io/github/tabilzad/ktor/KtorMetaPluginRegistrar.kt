package io.github.tabilzad.ktor

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.phases.CompilerContext

open class KtorMetaPluginRegistrar : Meta() {
    override fun intercept(ctx: CompilerContext): List<CliPlugin> = listOf(ktorDocs)
}

val Meta.ktorDocs: CliPlugin
    get() = "ktorDocs" {
        val config = configuration.buildPluginConfiguration()
        meta(swaggerExtensionPhase(config), enableIr())
    }

