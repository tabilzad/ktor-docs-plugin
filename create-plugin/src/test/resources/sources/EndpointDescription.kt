package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorDescription
import io.ktor.server.application.*
import io.ktor.server.routing.*

@GenerateOpenApi
fun Application.testDescription() {
    routing {
        route("/base") {

            route("/moreBase") {

                @KtorDescription(
                     "My Summary",
                    description = "My Description"
                )
                get("/getMixedExplicitParamName") {

                }
                @KtorDescription(
                    summary = "My Description" + " multi line" +
                            "d",
                    description = "1" +
                            " 2" +
                            " 3" +
                            "4"
                )
                get("/getExplicitParamName") {  }

                @KtorDescription(
                    "My Summary" + "multi line" +
                            "d",
                    "1" +
                            " 2" +
                            " 3" +
                            "4"
                )
                get("/getImplicitParamName") {  }
            }
        }
    }
}
