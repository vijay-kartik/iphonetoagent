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
    val message: String
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