import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish.base")
}

group = "io.guthub.tabilzad.ktor"

mavenPublishing {
    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Javadoc(),
            sourcesJar = true
        )
    )
    publishToMavenCentral(SonatypeHost.S01, automaticRelease = false)
    coordinates(
        project.group.toString(),
        project.properties["POM_ARTIFACT_ID"].toString(),
        project.version.toString()
    )
    signAllPublications()
    pom {
        name.set("Annotations for Open API specification generator")
        description.set("Annotations for Open API specification generator")
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
