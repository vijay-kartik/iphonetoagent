package org.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.example.types.ErrorResponse

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    message = "Internal server error: ${cause.message}",
                    code = "INTERNAL_ERROR"
                )
            )
        }
        
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    message = "Endpoint not found",
                    code = "NOT_FOUND"
                )
            )
        }
        
        status(HttpStatusCode.MethodNotAllowed) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    message = "Method not allowed",
                    code = "METHOD_NOT_ALLOWED"
                )
            )
        }
    }
}
