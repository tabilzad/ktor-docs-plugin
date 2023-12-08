package sources

import io.ktor.server.application.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs

@KtorDocs
fun Application.annotationIsAppliedToApplication() {
    routing {
        route("/v1") {
            get("/getRequest1") {

            }
        }
    }
}

@KtorDocs
fun Route.annotationIsAppliedToRoute() {
    route("/v2") {
        get("/getRequest2") {

        }
    }
}