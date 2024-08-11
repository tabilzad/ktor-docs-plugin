pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url="https://oss.sonatype.org/content/repositories/snapshots/")
        google()
    }
}

rootProject.name = "ktor-docs"
include("ktor-docs-plugin-gradle")
include("create-plugin")