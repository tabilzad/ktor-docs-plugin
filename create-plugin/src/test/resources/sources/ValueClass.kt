package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorFieldDescription
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

@JvmInline
value class Dollars(private val amount: Int)

@JvmInline
/**
 * Value class
 */
value class Cents(private val amount: Int)

@KtorFieldDescription("Description")
data class ValueWrapper(
    val value: Dollars
)

@GenerateOpenApi
fun Application.valueClassTest() {
    routing {
        route("/v1") {
            post("/wrapped") {
                call.receive<ValueWrapper>()
            }

            post("/plain") {
                call.receive<Cents>()
            }
        }
    }
}
