plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}
apply("../gradle/signing.gradle.kts")
dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.10")
    implementation("io.arrow-kt:arrow-meta:1.6.1-alpha.5")
    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    testImplementation("io.arrow-kt:arrow-meta-test:1.6.1-SNAPSHOT")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.9")
    testImplementation("junit:junit:4.13.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
}

java {
    withSourcesJar()
    withJavadocJar()
}
signing {
    sign(publishing.publications)
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
                name.set("Ktor Swagger Plugin")
                description.set("Provides Swagger support to Ktor")
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