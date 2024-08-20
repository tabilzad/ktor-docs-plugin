package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.Tag
import io.ktor.server.application.*
import io.ktor.server.routing.*

@GenerateOpenApi
@Tag(["MainModule"])
fun Application.moduleTags4() {
    routing {

        @Tag(["Tag1"])
        route("/v1") {
            subModuleTag4()

            @Tag(["Tag4"])
            get {

            }
        }
    }
}

fun Route.subModuleTag4() {
    route("/v2") {

        @Tag(["Tag3"])
        get("/sub_l1_get") {

        }
    }
}
