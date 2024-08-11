package sources

import io.github.tabilzad.ktor.Tag
import io.ktor.server.application.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs

@KtorDocs
@Tag(["AppLevel"])
fun Application.tagsAreAppliedToApplicationModule() {
    routing {
        route("/v1") {
            get("/getRequest1") {

            }
        }
    }
}

@KtorDocs
@Tag(["RouteLevel"])
fun Route.tagsAreAppliedToRoute() {
    route("/v2") {
        get("/getRequest2") {

        }
    }
}
