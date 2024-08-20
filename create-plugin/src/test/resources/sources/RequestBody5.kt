package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

data class GenericType<T>(
    val status: T,
    val other: String
)

data class SomeGenericRequest(
    val generic: List<List<GenericType<Int>>>
)

@GenerateOpenApi
fun Application.responseBody5() {
    routing {
        route("/v3") {
            post("/postGenericRequest") {
                call.receive<SomeGenericRequest>()
            }
        }
    }
}
