# Ktor Swagger Plugin

Take a look at `use-plugin` module as reference 

## How to apply plugin

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath ("io.github.tabilzad:ktor-docs-plugin-gradle:0.1.51-alpha")
    }
}

apply plugin: 'io.github.tabilzad.ktor-docs-plugin-gradle'

swagger {
    docsTitle = "Ktor Server"
    docsDescription = "Ktor Server Description"
    docsVersion = "1.0"
    enableRequestSchemas = true // experimental!
}
```

## How to use plugin
```kotlin
import io.github.tabilzad.ktor.KtorDocs

@KtorDocs
fun Route.ordersRouting() {
    route("/v1") {
        post("/order1") {
            call.receive<Sample>()
        }
        route("/order2") {
            route("/customOrder") {
               
            }
            post {
                call.receive<Sample>().let {

                }
            }
        }
        route("/orders") {
            get {
                call.receive<String>().let {

                }
            }
            get("/{order_id}") {
                call.receive<String>().let {

                }
            }
        }
    }
}

```