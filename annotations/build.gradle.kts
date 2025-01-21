import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish.base)
}

mavenPublishing {
    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Javadoc(),
            sourcesJar = true
        )
    )
}
