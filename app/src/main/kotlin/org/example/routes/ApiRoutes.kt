package org.example.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonObject
import org.example.services.auth.AuthService
import org.example.services.notion.NotionService
import org.example.types.*
import org.slf4j.LoggerFactory

class ApiRoutes {
    private val logger = LoggerFactory.getLogger(ApiRoutes::class.java)
    private val notionService = NotionService()
    private val authService = AuthService()
    
    fun configureRoutes(application: Application) {
        application.routing {
            route("/api") {
                route("/ingest") {
                    post {
                        try {
                            // Validate API key
                            if (!authService.validateApiKey(call)) {
                                return@post
                            }
                            
                            // Parse request body
                            val request = call.receive<IngestRequest>()
                            
                            // Validate request
                            if (request.title.isBlank() || request.content.isBlank()) {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    ErrorResponse(
                                        message = "Title and content are required",
                                        code = "VALIDATION_ERROR"
                                    )
                                )
                                return@post
                            }
                            
                            logger.info("Processing ingest request: ${request.title}")
                            
                            // Check if page exists and either append or create
                            val existingPageId = notionService.findPageByTitle(request.title)
                            val (notionResponse, action, message) = if (existingPageId != null) {
                                // Append to existing page
                                logger.info("Found existing page, appending content")
                                Triple(
                                    notionService.appendContentToPage(existingPageId, request.content),
                                    "appended",
                                    "Content successfully appended to existing page"
                                )
                            } else {
                                // Create new page
                                logger.info("No existing page found, creating new page")
                                Triple(
                                    notionService.createPage(request),
                                    "created",
                                    "New page successfully created in Notion"
                                )
                            }
                            
                            // Extract page ID from response
                            val pageId = existingPageId ?: notionResponse["id"]?.toString()?.removeSurrounding("\"") ?: "unknown"
                            
                            // Return success response
                            call.respond(
                                HttpStatusCode.OK,
                                IngestResponse(
                                    status = "success",
                                    notionPageId = pageId,
                                    message = message,
                                    action = action
                                )
                            )
                            
                        } catch (e: Exception) {
                            logger.error("Error processing ingest request", e)
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponse(
                                    message = "Internal server error: ${e.message}",
                                    code = "INTERNAL_ERROR"
                                )
                            )
                        }
                    }
                }
                
                // Health check endpoint
                get("/health") {
                    call.respond(
                        HealthResponse(
                            status = "healthy",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }
}
