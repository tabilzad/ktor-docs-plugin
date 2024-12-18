package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

sealed interface TopSealedInterface {
    val property: String
}

data class SubSealedInterface1(override val property: String) : TopSealedInterface
data class SubSealedInterface2(override val property: String) : TopSealedInterface
data class SubSealedInterface3(
    val list: List<String>,
    override val property: String
) : TopSealedInterface

data class InterfaceRequest(val types: List<TopSealedInterface>)

@GenerateOpenApi
fun Application.moduleAbstractions3() {
    routing {
        route("/v1") {

            post("/sealed") {
                call.receive<InterfaceRequest>()
            }
        }
    }
}
