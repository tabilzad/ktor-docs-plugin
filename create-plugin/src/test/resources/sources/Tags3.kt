package sources

import io.github.tabilzad.ktor.annotations.Tag
import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.routing.*

@GenerateOpenApi
@Tag(["MainModule"])
fun Application.module2() {
    routing {
        route("/l1") {
            subModule1()
            route("/l2") {
                get("/l2get") {

                }
            }
        }
    }
}

@Tag(["SubModule"])
fun Route.subModule1() {
    route("/sub_l1") {
        get("/sub_l1_get") {

        }
    }
}
