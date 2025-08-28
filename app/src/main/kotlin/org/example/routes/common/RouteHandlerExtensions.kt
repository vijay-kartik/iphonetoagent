package org.example.routes.common

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.example.types.ErrorResponse
import org.slf4j.Logger

/**
 * Extension function for ApplicationCall to handle common try-catch logic in route handlers.
 * This eliminates code duplication across different route handlers.
 * 
 * @param logger The logger instance for the calling class
 * @param errorMessage Custom error message prefix for the exception
 * @param errorCode Optional error code for the error response
 * @param block The suspend block to execute within try-catch
 */
suspend fun ApplicationCall.runCatching(
    logger: Logger,
    errorMessage: String = "Failed to process request",
    errorCode: String = "PROCESSING_ERROR",
    block: suspend ApplicationCall.() -> Unit
) {
    try {
        block()
    } catch (e: IllegalArgumentException) {
        // Handle validation errors with 400 Bad Request
        logger.warn("Validation error: ${e.message}")
        respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                message = e.message ?: "Invalid request",
                code = "VALIDATION_ERROR"
            )
        )
    } catch (e: Exception) {
        // Handle all other errors with 500 Internal Server Error
        logger.error("$errorMessage: ${e.message}", e)
        respond(
            HttpStatusCode.InternalServerError,
            ErrorResponse(
                message = "$errorMessage: ${e.message}",
                code = errorCode
            )
        )
    }
}

/**
 * Alternative extension function that allows customizing the success HTTP status code
 */
suspend fun ApplicationCall.runCatchingWithStatus(
    logger: Logger,
    successStatus: HttpStatusCode = HttpStatusCode.OK,
    errorMessage: String = "Failed to process request",
    errorCode: String = "PROCESSING_ERROR",
    block: suspend ApplicationCall.() -> Any?
) {
    try {
        val result = block()
        if (result != null) {
            respond(successStatus, result)
        }
    } catch (e: IllegalArgumentException) {
        logger.warn("Validation error: ${e.message}")
        respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                message = e.message ?: "Invalid request",
                code = "VALIDATION_ERROR"
            )
        )
    } catch (e: Exception) {
        logger.error("$errorMessage: ${e.message}", e)
        respond(
            HttpStatusCode.InternalServerError,
            ErrorResponse(
                message = "$errorMessage: ${e.message}",
                code = errorCode
            )
        )
    }
}