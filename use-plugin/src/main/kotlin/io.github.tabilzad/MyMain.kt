package io.github.tabilzad

import io.github.tabilzad.ktor.KtorDescription
import io.github.tabilzad.ktor.KtorDocs
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

data class Sample(
    @KtorDescription("Description for field 1")
    val a: Map<String, String>,
    @KtorDescription("Description for field 2")
    val b: NestedSample,
    @KtorDescription("Description for field 3")
    val c: List<NestedSample>,
)

data class NestedSample(
    @KtorDescription("Description for field 4")
    val d: List<List<String>>
)

fun main() {}

@KtorDocs
fun Route.sampleRouting() {
    route("/v1") {

        route("/orders") {
            @KtorDescription(
                "Create Order",
                "This endpoint will create a new order"
            )
            post("/create") {
                call.receive<Sample>()
            }
            @KtorDescription(
                "Get Order",
                "This endpoint will fetch an order by id"
            )
            get("/{order_id}") {

            }
            @KtorDescription(
                "Get Orders",
                "This endpoint will fetch all orders"
            )
            get("/") {
                call.request.queryParameters["price"].let{
                    println(it)
                }
            }
        }
    }
}



