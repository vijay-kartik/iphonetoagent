package org.example.services.business

import org.example.services.notion.NotionService
import org.example.types.IngestRequest
import org.slf4j.LoggerFactory

class IngestService(
    private val notionService: NotionService = NotionService()
) {
    private val logger = LoggerFactory.getLogger(IngestService::class.java)
    
    suspend fun processIngestRequest(request: IngestRequest): IngestResult {
        return try {
            // Check if a page with this title already exists
            val existingPageId = notionService.findPageByTitle(request.title)
            
            if (existingPageId != null) {
                // Append to existing page
                logger.info("Found existing page with ID: $existingPageId, appending content")
                appendToExistingPage(existingPageId, request)
            } else {
                // Create new page
                logger.info("No existing page found, creating new page with title: ${request.title}")
                createNewPage(request)
            }
        } catch (e: Exception) {
            logger.error("Error processing ingest request for title: ${request.title}", e)
            throw IngestException("Failed to process request: ${e.message}", e)
        }
    }
    
    private fun appendToExistingPage(pageId: String, request: IngestRequest): IngestResult {
        return try {
            logger.info("Successfully appended content to page: $pageId")
            
            IngestResult(
                pageId = pageId,
                action = "appended",
                message = "Content successfully appended to existing page",
                isSuccess = true
            )
        } catch (e: Exception) {
            logger.error("Failed to append content to page: $pageId", e)
            throw IngestException("Failed to append content to existing page", e)
        }
    }
    
    private suspend fun createNewPage(request: IngestRequest): IngestResult {
        return try {
            val response = notionService.createPage(request)
            val pageId = response["id"]?.toString()?.removeSurrounding("\"") ?: "unknown"
            
            logger.info("Successfully created new page with ID: $pageId")
            
            IngestResult(
                pageId = pageId,
                action = "created",
                message = "New page successfully created in Notion",
                isSuccess = true
            )
        } catch (e: Exception) {
            logger.error("Failed to create new page with title: ${request.title}", e)
            throw IngestException("Failed to create new page", e)
        }
    }
}

data class IngestResult(
    val pageId: String,
    val action: String,
    val message: String,
    val isSuccess: Boolean
)

class IngestException(message: String, cause: Throwable? = null) : Exception(message, cause)