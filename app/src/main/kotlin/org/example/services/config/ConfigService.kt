package org.example.services.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class ConfigService {
    private val config: Config = ConfigFactory.load()
    
    val serverPort: Int = config.getInt("server.port")
    val serverHost: String = config.getString("server.host")
    
    val notionApiToken: String = try {
        config.getString("notion.apiToken")
    } catch (e: Exception) {
        System.getenv("NOTION_API_TOKEN") ?: "test-token"
    }
    
    val notionApiVersion: String = config.getString("notion.apiVersion")
    
    val notionDatabaseId: String = try {
        config.getString("notion.databaseId")
    } catch (e: Exception) {
        System.getenv("NOTION_DATABASE_ID") ?: "test-db-id"
    }
    
    val notionBaseUrl: String = config.getString("notion.baseUrl")
    
    val apiKey: String = try {
        config.getString("security.apiKey")
    } catch (e: Exception) {
        System.getenv("API_KEY") ?: "test-api-key"
    }
    
    companion object {
        private var instance: ConfigService? = null
        
        fun getInstance(): ConfigService {
            if (instance == null) {
                instance = ConfigService()
            }
            return instance!!
        }
    }
}
