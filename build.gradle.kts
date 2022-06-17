import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    `maven-publish`
    `java-library`
    signing
}

buildscript {
    apply("gradle/dependencies.gradle")
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        google()
    }
    dependencies {
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
        classpath ("com.android.tools.build:gradle:7.0.0")
        classpath ("com.vanniktech:gradle-maven-publish-plugin:0.15.0")
        classpath ("org.jetbrains.dokka:dokka-gradle-plugin:1.5.30")
        classpath ("io.github.tabilzad:ktor-docs-plugin-gradle:0.1.66-alpha")
    }
}

subprojects {
    group = project.properties["GROUP"].toString()
    version =  project.properties["VERSION_NAME"].toString()
    repositories {
        mavenCentral()
        gradlePluginPortal()
        //maven { url ("https://jitpack.io") }
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

dependencies {
    implementation(kotlin("stdlib"))
}
