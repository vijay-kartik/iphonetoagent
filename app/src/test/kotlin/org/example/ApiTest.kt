package org.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import org.example.types.IngestRequest
import org.example.types.Metadata

class ApiTest {
    @Test
    fun `test health endpoint`() = testApplication {
        application {
            module()
        }
        
        client.get("/api/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().contains("healthy"))
        }
    }
    
    @Test
    fun `test ingest endpoint without API key`() = testApplication {
        application {
            module()
        }
        
        val request = IngestRequest(
            title = "Test Title",
            content = "Test Content",
            metadata = Metadata()
        )
        
        client.post("/api/ingest") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
            assertTrue(bodyAsText().contains("API key is required"))
        }
    }
    
    @Test
    fun `test ingest endpoint with invalid API key`() = testApplication {
        application {
            module()
        }
        
        val request = IngestRequest(
            title = "Test Title",
            content = "Test Content",
            metadata = Metadata()
        )
        
        client.post("/api/ingest") {
            contentType(ContentType.Application.Json)
            header("X-API-Key", "invalid-key")
            setBody(request)
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
            assertTrue(bodyAsText().contains("Invalid API key"))
        }
    }
    
    @Test
    fun `test ingest endpoint with empty title`() = testApplication {
        application {
            module()
        }
        
        val request = IngestRequest(
            title = "",
            content = "Test Content",
            metadata = Metadata()
        )
        
        client.post("/api/ingest") {
            contentType(ContentType.Application.Json)
            header("X-API-Key", "test-api-key")
            setBody(request)
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertTrue(bodyAsText().contains("Title and content are required"))
        }
    }
    
    @Test
    fun `test ingest endpoint with empty content`() = testApplication {
        application {
            module()
        }
        
        val request = IngestRequest(
            title = "Test Title",
            content = "",
            metadata = Metadata()
        )
        
        client.post("/api/ingest") {
            contentType(ContentType.Application.Json)
            header("X-API-Key", "test-api-key")
            setBody(request)
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertTrue(bodyAsText().contains("Title and content are required"))
        }
    }
}
