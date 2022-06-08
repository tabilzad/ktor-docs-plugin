# Ktor Swagger Plugin

This plugin implements a plug and play solution to support OpenAPI (Swagger) for Ktor server with mininal effort. Annotate your route(s) definition with `@KtorDocs` and swagger.json will be generated at build time.

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

data class RequestSample(
    val string: String,
    val int: Int,
    val double: Double,
    val `object`: More,
    val collection: List<More>,
)

data class More(
    val nested: List<List<String>>
)

@KtorDocs
fun Route.ordersRouting() {
    route("/v1") {
        post("/order1") {
            call.receive<RequestSample>()
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
Which will produce the following spec

![image](https://user-images.githubusercontent.com/16094286/172687047-b102b577-b252-4e33-af52-7a89a639c131.png)


