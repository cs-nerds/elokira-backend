package web

import io.ktor.application.call
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.index() {

    get("/") {
        call.respond(
            HttpStatusCode.OK,
            mapOf("status" to "It works!", "message" to "Welcome to Elokira Vote API")
        )
    }
}
