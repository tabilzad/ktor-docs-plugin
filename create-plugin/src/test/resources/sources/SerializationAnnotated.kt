package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SerialNameAnnotated(
    @SerialName("serial_annotated_constructor_parameter")
    val serialNameAnnotatedConstructorParameter: String,
    val notAnnotated: String,
) {
    @SerialName("serial_annotated_constructor_derived_property")
    val serialNameAnnotatedConstructorDerivedProperty = notAnnotated + ""

    @SerialName("serial_annotated_mutable_property")
    var serialNameAnnotatedProperty: String? = null

    @SerialName("serial_annotated_lateinit_var")
    lateinit var serialNameAnnotatedLateinitVar: String
}

@GenerateOpenApi
fun Application.moduleSerialization() {
    routing {
        route("/v1") {
            post("/action") {
                call.receive<SerialNameAnnotated>()
            }
        }
    }
}
