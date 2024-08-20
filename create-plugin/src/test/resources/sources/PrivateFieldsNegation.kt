package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import sources.requests.PrivateBodyRequest

@GenerateOpenApi
fun Application.bodyWithPrivateFieldsNegation() {
    routing {
        route("/v1"){
            post("/bodyWithPrivateFields") {
                call.receive<PrivateBodyRequest>()
            }
        }
    }
}
