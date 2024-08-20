package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.routing.*

@GenerateOpenApi
fun Application.annotationIsAppliedToApplication() {
    routing {
        route("/v1") {
            get("/getRequest1") {

            }
        }
    }
}

@GenerateOpenApi
fun Route.annotationIsAppliedToRoute() {
    route("/v2") {
        get("/getRequest2") {

        }
    }
}
