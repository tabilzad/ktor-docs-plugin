package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.http.HttpHeaders
import io.ktor.server.application.*
import io.ktor.server.request.header
import io.ktor.server.routing.*
import sources.HeaderStrings.headerParam


object HeaderStrings {
    val headerParam = "my_header_param"
}

@GenerateOpenApi
fun Application.queryParametersTest() {
    routing {
        route("/v1") {
            get("/order9") {
                call.request.header("my_header")
            }

            get("/order10") {
                call.request.header(HeaderStrings.headerParam)
            }

            get("/order11") {
                call.request.header(HttpHeaders.ContentLength)
            }
        }
    }
}
