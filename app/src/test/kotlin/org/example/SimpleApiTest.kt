package org.example

import kotlin.test.Test
import kotlin.test.assertTrue

class SimpleApiTest {
    @Test
    fun `test basic functionality`() {
        assertTrue(true, "Basic test should pass")
    }
    
    @Test
    fun `test configuration loading`() {
        val config = org.example.services.config.ConfigService.getInstance()
        assertTrue(config.apiKey.isNotEmpty(), "API key should be loaded")
        assertTrue(config.notionApiToken.isNotEmpty(), "Notion token should be loaded")
        assertTrue(config.notionDatabaseId.isNotEmpty(), "Notion database ID should be loaded")
    }
}
