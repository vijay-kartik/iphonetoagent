package org.example.routes

import io.ktor.server.application.*
import org.example.routes.config.RouteConfiguration
import org.slf4j.LoggerFactory

/**
 * Main API Routes class that delegates to modular route configuration.
 * This class serves as the entry point for all API routing configuration.
 */
class ApiRoutes {
    private val logger = LoggerFactory.getLogger(ApiRoutes::class.java)
    private val routeConfiguration = RouteConfiguration()
    
    /**
     * Configures all API routes for the application.
     * Routes are organized into separate handlers for better maintainability.
     */
    fun configureRoutes(application: Application) {
        logger.info("Configuring API routes...")
        
        try {
            routeConfiguration.configureApiRoutes(application)
            logger.info("API routes configured successfully")
        } catch (e: Exception) {
            logger.error("Failed to configure API routes", e)
            throw e
        }
    }
}
