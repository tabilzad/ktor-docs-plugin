package sources

import com.squareup.moshi.Json
import io.github.tabilzad.ktor.GenerateOpenApi
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing


data class MoshiAnnotated(
    @Json(name = "moshi_annotated_constructor_parameter")
    val moshiAnnotatedConstructorParameter: String,
    val notMoshiAnnotated: String,
) {
    @Json(name = "moshi_annotated_constructor_derived_property")
    val moshiAnnotatedConstructorDerivedProperty = notMoshiAnnotated + ""

    @Json(name = "moshi_annotated_mutable_property")
    var moshiAnnotatedProperty: String? = null

    @Json(name = "moshi_annotated_lateinit_var")
    lateinit var moshiAnnotatedLateinitVar: String
}

@GenerateOpenApi
fun Application.module1() {
    routing {
        route("/v1") {
            post("/action") {
                call.receive<MoshiAnnotated>()
            }
        }
    }
}
