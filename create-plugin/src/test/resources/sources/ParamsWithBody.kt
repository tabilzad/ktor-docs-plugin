package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import sources.requests.SimpleRequest

@GenerateOpenApi
fun Application.pathParamsWithBody() {
    routing {
        route("/v1") {

            post("/order/{param1}") {
                call.receive<SimpleRequest>().let {
                    call.parameters["my_param"].let {

                    }
                }
            }
        }
    }
}

