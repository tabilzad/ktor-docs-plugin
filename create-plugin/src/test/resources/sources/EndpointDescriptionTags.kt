package sources

import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorDescription
import io.github.tabilzad.ktor.annotations.Tag
import io.ktor.server.application.*
import io.ktor.server.routing.*

@GenerateOpenApi
@Tag(["module1"])
fun Application.testDescriptionTags() {
    routing {
        route("/v1") {
            @KtorDescription(
                "My Summary",
                description = "My Description",
                tags = ["tag1"]
            )
            get("/tagged1") {

            }
            @KtorDescription(
                summary = "My Description" + " multi line" +
                        "d",
                description = "1" +
                        " 2" +
                        " 3" +
                        "4",
                tags = ["tag1", "tag2"],
            )
            get("/tagged1and2") { }

            @KtorDescription(
                "My Summary" + "multi line" +
                        "d",
                "1" +
                        " 2" +
                        " 3" +
                        "4",
                tags = ["tag5", "tag6"],
            )
            get("/tagged5and6") { }

            get("/noTagsUnderModule1") { }

        }
    }
}

@GenerateOpenApi
@Tag(["module2"])
fun Application.testDescriptionTags2() {
    routing {
        route("/v2") {

            subRouteWithSpecialTag()
            route("/subRoute1") {

                @KtorDescription(
                    "My Summary",
                    description = "My Description",
                    tags = ["tag1"]
                )
                get("/tagged1AndModule2") {

                }

                get("/noTagsUnderModule2") { }
            }
        }
    }
}

@GenerateOpenApi
@Tag(["subModule"])
fun Route.subRouteWithSpecialTag(){

    route("/subRoute2") {

        @KtorDescription(
            "My Summary",
            description = "My Description",
            tags = ["tag1"]
        )
        get("/tagged1AndSubModule") {

        }
    }
}
