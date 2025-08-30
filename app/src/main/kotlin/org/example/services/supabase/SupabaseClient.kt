package org.example.services.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import org.example.services.config.ConfigService
import org.slf4j.LoggerFactory

/**
 * Supabase client configuration for backend-to-backend access.
 * Uses service role API key for elevated permissions.
 */
class SupabaseClient private constructor() {
    
    private val logger = LoggerFactory.getLogger(SupabaseClient::class.java)
    private val config = ConfigService.getInstance()
    
    val client = createSupabaseClient(
        supabaseUrl = config.supabaseUrl,
        supabaseKey = config.supabaseServiceRoleKey
    ) {
        install(Postgrest) {
            // Configure Postgrest for database operations
            logger.debug("Initializing Supabase Postgrest client")
        }
        
        install(Auth) {
            // Configure Auth for user management
            // Note: With service role key, you have admin access to auth
            logger.debug("Initializing Supabase Auth client with service role")
        }
        
        install(Realtime) {
            // Configure Realtime for live updates
            logger.debug("Initializing Supabase Realtime client")
        }
        
        install(Storage) {
            // Configure Storage for file operations
            logger.debug("Initializing Supabase Storage client")
        }
    }
    
    init {
        logger.info("Supabase client initialized with service role access")
        logger.info("Connected to Supabase URL: ${config.supabaseUrl}")
    }
    
    companion object {
        @Volatile
        private var instance: SupabaseClient? = null
        
        fun getInstance(): SupabaseClient {
            return instance ?: synchronized(this) {
                instance ?: SupabaseClient().also { instance = it }
            }
        }
    }
}