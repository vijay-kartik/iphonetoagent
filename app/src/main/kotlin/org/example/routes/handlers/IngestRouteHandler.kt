package org.example.routes.handlers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.routes.common.runCatching
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
        call.runCatching(
            logger = logger,
            errorMessage = "Failed to process ingest request",
            errorCode = "INGEST_ERROR"
        ) {
            // Validate API key
            if (!authService.validateApiKey(this)) {
                respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid or missing API key"))
                return@runCatching
            }
            
            // Parse and validate request
            val request = parseRequest(this)
            validateRequest(request)
            
            logger.info("Processing ingest request: ${request.title}")
            
            // Process the request (append or create)
            val result = ingestService.processIngestRequest(request)
            
            // Send successful response
            respond(
                HttpStatusCode.OK,
                IngestResponse(
                    status = "success",
                    notionPageId = result.pageId,
                    message = result.message,
                    action = result.action
                )
            )
        }
    }
    
    private suspend fun parseRequest(call: ApplicationCall): IngestRequest {
        return try {
            call.receive<IngestRequest>()
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid request format: ${e.message ?: "Unknown parsing error"}")
        }
    }
    
    private fun validateRequest(request: IngestRequest) {
        if (request.title.isBlank() || request.content.isBlank()) {
            throw IllegalArgumentException("Title and content are required")
        }
    }
}