package io.github.tabilzad

import io.github.tabilzad.ktor.KtorDescription
import io.github.tabilzad.ktor.KtorDocs
import io.github.tabilzad.ktor.KtorResponds
import io.github.tabilzad.ktor.ResponseEntry
import io.github.tabilzad.model.ErrorResponseSample
import io.github.tabilzad.model.Sample
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

@KtorDocs(["Main Module"])
fun Application.mainModule() {
    routing {
        sampleRouting()
    }
}

fun Route.sampleRouting() {
    route("/v1") {

        route("/orders") {
            @KtorDescription(
                "Create Order",
                "This endpoint will create a new order",
                tags = ["Order"]
            )
            post("/create") {
                call.receive<Sample>()
            }
            @KtorDescription(
                "Get Order",
                "This endpoint will fetch an order by id",
                tags = ["Order"]
            )
            @KtorResponds(
                [
                    ResponseEntry("200", Sample::class, description = "Order by ID"),
                    ResponseEntry("404", ErrorResponseSample::class, description = "OrderNotFound")
                ]
            )
            get("/{order_id}") {

            }
            @KtorDescription(
                "Get Orders",
                "This endpoint will fetch all orders",
                tags = ["Order"]
            )
            @KtorResponds([ResponseEntry("200", Sample::class, true, "All order by price")])
            get("/") {
                call.request.queryParameters["price"].let {
                    println(it)
                }
            }
        }

        route("/carts") {
            @KtorDescription(
                "Create Cart",
                "This endpoint will create a new cart",
                tags = ["Cart"]
            )
            post("/create") {
                call.receive<Sample>()
            }
            @KtorDescription(
                "Get Order",
                "This endpoint will fetch an cart by id",
                tags = ["Cart"]
            )
            @KtorResponds(
                [
                    ResponseEntry("200", Sample::class, description = "Cart by ID"),
                    ResponseEntry("404", ErrorResponseSample::class, description = "Cart Not Found")
                ]
            )
            get("/{cart_id}") {

            }
            @KtorDescription(
                "Get Carts",
                "This endpoint will fetch all carts",
                tags = ["Cart"]
            )
            @KtorResponds([ResponseEntry("200", Sample::class, true, "All carts by price")])
            get("/") {
                call.request.queryParameters["price"].let {
                    println(it)
                }
            }
        }
    }
}

