package org.example.types

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class IngestRequest(
    val title: String,
    val content: String,
    val metadata: Metadata? = null
)

@Serializable
data class Metadata(
    val source: String = "iPhoneShortcuts",
    val timestamp: String = Instant.now().toString()
)

@Serializable
data class IngestResponse(
    val status: String,
    val notionPageId: String? = null,
    val message: String,
    val action: String? = null // "created" or "appended"
)

@Serializable
data class ErrorResponse(
    val status: String = "error",
    val message: String,
    val code: String? = null
)

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: Long
)

@Serializable
data class NotionPageRequest(
    val parent: NotionParent,
    val properties: NotionProperties,
    val children: List<NotionBlock>? = null
)

@Serializable
data class NotionParent(
    val type: String = "database_id",
    val database_id: String
)

@Serializable
data class NotionProperties(
    val title: NotionTitle,
    val content: NotionRichText? = null
)

@Serializable
data class NotionTitle(
    val title: List<NotionRichText>
)

@Serializable
data class NotionRichText(
    val type: String = "text",
    val text: NotionText
)

@Serializable
data class NotionText(
    val content: String
)

@Serializable
data class NotionBlock(
    val `object`: String = "block",
    val type: String = "paragraph",
    val paragraph: NotionParagraph
)

@Serializable
data class NotionParagraph(
    val rich_text: List<NotionRichText>
)

@Serializable
data class NotionPageResponse(
    val id: String,
    val url: String,
    val properties: NotionProperties
)

// Search request and response types
@Serializable
data class NotionSearchRequest(
    val query: String,
    val filter: NotionSearchFilter? = null,
    val page_size: Int = 10
)

@Serializable
data class NotionSearchFilter(
    val value: String = "page",
    val property: String = "object"
)

@Serializable
data class NotionSearchResponse(
    val results: List<NotionSearchResult>,
    val has_more: Boolean,
    val next_cursor: String? = null
)

@Serializable
data class NotionSearchResult(
    val id: String,
    val url: String,
    val properties: Map<String, NotionProperty>
)

@Serializable
data class NotionProperty(
    val type: String,
    val title: List<NotionRichText>? = null
)

// Database query types
@Serializable
data class NotionDatabaseQueryRequest(
    val filter: NotionDatabaseFilter? = null,
    val page_size: Int = 10
)

@Serializable
data class NotionDatabaseFilter(
    val property: String,
    val title: NotionTitleFilter
)

@Serializable
data class NotionTitleFilter(
    val equals: String
)

// Table operations types
@Serializable
data class TableIngestRequest(
    val pageTitle: String,
    val tableData: Map<String, String>, // column_name -> value
    val metadata: Metadata? = null
)

@Serializable
data class TableIngestResponse(
    val status: String,
    val notionPageId: String? = null,
    val message: String,
    val action: String? = null, // "created" or "appended"
    val tableRowId: String? = null
)

// Notion table block types
@Serializable
data class NotionTableBlock(
    val `object`: String = "block",
    val type: String = "table",
    val table: NotionTable
)

@Serializable
data class NotionTable(
    val table_width: Int,
    val has_column_header: Boolean = true,
    val has_row_header: Boolean = false,
    val children: List<NotionTableRow>
)

@Serializable
data class NotionTableRow(
    val `object`: String = "block", 
    val type: String = "table_row",
    val table_row: NotionTableRowData
)

@Serializable
data class NotionTableRowData(
    val cells: List<List<NotionRichText>>
)