package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

/**
 * This class contains fields with kdocs.
 */
data class KDocsClass(
    /**
     * This field is called [kdocsConstructorParameter].
     * This is another line with
     * * This is another line with extra *
     * * This \is another \*line with extra *
     */
    val kdocsConstructorParameter: String,
    val noKdocs: String,
) {

    /**
     * This field is called [kdocsConstructorDerivedProperty].
     */
    val kdocsConstructorDerivedProperty = noKdocs + ""

    /**
     * This field is called [kdocsProperty].
     */
    var kdocsProperty: String? = null

    /**
     * This field is called [kdocsLateinitVar]. */
    lateinit var kdocsLateinitVar: String
}

@GenerateOpenApi
fun Application.moduleKdocs() {
    routing {
        route("/v1") {
            post("/action") {
                call.receive<KDocsClass>()
            }
        }
    }
}
