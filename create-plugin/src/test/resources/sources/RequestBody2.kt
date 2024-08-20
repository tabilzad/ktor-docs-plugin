package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.Tag
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import sources.requests.SimpleRequest

@GenerateOpenApi
@Tag(["module1"])
fun Application.moduleWithBody() {
    routing {
        requestBodyTest2()
    }
}

@GenerateOpenApi
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
