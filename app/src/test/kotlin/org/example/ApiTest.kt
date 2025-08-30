package org.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

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
    
}
