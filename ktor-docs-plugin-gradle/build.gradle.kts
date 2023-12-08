plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    `maven-publish`
    signing
}

apply("../gradle/signing.gradle.kts")

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.9.10")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
}
//gradlePlugin {
//    plugins {
//        create("simplePlugin") {
//            id = "io.github.tabilzad.ktor-docs-plugin-gradle"
//            displayName = "Ktor Docs Generator"
//            description = "Ktor Docs Generator plugin"
//            implementationClass = "io.github.tabilzad.ktor.KtorMetaPlugin"
//        }
//    }
//}

java {
    withSourcesJar()
    withJavadocJar()
}
signing {
    sign(publishing.publications)
}
val versionDirectory = "$buildDir/generated/version/"
sourceSets {
    main{
        java.srcDir(versionDirectory)
    }
}
tasks {
    register("pluginVersion") {
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

publishing {
    repositories {
        maven {
            name = "sonartype"
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            setUrl { if (version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else releasesRepoUrl }
            credentials {
                username = extra["ossrhUsername"]?.toString()
                password = extra["ossrhPassword"]?.toString()
            }
        }
    }
    publications {
        create<MavenPublication>("sonartype") {
            groupId = project.group.toString()
            version = project.version.toString()
            artifactId = project.properties["POM_ARTIFACT_ID"].toString()

            from(components["java"])
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
                    }
                }
                scm {
                    url.set("https://github.com/tabilzad/ktor-docs-plugin")
                }
            }
        }
    }
}