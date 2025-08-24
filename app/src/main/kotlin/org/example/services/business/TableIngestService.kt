package org.example.services.business

import org.example.services.notion.NotionService
import org.example.types.TableIngestRequest
import org.slf4j.LoggerFactory

class TableIngestService(
    private val notionService: NotionService = NotionService()
) {
    private val logger = LoggerFactory.getLogger(TableIngestService::class.java)

    suspend fun processTableIngestRequest(request: TableIngestRequest): TableIngestResult {
        try {
            logger.info("Processing table ingest request for page: ${request.pageTitle}")
            
            val response = notionService.processTableRequest(request)
            
            // Extract information from Notion response
            val pageId = response["id"]?.toString()?.replace("\"", "") 
            val objectType = response["object"]?.toString()?.replace("\"", "")
            
            // Check if response indicates success (could be page, block, or list)
            val success = objectType in listOf("page", "block", "list") || response.containsKey("results")
            
            return if (success) {
                val action = if (pageId != null) "appended" else "created"
                TableIngestResult(
                    success = true,
                    pageId = pageId,
                    message = "Successfully ${action} table data to page: ${request.pageTitle}",
                    action = action,
                    tableRowId = null // Could extract from response if needed
                )
            } else {
                throw TableIngestException("Notion API returned unsuccessful response: $response")
            }
            
        } catch (e: Exception) {
            logger.error("Error processing table ingest request for page: ${request.pageTitle}", e)
            throw TableIngestException("Failed to process table request: ${e.message}", e)
        }
    }
}

data class TableIngestResult(
    val success: Boolean,
    val pageId: String?,
    val message: String,
    val action: String?, // "created" or "appended"
    val tableRowId: String?
)

class TableIngestException(message: String, cause: Throwable? = null) : Exception(message, cause)