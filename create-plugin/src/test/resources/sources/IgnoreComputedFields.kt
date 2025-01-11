package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

data class ClassWithComputedFields(
    val a: String,
    val b: String
) {
    val noSetter: Int
        get() {
            return a.length + b.length
        }

    val noSetter2 get() = false
}

@GenerateOpenApi
fun Application.computedFields() {
    routing {
        route("/v1") {
            post("/postWithComputedFields") {
                call.receive<ClassWithComputedFields>()
            }
        }
    }
}
