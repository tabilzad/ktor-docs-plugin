package sources

import sources.annotations.KtorDescription
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs

interface MyInterface {
    val abstractField1: String,
    val abstractField2: Int,
    val fieldWithGetter: String
        get() = abstractField1 + abstractField2.toString()
}

data class SomeRequest(
    val concreteField: String
) : MyInterface

@KtorDocs
fun Application.responseBody() {
    routing {
        route("/v3") {
            post("/postBodyRequestSimple") {
                call.receive<SomeRequest>()
            }
        }
    }
}