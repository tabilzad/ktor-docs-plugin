package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorResponds
import io.github.tabilzad.ktor.annotations.ResponseEntry
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
data class Response(
    val message: Instant = Clock.System.now()
)

@GenerateOpenApi
fun Application.responseBodySample() {
    routing {
        @KtorResponds(
            mapping = [
                ResponseEntry("200", Response::class, description = "Success"),
            ]
        )
        get("temp") {
            call.respond(Response())
        }
    }
}