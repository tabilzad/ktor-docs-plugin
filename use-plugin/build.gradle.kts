plugins {
    kotlin("jvm")
   // id("io.github.tabilzad.ktor-docs-plugin-gradle") version "0.3.0-alpha"
}

//swagger {
//    title = "Ktor Server Title"
//    description = "Ktor Server Description"
//    version = "1.0"
//    requestFeature = true
//}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url= "https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    implementation("io.ktor:ktor:2.2.4")
    implementation("io.ktor:ktor-server-netty:2.2.4")
}
