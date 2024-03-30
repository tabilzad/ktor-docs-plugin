package sources

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs
import sources.requests.SimpleRequest

@KtorDocs
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

