package sources

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs
import sources.requests.PolyRequest
import sources.requests.SimplePolymorphicRequest

@KtorDocs
fun Application.polymorphicTestRoute() {
    routing {
        post("/polymorphic") {
            call.receive<SimplePolymorphicRequest>()
        }
    }
}
