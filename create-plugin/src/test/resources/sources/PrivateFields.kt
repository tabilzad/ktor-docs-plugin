package sources

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs
import sources.requests.ComplexRequest
import sources.requests.NestedRequest
import sources.requests.PrivateBodyRequest
import sources.requests.SimpleRequest

@KtorDocs
fun Application.bodyWithPrivateFields() {
    routing {
        route("/v1"){
            post("/bodyWithPrivateFields") {
                call.receive<PrivateBodyRequest>()
            }
        }
    }
}