import java.util.*
import org.gradle.api.Project.*

extra["signing.keyId"] = null
extra["signing.password"] = null
extra["signing.secretKeyRingFile"] = null
extra["ossrhUsername"] = null
extra["ossrhPassword"] = null

val secretPropsFile = project.rootProject.file("secrets.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        extra[name.toString()] = value
    }
} else {
    extra["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    extra["signing.password"] = System.getenv("SIGNING_PASSWORD")
    extra["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    extra["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    extra["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}


fun getExtraString(name: String) = extra[name]?.toString()

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}