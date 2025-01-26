package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorResponds
import io.github.tabilzad.ktor.annotations.ResponseEntry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import sources.requests.PrivateBodyRequest
import sources.requests.SimpleRequest

@GenerateOpenApi
fun Application.responseBodySample() {
    routing {
        route("/v1") {
            @KtorResponds(
                mapping = [
                    ResponseEntry("200", Nothing::class, description = "Success"),
                    ResponseEntry("500", PrivateBodyRequest::class, description = "Failure")
                ]
            )
            post("/noResponseBody") {
                call.receive<SimpleRequest>()
            }

            @KtorResponds([ResponseEntry("204", Nothing::class)])
            post("/implicitArgNames") {
                call.receive<SimpleRequest>()
            }
        }
    }
}
