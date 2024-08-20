package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.routing.*

@GenerateOpenApi
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
