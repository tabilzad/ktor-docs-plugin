package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
//import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import sources.requests.SimpleRequest

@Resource("articles")
class Articles

@GenerateOpenApi
fun Application.responseBodyResource() {
    routing {
        post { article: Articles, request: SimpleRequest ->
            println(request)
            call.respondText("Create a new article")
        }
    }
}