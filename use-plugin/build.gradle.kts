plugins {
    kotlin("jvm")
    id("io.github.tabilzad.ktor-docs-plugin-gradle") version "0.5.1-alpha"
}

swagger {

    documentation {
        docsTitle = "Ktor Server Title"
        docsDescription = "Ktor Server Description"
        docsVersion = "1.0"
        generateRequestSchemas = true
        hideTransientFields = true
        hidePrivateAndInternalFields = true
    }

    pluginOptions {
        enabled = true // true by default
        saveInBuild = true// false by default
        format = "yaml" // or json
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    implementation("io.ktor:ktor:2.2.4")
    implementation("io.ktor:ktor-server-netty:2.2.4")
}
