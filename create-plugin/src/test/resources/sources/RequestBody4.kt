package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

data class MyGenericType<T>(
    val status: T,
    val other: String
)

data class MyGenericRequest(
    val generic: MyGenericType<Int>
)

@GenerateOpenApi
fun Application.responseBody4() {
    routing {
        route("/v3") {
            post("/postGenericRequest") {
                call.receive<MyGenericRequest>()
            }
        }
    }
}
