package sources

import io.ktor.server.application.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs

@KtorDocs
fun Application.testRoute() {
    routing {
        route("/v1") {
            route("else") {
                post {

                }
            }
            route("else1") {
                get {

                }
            }
        }
    }
}