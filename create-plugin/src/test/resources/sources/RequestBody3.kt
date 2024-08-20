package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorFieldDescription
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

enum class MyEnumClass{
    ENTRY1,
    ENTRY2
}

data class LocalSomeRequest(
    @KtorFieldDescription("this is an enum field")
    val myEnum: MyEnumClass
)
@GenerateOpenApi
fun Application.responseBody3() {
    routing {
        route("/v3") {
            post("/postBodyRequestSimple") {
                call.receive<LocalSomeRequest>()
            }
        }
    }
}
