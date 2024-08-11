package sources

import io.github.tabilzad.ktor.GenerateOpenApi
import sources.annotations.KtorDescription
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs

data class GenericType<T>(
    val status: T,
    val other: String
)

data class SomeGenericRequest(
    val generic: GenericType<Int>
)

@GenerateOpenApi
fun Application.responseBody() {
    routing {
        route("/v3") {
            post("/postGenericRequest") {
                call.receive<SomeGenericRequest>()
            }
        }
    }
}