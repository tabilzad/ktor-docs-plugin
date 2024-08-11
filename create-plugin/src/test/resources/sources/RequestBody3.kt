package sources

import sources.annotations.KtorDescription
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import sources.annotations.KtorDocs

enum class MyEnumClass{
    ENTRY1,
    ENTRY2
}

data class SomeRequest(
    @KtorDescription("this is an enum field")
    val myEnum: MyEnumClass
)
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