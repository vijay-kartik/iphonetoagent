package org.example.routes.handlers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.services.auth.AuthService
import org.example.services.business.IngestService
import org.example.types.*
import org.slf4j.LoggerFactory

class IngestRouteHandler(
    private val ingestService: IngestService = IngestService(),
    private val authService: AuthService = AuthService()
) {
    private val logger = LoggerFactory.getLogger(IngestRouteHandler::class.java)
    
    suspend fun handleIngestRequest(call: ApplicationCall) {
        try {
            // Validate API key
            if (!authService.validateApiKey(call)) {
                return
            }
            
            // Parse and validate request
            val request = parseRequest(call)
            validateRequest(request)
            
            logger.info("Processing ingest request: ${request.title}")
            
            // Process the request (append or create)
            val result = ingestService.processIngestRequest(request)
            
            // Send successful response
            call.respond(
                HttpStatusCode.OK,
                IngestResponse(
                    status = "success",
                    notionPageId = result.pageId,
                    message = result.message,
                    action = result.action
                )
            )
            
        } catch (e: ValidationException) {
            logger.warn("Validation error: ${e.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    message = e.message ?: "Validation error",
                    code = "VALIDATION_ERROR"
                )
            )
        } catch (e: Exception) {
            logger.error("Error processing ingest request", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    message = "Internal server error: ${e.message ?: "Unknown error"}",
                    code = "INTERNAL_ERROR"
                )
            )
        }
    }
    
    private suspend fun parseRequest(call: ApplicationCall): IngestRequest {
        return try {
            call.receive<IngestRequest>()
        } catch (e: Exception) {
            throw ValidationException("Invalid request format: ${e.message ?: "Unknown parsing error"}")
        }
    }
    
    private fun validateRequest(request: IngestRequest) {
        if (request.title.isBlank() || request.content.isBlank()) {
            throw ValidationException("Title and content are required")
        }
    }
}

class ValidationException(message: String) : Exception(message)