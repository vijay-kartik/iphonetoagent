package org.example.services.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.example.services.config.ConfigService
import org.slf4j.LoggerFactory

class AuthService {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)
    private val config = ConfigService.getInstance()
    
    suspend fun validateApiKey(call: ApplicationCall): Boolean {
        val apiKey = call.request.headers["X-API-Key"]
        
        if (apiKey.isNullOrBlank()) {
            logger.warn("Missing API key in request")
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf(
                    "status" to "error",
                    "message" to "API key is required",
                    "code" to "MISSING_API_KEY"
                )
            )
            return false
        }
        
        if (apiKey != config.apiKey) {
            logger.warn("Invalid API key provided: $apiKey")
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf(
                    "status" to "error",
                    "message" to "Invalid API key",
                    "code" to "INVALID_API_KEY"
                )
            )
            return false
        }
        
        return true
    }
}
