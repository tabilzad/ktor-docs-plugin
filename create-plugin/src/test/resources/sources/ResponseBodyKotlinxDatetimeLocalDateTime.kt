package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorResponds
import io.github.tabilzad.ktor.annotations.ResponseEntry
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class Response(
    val message: LocalDateTime = java.time.LocalDateTime.now().toKotlinLocalDateTime()
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