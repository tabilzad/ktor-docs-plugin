package sources

import io.ktor.server.application.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs

@KtorDocs
fun Application.routesWithSamePathButDifferentMethod() {
    routing {
        routeOneD()
        routeTwoD()
    }
}


fun Route.routeOneD() {
    route("/v1") {
        get("/getRequest1") {

        }
        post("/getRequest1") {

        }
    }
}

fun Route.routeTwoD() {
    route("/v2") {
        get("/getRequest2") {

        }
        post("/getRequest2") {

        }
    }
    routeThreeD()
}

fun Route.routeThreeD() {
    route("/v3") {
        routeThreeDSub()
        get("/getRequest3") {

        }
        post("/getRequest3") {

        }
    }
}

fun Route.routeThreeDSub() {
    route("/v3a") {
        get("/getRequest3") {

        }
        post("/getRequest3") {

        }
    }
}