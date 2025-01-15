
import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish.base)
}

dependencies {
    compileOnly(libs.bundles.kotlinGradle)
}

gradlePlugin {
    plugins {
        create("gradlePlugin") {
            id = "io.github.tabilzad.ktor-docs-plugin-gradle"
            displayName = "Ktor Open API specification Generator"
            description = "Open API (Swagger) specification Generator for Ktor"
            implementationClass = "io.github.tabilzad.ktor.KtorMetaPlugin"
        }
    }
}

val versionDirectory = "${layout.buildDirectory.asFile.get().path}/generated/version/"

sourceSets {
    main {
        java.srcDir(versionDirectory)
    }
}

tasks {
    register("pluginVersion") {
        mustRunAfter("sourcesJar")
        val outputDir = file(versionDirectory)
        inputs.property("version", project.version)
        outputs.dir(outputDir)
        doLast {
            val versionFile = file("$outputDir/io/github/tabilzad/ktor/version.kt")
            versionFile.parentFile.mkdirs()
            versionFile.writeText(
                """ | // Generated file. Do not edit!
                | package io.github.tabilzad.ktor
                |            
                | internal const val ktorDocsVersion = "${project.version}" 
                """.trimMargin("| ")
            )
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("pluginVersion")
}

mavenPublishing {
    configure(GradlePlugin(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = true
    ))
    publishToMavenCentral(SonatypeHost.S01, automaticRelease = false)
    coordinates(
        groupId = project.group.toString(),
        artifactId = project.properties["POM_ARTIFACT_ID"].toString(),
        version = project.version.toString()
    )
    signAllPublications()
    pom {
        name.set("Ktor Swagger Gradle Plugin")
        description.set("Provides Gradle bridge for ktor-docs-plugin")
        url.set("https://github.com/tabilzad/ktor-docs-plugin")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("tabilzad")
                email.set("tim.abilzade@gmail.com")
                url.set("https://github.com/tabilzad")
            }
        }
        scm {
            url.set("https://github.com/tabilzad/ktor-docs-plugin")
        }
    }
}

