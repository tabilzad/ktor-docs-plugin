package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.Tag
import io.ktor.server.application.*
import io.ktor.server.routing.*

@GenerateOpenApi
@Tag(["AppLevel"])
fun Application.tagsAreAppliedToApplicationModule() {
    routing {
        route("/v1") {
            get("/getRequest1") {

            }
        }
    }
}

@GenerateOpenApi
@Tag(["RouteLevel"])
fun Route.tagsAreAppliedToRoute() {
    route("/v2") {
        get("/getRequest2") {

        }
    }
}
