package sources

import io.github.tabilzad.ktor.Tag
import io.github.tabilzad.ktor.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.routing.*


@GenerateOpenApi
@Tag(["Module"])
fun Application.module1() {
    routing {
        subModule1()
        subModule2()
    }
}

fun Route.subModule1() {
    route("/submodule1") {
        get("/sub1") {

        }
    }
}

@Tag(["SubModule"])
fun Route.subModule2() {
    route("/submodule2") {
        get("/sub2") {

        }
    }
}
