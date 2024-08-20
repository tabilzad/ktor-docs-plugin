package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import sources.requests.SimpleRequest

@Resource("/articles")
class Articles(val sort: String? = "new") {
    @Resource("/new")
    class New(val parent: Articles = Articles()){
        @Resource("{id}")
        class Id(val parent: New = New(), val id: Long)
    }

    @Resource("/remove")
    class Remove(val parent: Articles = Articles())
}

@GenerateOpenApi
fun Application.responseBodyResource() {
    routing {
        get<Articles> { article ->
            // Get all articles ...
            call.respondText("List of articles sorted starting from ${article.sort}")
        }
        post<Articles.New> {
            // Show a page with fields for creating a new article ...
            call.respondText("Create a new article")
        }

        get<Articles.New.Id> {
            // Show a page with fields for creating a new article ...
            call.respondText("Create a new article")
        }
        post<Articles.Remove> {
            // Show a page with fields for creating a new article ...
            call.respondText("Create a new article")
        }

        get("/noResources") {

        }

        post("/noResources") {
            call.receive<SimpleRequest>()
        }

        post<SimpleRequest>("/regularPost") {

        }

    }
}
