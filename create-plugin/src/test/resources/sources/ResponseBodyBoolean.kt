package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorResponds
import io.github.tabilzad.ktor.annotations.ResponseEntry
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@GenerateOpenApi
fun Application.responseBodySample() {
    routing {
        @KtorResponds(
            mapping = [
                ResponseEntry("200", Boolean::class, description = "Success"),
            ]
        )
        post("/booleanResponseBody") {
            call.respond(true)
        }
    }
}