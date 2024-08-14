# Open API (Swagger) Plugin for Ktor

This plugin implements a plug and play solution for generating OpenAPI (Swagger) specification for your Ktor server with minimal effort - no need to modify your existing code, no special DSL wrappers etc.
Just annotate your route(s) definitions with `@GenerateOpenApi` and `openapi.yaml` will be generated at build time.

## How to apply the plugin

```groovy
plugins {
    id("io.github.tabilzad.ktor-docs-plugin-gradle") version "0.6.0-alpha"
}

swagger {

    documentation {
        docsTitle = "Ktor Server Title"
        docsDescription = "Ktor Server Description"
        docsVersion = "1.0"
        generateRequestSchemas = true
        hideTransientFields = true
        hidePrivateAndInternalFields = true
        deriveFieldRequirementFromTypeNullability = true
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

### Documentation options

| Option                         | Default Value                             | Explanation                                                                                 |
|--------------------------------|-------------------------------------------|---------------------------------------------------------------------------------------------|
| `docsTitle`                    | `"Open API Specification"`                | Title for the API specification that is generated                                           |
| `docsDescription`              | `"Generated using Ktor Docs Plugin"`      | A brief description for the generated API specification                                     |
| `docsVersion`                  | `"1.0.0"`                                 | Specifies the version for the generated API specification                                   |
| `generateRequestSchemas`       | `true`                                    | Determines if request body schemas should <br/>be automatically resolved and included       |
| `hideTransientFields`          | `true`                                    | Controls whether fields marked with `@Transient` <br/> are omitted in schema outputs        |
| `hidePrivateAndInternalFields` | `true`                                    | Opts to exclude fields with `private` or `internal` modifiers from schema outputs           |
| `deriveFieldRequirementFromTypeNullability` | `true`                       | Automatically derive object fields' requirement from its type nullability                   |

### Plugin options
| Option                         | Default Value                             | Explanation                                                                                 |
|--------------------------------|-------------------------------------------|---------------------------------------------------------------------------------------------|
| `enabled`                      | `true`                                    | Enable/Disables the plugin                                                                  |
| `saveInBuild`                  | `false`                                   | Decides if the generated specification file should <br/> be saved in the `build/` directory |
| `format`                       | `yaml`                                    | The chosen format for the OpenAPI specification <br/>(options: json/yaml)                   |
| `filePath`                     | `$modulePath/src/main/resources/openapi/` | The designated absolute path for saving <br/> the generated specification file              |

## How to use the plugin

### Generating endpoint specifications

Annotate the specific route definitions you want the OpenAPI specification to be generated for.

```kotlin

@GenerateOpenApi
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

@GenerateOpenApi
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

Describe endpoints or schema fields.

```kotlin
data class RequestSample(
    @KtorFieldDescription("this is a string")
    val string: String,
    val int: Int,
    val double: Double
)

@GenerateOpenApi
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
            get { /*...*/ }
        }
    }
}
```

### Responses
Defining response schemas and their corresponding HTTP status codes are done via `@KtorResponds` annotation on an endpoint. 

```kotlin
@GenerateOpenApi
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
Tags specified at the parent route will propogate down to all endpoints contained within it.

```kotlin
@Tag(["Orders"])
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
On the other hand, if the tags are specified with `@KtorDescription` or `@Tag` annotation on an endpoint, they are associated exclusively with that particular endpoint.

```kotlin
@GenerateOpenApi
fun Route.ordersRouting() {
    route("/v1") {
        @KtorDescription(tags = ["Order Operations"])
        post("/order") { /*...*/ }
        @Tag(["Cart Operations"])
        get("/cart") { /*...*/ }
    }
}
```

## Planned Features

* Automatic Response resolution
* Support for polymorphic types
* Support ktor resources
* Option for an automatic tag resolution from module/route function declaration
* Tag descriptions

## Sample Specification

![sample](https://github.com/tabilzad/ktor-docs-plugin/assets/16094286/6d0b0a6a-5925-4f52-ad23-11b1c44b43a1)




