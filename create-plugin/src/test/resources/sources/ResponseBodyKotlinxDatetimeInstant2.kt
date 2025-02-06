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


@GenerateOpenApi
fun Application.responseBodySample() {
    routing {
        @KtorResponds(
            mapping = [
                ResponseEntry("200", kotlinx.datetime.Instant::class, description = "Success"),
            ]
        )
        get("temp") {
            call.respond(Clock.System.now())
        }
    }
}