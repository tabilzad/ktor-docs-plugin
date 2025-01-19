import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.mavenPublish.base) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.detekt) apply false
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = libs.versions.kotlinVersion.get()))
    }
}

subprojects {
    group = project.properties["GROUP"].toString()
    version =  project.properties["VERSION_NAME"].toString()
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
        javaCompiler.set(
            javaToolchains.compilerFor {
                languageVersion.set(JavaLanguageVersion.of(11))
            }
        )
    }

    apply(plugin = "org.jetbrains.dokka")
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    configure<DetektExtension>{
        autoCorrect = true
        parallel = true
        toolVersion = rootProject.libs.versions.detekt.get()
        source.from(fileTree("src") {
            include("*/java/")
            include("*/kotlin/")
        })
        config.from("$rootDir/config/detekt/detekt.yaml")
        // baseline = file("$rootDir/config/detekt/baseline.xml")
    }
    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
        "detektPlugins"(rootProject.libs.detekt.libraries)
    }
}

dependencies {
    implementation(kotlin("stdlib"))
}
