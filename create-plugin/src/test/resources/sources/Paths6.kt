package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.routing.*

object MyPaths {
    val orderPath = "/order"
    val getEndpoint = "/get"
}

const val globalPath = "/global"
val combined = globalPath + "/somethingElse"
val combined2 = combined + MyPaths.orderPath

enum class MyEnumPaths(val path: String) {
    ORDER("/order")
}

@GenerateOpenApi
fun Application.pathsFromVariables() {
    routing {
        route("/v1") {
            route(MyPaths.orderPath) {
                post(MyPaths.getEndpoint) {

                }
            }

            route("$globalPath/v2") {
                get(MyPaths.orderPath + MyPaths.getEndpoint) { }
            }

            route("globalPath" + "/v3") {
                get(combined2) { }
            }

            route(MyEnumPaths.ORDER.path) {
                get {

                }

                get("/another" + MyEnumPaths.ORDER.path) {

                }

                get("/yetAnother${MyEnumPaths.ORDER.path}") {

                }
            }
        }
    }
}
