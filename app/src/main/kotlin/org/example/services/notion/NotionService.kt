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
import org.example.services.agentic_ai.KoogService
import org.example.services.config.ConfigService
import org.example.types.*
import org.slf4j.LoggerFactory

class NotionService {
    private val logger = LoggerFactory.getLogger(NotionService::class.java)
    private val config = ConfigService.getInstance()
    private val koog = KoogService.getInstance()
    
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
            val aiContent = koog.analyseSMS(content)
            
            val newBlock = NotionBlock(
                paragraph = NotionParagraph(
                    rich_text = listOf(NotionRichText(text = NotionText(aiContent.toString())))
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

    suspend fun appendToTable(pageId: String, tableData: Map<String, String>): JsonObject {
        try {
            logger.info("Appending row to table in page: $pageId")
            
            // First, get the page blocks to find the table
            val tableBlockId = findTableInPage(pageId)
                ?: throw Exception("No table found in page $pageId")
            
            // Convert the map data to table cells
            val cells = tableData.values.map { value ->
                listOf(NotionRichText(text = NotionText(value)))
            }
            
            val newTableRow = NotionTableRow(
                table_row = NotionTableRowData(cells = cells)
            )
            
            val response = httpClient.patch("${config.notionBaseUrl}/v1/blocks/$tableBlockId/children") {
                header("Authorization", "Bearer ${config.notionApiToken}")
                header("Notion-Version", config.notionApiVersion)
                header("Content-Type", "application/json")
                setBody(mapOf("children" to listOf(newTableRow)))
            }
            
            if (response.status.isSuccess()) {
                val rawResponse = response.body<JsonObject>()
                logger.info("Successfully appended row to table. Response: $rawResponse")
                return rawResponse
            } else {
                val errorBody = response.body<String>()
                logger.error("Failed to append row to table. Status: ${response.status}, Body: $errorBody")
                throw Exception("Failed to append row to table: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error("Error appending row to table", e)
            throw e
        }
    }

    suspend fun createTableInPage(pageId: String, tableData: Map<String, String>, columnHeaders: List<String>): JsonObject {
        try {
            logger.info("Creating table in page: $pageId")
            
            // Create header row
            val headerCells = columnHeaders.map { header ->
                listOf(NotionRichText(text = NotionText(header)))
            }
            
            // Create data row
            val dataCells = tableData.values.map { value ->
                listOf(NotionRichText(text = NotionText(value)))
            }
            
            val tableRows = listOf(
                NotionTableRow(table_row = NotionTableRowData(cells = headerCells)), // Header row
                NotionTableRow(table_row = NotionTableRowData(cells = dataCells))    // Data row
            )
            
            val tableBlock = NotionTableBlock(
                table = NotionTable(
                    table_width = columnHeaders.size,
                    has_column_header = true,
                    children = tableRows
                )
            )
            
            val response = httpClient.patch("${config.notionBaseUrl}/v1/blocks/$pageId/children") {
                header("Authorization", "Bearer ${config.notionApiToken}")
                header("Notion-Version", config.notionApiVersion)
                header("Content-Type", "application/json")
                setBody(mapOf("children" to listOf(tableBlock)))
            }
            
            if (response.status.isSuccess()) {
                val rawResponse = response.body<JsonObject>()
                logger.info("Successfully created table in page. Response: $rawResponse")
                return rawResponse
            } else {
                val errorBody = response.body<String>()
                logger.error("Failed to create table in page. Status: ${response.status}, Body: $errorBody")
                throw Exception("Failed to create table in page: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error("Error creating table in page", e)
            throw e
        }
    }

    private suspend fun findTableInPage(pageId: String): String? {
        try {
            logger.info("Looking for table in page: $pageId")
            val response = httpClient.get("${config.notionBaseUrl}/v1/blocks/$pageId/children") {
                header("Authorization", "Bearer ${config.notionApiToken}")
                header("Notion-Version", config.notionApiVersion)
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.body<JsonObject>()
                logger.info("Page blocks response: $responseBody")
                
                // Parse the results array properly
                val results = responseBody["results"]?.toString()
                if (results != null) {
                    // First, check if we have a table type anywhere in the response
                    if (results.contains("\"type\":\"table\"")) {
                        // Extract the ID that comes before the table type
                        // Look for pattern: "id":"some-id"...anything..."type":"table"
                        val tableBlockRegex = "\"id\":\"([^\"]+)\"[\\s\\S]*?\"type\":\"table\"".toRegex()
                        val matchResult = tableBlockRegex.find(results)
                        
                        if (matchResult != null) {
                            val tableId = matchResult.groupValues[1]
                            logger.info("Found existing table with ID: $tableId")
                            return tableId
                        }
                    }
                    logger.info("No table found in page blocks")
                }
            } else {
                logger.error("Failed to get page blocks. Status: ${response.status}")
            }
            return null
        } catch (e: Exception) {
            logger.error("Error finding table in page", e)
            return null
        }
    }

    suspend fun processTableRequest(request: TableIngestRequest): JsonObject {
        try {
            // First, try to find an existing page with the same title
            val existingPageId = findPageByTitle(request.pageTitle)
            
            return if (existingPageId != null) {
                // Page exists, try to append to existing table or create new table
                logger.info("Found existing page: $existingPageId")
                val tableBlockId = findTableInPage(existingPageId)
                
                if (tableBlockId != null) {
                    logger.info("Appending to existing table in page: $existingPageId")
                    appendToTable(existingPageId, request.tableData)
                } else {
                    logger.info("Creating new table in existing page: $existingPageId")
                    createTableInPage(existingPageId, request.tableData, request.tableData.keys.toList())
                }
            } else {
                // Page doesn't exist, create a new one with a table
                logger.info("Creating new page with table. Title: ${request.pageTitle}")
                val newPageResponse = createPage(IngestRequest(
                    title = request.pageTitle,
                    content = "Created with table data",
                    metadata = request.metadata
                ))
                
                // Extract page ID from response and create table
                val pageId = newPageResponse["id"]?.toString()?.replace("\"", "") 
                    ?: throw Exception("Could not extract page ID from response")
                
                createTableInPage(pageId, request.tableData, request.tableData.keys.toList())
            }
        } catch (e: Exception) {
            logger.error("Error processing table request", e)
            throw e
        }
    }
    
    fun close() {
        httpClient.close()
    }
}
