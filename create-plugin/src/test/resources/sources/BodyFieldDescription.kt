package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorDescription
import io.github.tabilzad.ktor.annotations.KtorFieldDescription
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.time.Instant


data class MyDescribedPayload(
    @KtorFieldDescription("this is field 1 (string)", required = true)
    val field1: String,
    @KtorFieldDescription("this is field 2 (int)", required = false)
    val field2: Int,
    @KtorFieldDescription("this is field 3 (int)")
    val field3: NestedObject,
    @KtorFieldDescription("this is field 4 (instant)", explicitType = "string", format = "iso 8601")
    val field4: Instant
)

data class NestedObject(
    @KtorFieldDescription(required = true)
    val subField: List<String>,
    @KtorFieldDescription(required = false)
    val subField2: String
)

@GenerateOpenApi
fun Application.testDescriptionBody() {
    routing {
        route("/v1") {
            @KtorDescription(
                "My Summary",
                description = "My Description"
            )
            post("/requestWithDescribedFields") {
                call.receive<MyDescribedPayload>()
            }
        }
    }
}
