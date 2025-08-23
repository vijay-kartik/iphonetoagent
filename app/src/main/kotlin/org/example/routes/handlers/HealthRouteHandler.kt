package org.example.routes.handlers

import io.ktor.server.application.*
import io.ktor.server.response.*
import org.example.types.HealthResponse
import org.slf4j.LoggerFactory

class HealthRouteHandler {
    private val logger = LoggerFactory.getLogger(HealthRouteHandler::class.java)
    
    suspend fun handleHealthCheck(call: ApplicationCall) {
        try {
            logger.debug("Health check requested")
            
            call.respond(
                HealthResponse(
                    status = "healthy",
                    timestamp = System.currentTimeMillis()
                )
            )
            
        } catch (e: Exception) {
            logger.error("Error during health check", e)
            call.respond(
                HealthResponse(
                    status = "unhealthy",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}