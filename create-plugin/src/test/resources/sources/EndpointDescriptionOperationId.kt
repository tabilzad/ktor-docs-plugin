package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorDescription
import io.ktor.server.application.*
import io.ktor.server.routing.*

@GenerateOpenApi
fun Application.testOperationIdInDescriptions() {
    routing {
        route("/v1") {

            @KtorDescription(
                description = "getOrdersFromPreviousDay",
                operationId = "this" +
                        "is" +
                        "OpertaionId"
            )
            get("/multilineOperationsId") {

            }
            route("/else") {

                @KtorDescription(
                     "My Summary",
                    operationId = "getOrdersAndCart"
                )
                get("/endpointWithOperationId") {

                }
                @KtorDescription(
                    description = "getOrdersFromPreviousDay",
                    operationId = "getOrdersAndCart2"
                )
                get("/endpointWithOperationId2") {

                }
            }
        }
    }
}
