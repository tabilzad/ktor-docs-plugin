package sources

import io.ktor.server.application.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs

@KtorDocs
fun Application.testRoute() {
    routing {
        route("/v1"){
            get("/getRequest1") {

            }
        }
        route("/v2"){
            get("/getRequest2") {

            }
        }
        route("/v3"){
            route("/v3a"){
                get("/getElse") {

                }
            }
            route("/v3b"){
                get("/getRequest3a") {

                }
                get("/getRequestElse") {

                }
            }
        }
    }
}