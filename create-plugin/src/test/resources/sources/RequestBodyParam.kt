package sources

import io.github.tabilzad.ktor.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.routing.*
import sources.requests.SimpleRequest

@GenerateOpenApi
fun Application.responseBodyParam() {
    routing {
        route("/v1") {
            post<SimpleRequest>("/post") {
                println()
            }
            post<SimpleRequest>("/otherPost") {
            }
        }
        route("/v2") {
            route("/v2a") {
                patch<SimpleRequest>("/patch") {
                    println()
                }
                post<String>("/primitive") {

                }
            }
        }
    }
}
