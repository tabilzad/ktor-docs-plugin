package sources

import io.github.tabilzad.ktor.Tag
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import sources.annotations.KtorDescription
import sources.annotations.KtorDocs
import sources.requests.ComplexRequest
import sources.requests.SimpleRequest

@KtorDocs
@Tag(["module1"])
fun Application.module1() {
    routing {
        requestBodyTest2()
    }
}

@KtorDocs
@Tag(["submodule"])
fun Route.requestBodyTest2() {

    val service = MyService2()
    route("/v1") {
        route("/else") {

            put("/putBodyRequestWithLambda") {
                call.receive<SimpleRequest>().let {
                    service.handleResponse(call)
                    println(it)
                }
            }

            patch("/postBodyRequestWithLambda") {
                call.receive<SimpleRequest>().let {
                    service.handleResponse(call)
                    println(it)
                }
            }
        }
    }
}

class MyService2 {
    suspend fun handleResponse(call: ApplicationCall) {

    }

}