package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.Tag
import io.ktor.server.application.*
import io.ktor.server.routing.*

@GenerateOpenApi
@Tag(["Module"])
fun Application.module1() {
    routing {
        subModule1_tag2()
        subModule2_tag2()
    }
}

fun Route.subModule1_tag2() {
    route("/submodule1") {
        get("/sub1") {

        }
    }
}

@Tag(["SubModule"])
fun Route.subModule2_tag2() {
    route("/submodule2") {
        get("/sub2") {

        }
    }
}
