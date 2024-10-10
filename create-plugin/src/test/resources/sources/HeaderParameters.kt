package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.http.HttpHeaders
import io.ktor.server.application.*
import io.ktor.server.request.header
import io.ktor.server.routing.*
import sources.MyStringQueries.queryParam
import sources.HeaderStrings.headerParam

enum class MyQueries(val param: String) {
    ORDER_ID("orderId")
}

object MyStringQueries {
    val queryParam = "my_string_query_param"
}

object HeaderStrings {
    val headerParam = "my_header_param"
}

@GenerateOpenApi
fun Application.queryParametersTest() {
    routing {
        route("/v1") {
            get("/order0") {
                val query = call.request.queryParameters["my_query_param"]
                val header = call.request.headers["X_API_KEY"]
            }

            get("/order1") {
                val header = call.request.headers["my_header_param"].let {
                    println(it)
                }
                call.request.queryParameters["my_query_param"].let{
                    println(it)
                }
            }

            get("/order2") {
                val query1 = call.request.headers["my_header_param"]
                call.request.headers["my_header_param2"].let { query2 ->
                    println(query1 + query2)
                }
            }

            get("/order3") {
                call.request.headers[MyQueries.ORDER_ID.param].let{
                    println(it)
                }
            }

            get("/order4") {
                val query = call.request.queryParameters[queryParam]
                val header = call.request.headers[headerParam]
            }

            get("/order5/{withPath}") {
                call.request.queryParameters[queryParam].let{
                    println(it)
                }
                val header = call.request.headers[headerParam]
            }

            get("/order6") {
                call.request.headers["param_part1" + "param_part2"].let{
                    "notParam"+ "notParam"
                    println(it)
                }
            }

            get("/order7") {
                // DotQualified reference to a property inside string template
                call.request.headers["param_part1_${HeaderStrings.headerParam}"].let{
                    println(it)
                }
            }

            get("/order8") {
                // Reference to a property inside string template
                call.request.headers["param_part1_${headerParam}"].let{
                    println(it)
                }
            }
//           not supported yet
//            get("/order10") {
//                call.request.queryParameters[MyQueries.ORDER_ID.param + "param2"].let{
//                    println(it)
//                }
//            }
        }
    }
}
