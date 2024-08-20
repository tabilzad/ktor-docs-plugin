package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.routing.*

@GenerateOpenApi
fun Application.pathParametersTest() {
    routing {
        route("/v1") {
            get("/order/{order_id}") {

            }
            // swagger does not support optional path parameters
            get("/orders/{optional_order_id?}") {

            }
        }
    }
}
