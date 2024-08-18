package sources

import io.github.tabilzad.ktor.GenerateOpenApi
import io.github.tabilzad.ktor.KtorFieldDescription
import sources.annotations.KtorDescription
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*


@Resource("/articles")
class DescribedArticles(
    @KtorFieldDescription(
        description = "this will sort the articles"
    )
    val sort: String? = "new"
) {
    @Resource("new")
    class New(val parent: DescribedArticles = DescribedArticles())

    @Resource("{id}")
    class Id(val parent: DescribedArticles = DescribedArticles(),
             @KtorFieldDescription(
                 description = "Id of the article"
             )
             val id: Long) {
        @Resource("edit")
        class Edit(val parent: Id)
    }
}

@GenerateOpenApi
fun Application.describedArticles() {
    routing {
        get<DescribedArticles> { article ->
            // Get all articles ...
            call.respondText("List of articles sorted starting from ${article.sort}")
        }
        @KtorDescription(
            summary = "Creates new article",
            description = "Creates new articles (description)"
        )
        get<DescribedArticles.New> {
            // Show a page with fields for creating a new article ...
            call.respondText("Create a new article")
        }
        @KtorDescription("Saves an article")
        post<DescribedArticles> {
            // Save an article ...
            call.respondText("An article is saved", status = HttpStatusCode.Created)
        }
        @KtorDescription("Get article by id")
        get<DescribedArticles.Id> { article ->
            // Show an article with id ${article.id} ...
            call.respondText("An article with id ${article.id}", status = HttpStatusCode.OK)
        }
        @KtorDescription("Get Edit article by id")
        get<DescribedArticles.Id.Edit> { article ->
            // Show a page with fields for editing an article ...
            call.respondText("Edit an article with id ${article.parent.id}", status = HttpStatusCode.OK)
        }
    }
}
