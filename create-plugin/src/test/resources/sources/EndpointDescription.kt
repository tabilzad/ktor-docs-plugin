package sources

import io.ktor.server.application.*
import io.ktor.server.routing.*
import sources.annotations.KtorDescription
import sources.annotations.KtorDocs

@KtorDocs
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
