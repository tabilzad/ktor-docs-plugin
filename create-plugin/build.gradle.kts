plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}
apply("../gradle/signing.gradle.kts")
repositories {
    mavenCentral()
}
dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.10")

    implementation("io.arrow-kt:arrow-meta:1.6.2") // test debug
    implementation("com.squareup.moshi:moshi:1.14.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")


    testImplementation ("io.arrow-kt:arrow-meta-test:1.6.2")
    testImplementation ("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    testImplementation ("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.5.0")
    testImplementation ("io.ktor:ktor:2.2.4")
    testImplementation ("io.ktor:ktor-server-netty:2.2.4")

    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
java {
    withSourcesJar()
    withJavadocJar()
}
tasks.test {
    useJUnitPlatform()
}
signing {
    val signingKey = extra["signing.keyId"].toString()
    val signingPassword = extra["signing.password"].toString()
    useInMemoryPgpKeys(signingKey, signingPassword)
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