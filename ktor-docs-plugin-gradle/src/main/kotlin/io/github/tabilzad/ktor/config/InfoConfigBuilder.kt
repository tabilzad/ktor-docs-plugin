package io.github.tabilzad.ktor.config

import io.github.tabilzad.ktor.model.Info

class InfoConfigBuilder {
    var title: String? = null
    var description: String? = null
    var version: String? = null

    private var contact: Info.Contact? = null
    private var license: Info.License? = null

    fun contact(builder: Info.Contact.() -> Unit) {
        contact = Info.Contact().apply(builder)
    }

    fun license(builder: Info.License.() -> Unit) {
        license = Info.License().apply(builder)
    }

    fun build(): Info = Info(
        title = title,
        description = description,
        version = version,
        contact = contact,
        license = license
    )
}
