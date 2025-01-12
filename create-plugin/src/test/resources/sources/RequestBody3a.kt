package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

interface MyInterface {
    val abstractField1: String
    val abstractField2: Int
}

data class LocalSampleRequest(
    val concreteField: String, override val abstractField1: String, override val abstractField2: Int
) : MyInterface

@GenerateOpenApi
fun Application.responseBody3a() {
    routing {
        route("/v3") {
            post("/postBodyRequestSimple") {
                call.receive<LocalSampleRequest>()
            }
        }
    }
}
