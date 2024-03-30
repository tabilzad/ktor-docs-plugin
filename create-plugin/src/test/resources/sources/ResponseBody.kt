package sources

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs
import sources.annotations.KtorResponds
import sources.annotations.ResponseEntry
import sources.requests.PrivateBodyRequest
import sources.requests.SimpleRequest

@KtorDocs
fun Application.responseBody() {
    routing {
        route("/v1") {
            @KtorResponds(
                mapping = [
                    ResponseEntry("200", SimpleRequest::class, description = "Success"),
                    ResponseEntry("500", PrivateBodyRequest::class, description = "Failure")
                ]
            )
            post("/postBodyRequestSimple") {
                call.receive<SimpleRequest>()
            }

            @KtorResponds([ResponseEntry("200", SimpleRequest::class)])
            post("/implicitArgNames") {
                call.receive<SimpleRequest>()
            }

            @KtorResponds([ResponseEntry("200", SimpleRequest::class, true)])
            post("/listOfs") {
                call.respond<List<SimpleRequest>>(HttpStatusCode.OK, listOf())
            }
        }
    }
}