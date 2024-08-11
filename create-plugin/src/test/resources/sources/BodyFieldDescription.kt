package sources

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import sources.annotations.KtorDescription
import sources.annotations.KtorDocs
import io.github.tabilzad.ktor.*


data class MyDescribedPayload(
    @KtorFieldDescription("this is field 1 (string)", required = true)
    val field1: String,
    @KtorFieldDescription("this is field 2 (int)", required = false)
    val field2: Int,
    @KtorFieldDescription("this is field 3 (int)")
    val field3: NestedObject
)

data class NestedObject(
    @KtorFieldDescription(required = true)
    val subField: List<String>,
    @KtorFieldDescription(required = false)
    val subField2: String
)

@KtorDocs
fun Application.testDescription() {
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
