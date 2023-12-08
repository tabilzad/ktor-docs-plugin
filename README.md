# Ktor Swagger Plugin

This plugin implements a plug and play solution to support OpenAPI (Swagger) for Ktor server with minimal effort.
Annotate your route(s) definition with `@KtorDocs` and `openapi.json` will be generated at build time.

Take a look at `use-plugin` module to reference the plugin gradle setup and `StabilityTests` to see all the supported features.

## How to apply the plugin

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath ("io.github.tabilzad:ktor-docs-plugin-gradle:0.3.0-alpha")
    }
}

apply plugin: 'io.github.tabilzad.ktor-docs-plugin-gradle'

swagger {
    title = "Ktor Server"
    description = "Ktor Server Description"
    version = "1.0"
    // generates request body schemas
    requestFeature = true 
}
```

## How to use the plugin

### Generating endpoint specifications
Annotate the specific route definitions you want the OpenAPI specification to be generated for. 
```kotlin

@KtorDocs
fun Route.ordersRouting() {
    route("/v1") {
        post("/order1") {
            /*...*/
        }
    }
}

```

You could also annotate the entire `Application` module with multiple/nested route definitions. The plugin will recursively visit each `Route`. extension and generate its documentation. 
```kotlin

@KtorDocs
fun Application.ordersModule() {
    routing {
        routeOne()
        routeTwo()
    }
}

fun Route.routeOne() {
    route("/v1") { /*...*/ }
}

fun Route.routeTwo() {
    route("/v2") { /*...*/ }
    routeThree()
}

```
### Endpoint and field descriptions

Annotate the HTTP methods of the route with `@KtorDescription`

```kotlin
import io.github.tabilzad.ktor.KtorDocs

data class RequestSample(
    @KtorDescription("this is a string")
    val string: String,
    val int: Int,
    val double: Double,
    val obj: More,
    val collection: List<More>,
)

data class More(
    val nested: List<List<String>>
)

@KtorDocs
fun Route.ordersRouting() {
    route("/v1") {
        @KtorDescription(
            summary= "Orders Endpoint",
            description= "This endpoint will provide a list of all orders"
        )
        post("/create") {
            call.receive<RequestSample>()
        }
 
        route("/orders") {
            @KtorDescription(
                summary= "Orders Endpoint",
                description= "This endpoint will provide a list of all orders"
            )
            get {
                /*...*/
            }
        }
    }
}
```

Sample Specification

![image](https://github.com/tabilzad/ktor-docs-plugin/assets/16094286/84f94b0c-8064-4314-ac27-afc422a7f5a0)



