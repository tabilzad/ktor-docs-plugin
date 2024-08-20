package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.routing.*

@GenerateOpenApi
fun Application.annotationIsAppliedToWithImportedRoutes() {
    routing {
        routeOne()
        routeTwo()
    }
}


fun Route.routeOne() {
    route("/v1") {
        get("/getRequest1") {

        }
    }
}

fun Route.routeTwo() {
    route("/v2") {
        get("/getRequest2") {

        }
    }
    routeThree()
}

fun Route.routeThree() {
    route("/v3") {
        get("/getRequest3") {

        }
    }
}
