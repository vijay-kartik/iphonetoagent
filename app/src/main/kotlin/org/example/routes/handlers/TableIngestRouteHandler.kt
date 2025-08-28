package org.example.routes.handlers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.routes.common.runCatching
import org.example.services.auth.AuthService
import org.example.services.business.TableIngestService
import org.example.types.*
import org.slf4j.LoggerFactory

class TableIngestRouteHandler(
    private val authService: AuthService = AuthService(),
    private val tableIngestService: TableIngestService = TableIngestService()
) {
    private val logger = LoggerFactory.getLogger(TableIngestRouteHandler::class.java)

    suspend fun handleTableIngestRequest(call: ApplicationCall) {
        call.runCatching(
            logger = logger,
            errorMessage = "Failed to process table request",
            errorCode = "TABLE_INGEST_ERROR"
        ) {
            // Validate API key
            if (!authService.validateApiKey(this)) {
                respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid or missing API key"))
                return@runCatching
            }

            // Parse and validate request
            val request = parseRequest(this)
            validateRequest(request)
            
            logger.info("Processing table ingest request: ${request.pageTitle}")
            val result = tableIngestService.processTableIngestRequest(request)

            respond(HttpStatusCode.OK, 
                TableIngestResponse(
                    status = "success",
                    notionPageId = result.pageId,
                    message = result.message,
                    action = result.action,
                    tableRowId = result.tableRowId
                )
            )
        }
    }

    private suspend fun parseRequest(call: ApplicationCall): TableIngestRequest {
        return try {
            call.receive<TableIngestRequest>()
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid request body: ${e.message}")
        }
    }

    private fun validateRequest(request: TableIngestRequest) {
        when {
            request.pageTitle.isBlank() -> throw IllegalArgumentException("Page title cannot be empty")
            request.tableData.isEmpty() -> throw IllegalArgumentException("Table data cannot be empty")
            request.tableData.values.any { it.isBlank() } -> throw IllegalArgumentException("Table data values cannot be empty")
        }
    }
}