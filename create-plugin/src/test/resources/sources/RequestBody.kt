package sources

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs
import sources.requests.ComplexRequest
import sources.requests.NestedRequest
import sources.requests.SimpleRequest

@KtorDocs
fun Application.requestBodyTest() {
    routing {
        route("/v1"){
            post("/postBodyRequestSimple") {
                call.receive<SimpleRequest>()
            }
            post("/postBodyRequestWithVariableDeclaration") {
               val request = call.receive<SimpleRequest>()
            }
            post("/postBodyRequestWithLambda") {
                call.receive<SimpleRequest>().let{
                    println(it)
                }
            }
        }

        route("/v2"){
            post("/postBodyNestedRequest") {
                call.receive<NestedRequest>()
            }

            post("/postBodyComplexRequest") {
                call.receive<ComplexRequest>()
            }
        }
    }
}