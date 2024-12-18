package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

sealed class TopSealedClass {
    data class SunSealedClass1(val property: String) : TopSealedClass()
}

data class SunSealedClass2(val property: Int) : TopSealedClass()
data class SunSealedClass3(val list: List<String>) : TopSealedClass()

data class Request(val types: List<TopSealedClass>)

@GenerateOpenApi
fun Application.moduleAbstractions2() {
    routing {
        route("/v1") {

            post("/sealed") {
                call.receive<Request>()
            }
        }
    }
}
