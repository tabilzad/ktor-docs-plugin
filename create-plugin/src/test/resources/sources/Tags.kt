package sources

import io.ktor.server.application.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs

@KtorDocs(["AppLevel"])
fun Application.tagsAreAppliedToApplicationModule() {
    routing {
        route("/v1") {
            get("/getRequest1") {

            }
        }
    }
}

@KtorDocs(["RouteLevel"])
fun Route.tagsAreAppliedToRoute() {
    route("/v2") {
        get("/getRequest2") {

        }
    }
}
