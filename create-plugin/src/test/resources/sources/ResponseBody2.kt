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

const val externalDesc = "external"

@KtorDocs
fun Application.responseBody() {
    routing {
        route("/v5") {
            @KtorResponds(
                mapping = [
                    ResponseEntry(
                        "200", SimpleRequest::class, description = "line0" + "line1"
                                + "line3"
                    ),
                    ResponseEntry("500", PrivateBodyRequest::class, description = externalDesc)
                ]
            )
            post("/postBodyRequestSimple") {
                call.receive<SimpleRequest>()
            }
            @KtorResponds(
                mapping = [
                    ResponseEntry(
                        "200", SimpleRequest::class, isCollection=true, "line0"
                                + "line1"
                                + "line3"
                    )
                ]
            )
            post("/implicitField") {
                call.receive<SimpleRequest>()
            }

            @KtorResponds([ResponseEntry("200", SimpleRequest::class, isCollection=true)])
            get("/implicitArgs") {

            }
        }
    }
}