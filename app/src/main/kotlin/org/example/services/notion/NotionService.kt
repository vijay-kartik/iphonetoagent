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
    
    fun close() {
        httpClient.close()
    }
}
