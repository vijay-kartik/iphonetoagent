package org.example.routes.config

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.example.routes.handlers.HealthRouteHandler
import org.example.routes.handlers.IngestRouteHandler
import org.example.routes.handlers.TableIngestRouteHandler
import org.example.routes.handlers.TxnSMSRouteHandler

class RouteConfiguration {
    
    fun configureApiRoutes(application: Application) {
        application.routing {
            route("/api") {
                configureIngestRoutes()
                configureTableIngestRoutes()
                configureHealthRoutes()
                configureTxnAnalyserRoutes()
            }
        }
    }

    private fun Route.configureTxnAnalyserRoutes() {
        val txnRouteHandler = TxnSMSRouteHandler()

        route("/txn") {
            post {
                txnRouteHandler.handleTxnSMSRequest(call)
            }
        }
        route("/txn/analyse") {
            get {
                txnRouteHandler.analyseTransaction(call)
            }
        }
    }

    private fun Route.configureIngestRoutes() {
        val ingestHandler = IngestRouteHandler()
        
        route("/ingest") {
            post {
                ingestHandler.handleIngestRequest(call)
            }
        }
    }
    
    private fun Route.configureTableIngestRoutes() {
        val tableIngestHandler = TableIngestRouteHandler()
        
        route("/table-ingest") {
            post {
                tableIngestHandler.handleTableIngestRequest(call)
            }
        }
    }
    
    private fun Route.configureHealthRoutes() {
        val healthHandler = HealthRouteHandler()
        
        get("/health") {
            healthHandler.handleHealthCheck(call)
        }
    }
    
    // Extension function to make it easy to add new route groups
    private fun Route.configureAdditionalRoutes() {
        // Future routes can be added here
        // Example:
        // route("/analytics") {
        //     configureAnalyticsRoutes()
        // }
    }
}