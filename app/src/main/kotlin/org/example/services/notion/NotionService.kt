package org.example.services.notion

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.example.services.config.ConfigService
import org.example.types.*
import org.slf4j.LoggerFactory

class NotionService {
    private val logger = LoggerFactory.getLogger(NotionService::class.java)
    private val config = ConfigService.getInstance()
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }
    
    suspend fun findPageByTitle(title: String): String? {
        try {
            logger.info("Searching for existing page with title: $title")
            
            val queryRequest = NotionDatabaseQueryRequest(
                filter = NotionDatabaseFilter(
                    property = "title",
                    title = NotionTitleFilter(equals = title)
                )
            )
            
            val response = httpClient.post("${config.notionBaseUrl}/v1/databases/${config.notionDatabaseId}/query") {
                header("Authorization", "Bearer ${config.notionApiToken}")
                header("Notion-Version", config.notionApiVersion)
                header("Content-Type", "application/json")
                setBody(queryRequest)
            }
            
            if (response.status.isSuccess()) {
                val searchResponse = response.body<NotionSearchResponse>()
                logger.info("Search completed. Found ${searchResponse.results.size} results")
                
                return if (searchResponse.results.isNotEmpty()) {
                    val pageId = searchResponse.results.first().id
                    logger.info("Found existing page with ID: $pageId")
                    pageId
                } else {
                    logger.info("No existing page found with title: $title")
                    null
                }
            } else {
                val errorBody = response.body<String>()
                logger.error("Failed to search pages. Status: ${response.status}, Body: $errorBody")
                return null
            }
        } catch (e: Exception) {
            logger.error("Error searching for page", e)
            return null
        }
    }

    suspend fun appendContentToPage(pageId: String, content: String): JsonObject {
        try {
            logger.info("Appending content to page: $pageId")
            
            val newBlock = NotionBlock(
                paragraph = NotionParagraph(
                    rich_text = listOf(NotionRichText(text = NotionText(content)))
                )
            )
            
            val response = httpClient.patch("${config.notionBaseUrl}/v1/blocks/$pageId/children") {
                header("Authorization", "Bearer ${config.notionApiToken}")
                header("Notion-Version", config.notionApiVersion)
                header("Content-Type", "application/json")
                setBody(mapOf("children" to listOf(newBlock)))
            }
            
            if (response.status.isSuccess()) {
                val rawResponse = response.body<JsonObject>()
                logger.info("Successfully appended content to page. Response: $rawResponse")
                return rawResponse
            } else {
                val errorBody = response.body<String>()
                logger.error("Failed to append content to page. Status: ${response.status}, Body: $errorBody")
                throw Exception("Failed to append content to page: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error("Error appending content to page", e)
            throw e
        }
    }

    suspend fun createPage(request: IngestRequest): JsonObject {
        try {
            logger.info("Creating Notion page with title: ${request.title}")
            
            val notionRequest = NotionPageRequest(
                parent = NotionParent(database_id = config.notionDatabaseId),
                properties = NotionProperties(
                    title = NotionTitle(
                        listOf(NotionRichText(text = NotionText(request.title)))
                    )
                ),
                children = listOf(
                    NotionBlock(
                        paragraph = NotionParagraph(
                            rich_text = listOf(NotionRichText(text = NotionText(request.content)))
                        )
                    )
                )
            )
            
            logger.info("Sending request to Notion: ${Json.encodeToString(NotionPageRequest.serializer(), notionRequest)}")
            
            val response = httpClient.post("${config.notionBaseUrl}/v1/pages") {
                header("Authorization", "Bearer ${config.notionApiToken}")
                header("Notion-Version", config.notionApiVersion)
                header("Content-Type", "application/json")
                setBody(notionRequest)
            }
            
            if (response.status.isSuccess()) {
                val rawResponse = response.body<JsonObject>()
                logger.info("Successfully created Notion page. Response: $rawResponse")
                return rawResponse
            } else {
                val errorBody = response.body<String>()
                logger.error("Failed to create Notion page. Status: ${response.status}, Body: $errorBody")
                throw Exception("Failed to create Notion page: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error("Error creating Notion page", e)
            throw e
        }
    }

    suspend fun processRequest(request: IngestRequest): JsonObject {
        try {
            // First, try to find an existing page with the same title
            val existingPageId = findPageByTitle(request.title)
            
            return if (existingPageId != null) {
                // Page exists, append content to it
                logger.info("Appending to existing page: $existingPageId")
                appendContentToPage(existingPageId, request.content)
            } else {
                // Page doesn't exist, create a new one
                logger.info("Creating new page with title: ${request.title}")
                createPage(request)
            }
        } catch (e: Exception) {
            logger.error("Error processing request", e)
            throw e
        }
    }
    
    fun close() {
        httpClient.close()
    }
}
