package sources

import io.ktor.server.application.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs

@KtorDocs
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