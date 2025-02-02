package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import sources.requests.SimpleRequest

@Resource("articles")
class Articles

@GenerateOpenApi
fun Application.responseBodyResource() {
    routing {
        post<Articles> {
            call.receive<SimpleRequest>()
        }
    }
}