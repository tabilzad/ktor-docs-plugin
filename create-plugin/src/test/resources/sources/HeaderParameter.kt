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
            get("/order") {
                val type = call.request.header(HttpHeaders.ContentType)
                call.request.headers["param_part1_${headerParam}"].let{
                    println(it)
                }
            }

            get("/order1") {
                val type = call.request.header("Manually-Added-Header")
                call.request.headers["param_part1_${headerParam}"].let{
                    println(it)
                }
            }
        }
    }
}
