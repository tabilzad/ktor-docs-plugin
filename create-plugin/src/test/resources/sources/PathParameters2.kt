package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import sources.requests.SimpleRequest

@GenerateOpenApi
fun Application.pathParameters2Test() {
    val service = MyService()
    routing {
        route("/v1") {

            post("/order/{param_with_body}"){
                call.parameters["param_with_body"].onPresent { orderId ->
                    call.receive<SimpleRequest>().let { body ->


                    }
                        .let {  }
                        .let {  }
                        .let {  }
                        .let {  }

                }.onAbsentOrBlank {

                }
            }

            post("/complexExpression/{order_id}") {
                service.handleResponse(call)
            }
        }
    }
}

class MyService {
    suspend fun handleResponse(call: ApplicationCall) {
        call.parameters["order_id"].onPresent { orderId ->
            call.receive<SimpleRequest>().let { body ->


            }

        }.onAbsentOrBlank {

        }
    }

}

inline fun String?.onAbsentOrBlank(handle: () -> Unit): String? =
    if (this.isNullOrBlank()) this.also { handle() } else this
inline fun String?.onPresent(handle: (String) -> Unit): String? =
    if (this.isNullOrBlank()) this else this.also(handle)
