# Open API (Swagger) Plugin for Ktor

This plugin implements a plug and play solution to support OpenAPI (Swagger) for Ktor server with minimal effort.
Annotate your route(s) definition with `@KtorDocs` and `openapi.yaml` will be generated at build time.

Take a look at `use-plugin` module to reference the plugin setup and `StabilityTests` to see all supported
features.

## How to apply the plugin

```groovy
plugins {
    id("io.github.tabilzad.ktor-docs-plugin-gradle") version "0.5.2-alpha"
}

swagger {

    documentation {
        docsTitle = "Ktor Server Title"
        docsDescription = "Ktor Server Description"
        docsVersion = "1.0"
        generateRequestSchemas = true
        hideTransientFields = true
        hidePrivateAndInternalFields = true
    }

    pluginOptions {
        enabled = true // true by default
        saveInBuild = true// false by default
        format = "yaml" // or json
    }
}
```

## Supported Features

| Feature                      | isSupported | type      |
|------------------------------|-------------|-----------|
| Path/Endpoint definitions    | ✅           | Automatic |
| Request Schemas              | ✅           | Automatic |
| Response Schemas             | ✅           | Explicit  |
| Endpoint/Scheme Descriptions | ✅           | Explicit  |
| Endpoint Tagging             | ✅           | Explicit  |

## Plugin Configuration

| Option                                       | Default Value                             | Explanation                                                                                 |
|----------------------------------------------|-------------------------------------------|---------------------------------------------------------------------------------------------|
| `documentation.docsTitle`                    | `"Open API Specification"`                | Title for the API specification that is generated                                           |
| `documentation.docsDescription`              | `"Generated using Ktor Docs Plugin"`      | A brief description for the generated API specification                                     |
| `documentation.docsVersion`                  | `"1.0.0"`                                 | Specifies the version for the generated API specification                                   |
| `documentation.generateRequestSchemas`       | `true`                                    | Determines if request body schemas should <br/>be automatically resolved and included       |
| `documentation.hideTransientFields`          | `true`                                    | Controls whether fields marked with `@Transient` <br/> are omitted in schema outputs        |
| `documentation.hidePrivateAndInternalFields` | `true`                                    | Opts to exclude fields labeled as `private` or `internal` from schema outputs               |
| `pluginOptions.enabled`                      | `true`                                    | Enable/Disables the plugin                                                                  |
| `pluginOptions.saveInBuild`                  | `false`                                   | Decides if the generated specification file should <br/> be saved in the `build/` directory |
| `pluginOptions.format`                       | `yaml`                                    | The chosen format for the OpenAPI specification <br/>(options: json/yaml)                   |
| `pluginOptions.filePath`                     | `$modulePath/src/main/resources/openapi/` | The designated absolute path for saving <br/> the generated specification file              |

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

You could also annotate the entire `Application` module with multiple/nested route definitions. The plugin will
recursively visit each `Route`. extension and generate its documentation.

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

Annotate the HTTP methods or class fields with `@KtorDescription`.

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
            summary = "Create Order",
            description = "This endpoint will create an order",
        )
        post("/create") {
            call.receive<RequestSample>()
        }

        route("/orders") {
            @KtorDescription(
                summary = "All Orders",
                description = "This endpoint will return a list of all orders"
            )
            get {
                /*...*/
            }
        }
    }
}
```

### Responses
Defining response schemas and their corresponding HTTP status codes are done via `@KtorRespons` annotation on an endpoint. 
```kotlin
@KtorDocs(["Orders"])
fun Route.ordersRouting() {
    route("/v1") {
        @KtorResponds(
               [
                   ResponseEntry("200", Order::class, description = "Created order"),
                   ResponseEntry("400", ErrorResponseSample::class, description = "Invalid order payload")
               ]
        )
        post("/create") { /*...*/ }
        @KtorResponds([ResponseEntry("200", Order::class, isCollection=true, description = "All orders")])
        get("/orders") { /*...*/ }
    }
}
```

### Tagging

Using tags enables the categorization of individual endpoints into designated groups.
When tags are defined within the `@KtorDocs` annotation, these tags apply to every endpoint contained within it.

```kotlin
@KtorDocs(["Orders"])
fun Route.ordersRouting() {
    route("/v1") {
        post("/create") { /*...*/ }
        get("/orders") { /*...*/ }
    }
    route("/v2") {
        post("/create") { /*...*/ }
        get("/orders") { /*...*/ }
    }
}
```
On the other hand, if the tags are specified within the `@KtorDescription` annotation, they are associated exclusively with that particular endpoint.

```kotlin
@KtorDocs(["Orders"])
fun Route.ordersRouting() {
    route("/v1") {
        @KtorDescription(tags = ["Order Operations"])
        post("/order") { /*...*/ }
        @KtorDescription(tags = ["Cart Operations"])
        get("/cart") { /*...*/ }
    }
}
```

## Planned Features

* Automatic Response resolution
* Support for polymorphic types
* Option for an automatic tag resolution from module/route function declaration
* Introduce a separate `@KtorTag` annotation that is applicable to module/route/endpoint
* Tag descriptions

## Sample Specification

![sample](https://github.com/tabilzad/ktor-docs-plugin/assets/16094286/6d0b0a6a-5925-4f52-ad23-11b1c44b43a1)




