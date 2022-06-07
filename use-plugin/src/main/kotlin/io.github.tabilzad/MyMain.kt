package io.github.tabilzad

import com.squareup.moshi.Json
import io.github.tabilzad.ktor.KtorDocs
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*

data class Sample(
    val a: Map<String, String>,
    @Json(name = "b_changed")
    val b: More,
    val c: List<More>,
)

data class More(
    val h: List<List<String>>
)

fun main() { }

@KtorDocs
fun Route.ordersRouting() {
    route("/v1") {

        post("/digitalOrder") {
            call.receive<Sample>()
        }
        route("/order") {
            route("/myOrder") {
                get {

                }
            }
            post {
                call.receive<Sample>().let {

                }
            }
            post("/localsave") {
                call.receive<String>().let {

                }
            }
            post("/print") {
                call.receive<String>().let {

                }
            }
        }
        route("/orders") {
            get {
                call.receive<String>().let {

                }
            }
            get("/{order_id}") {
                call.receive<String>().let {

                }
            }
        }
    }
}



