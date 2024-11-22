package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

@GenerateOpenApi
fun Route.returnRouteModule() {
    route("/root") {
        routeExpression()
        routeBlockBodyWithReturn()
        routeNoReturnType()
    }
}

fun Route.routeExpression(): Route = route("/expressBody") {
    get {}
}

fun Route.routeBlockBodyWithReturn(): Route {
    return route("/blockBodyWithReturn") {
        get {}
    }
}

fun Route.routeNoReturnType() = route("/noReturnType") {
    routeReturningGet()
}

fun Route.routeReturningGet() = get("returnGet") {}
