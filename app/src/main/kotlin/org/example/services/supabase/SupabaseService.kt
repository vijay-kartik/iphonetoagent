package org.example.services.supabase

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import org.slf4j.LoggerFactory

/**
 * Service class for Supabase operations.
 * Provides high-level methods for database operations using the Supabase client.
 */
class SupabaseService {
    
    val logger = LoggerFactory.getLogger(SupabaseService::class.java)
    val supabaseClient = SupabaseClient.getInstance().client
    
    /**
     * Select all records from a table
     */
    suspend inline fun <reified T : Any> selectAll(
        tableName: String,
        columns: Columns = Columns.ALL
    ): List<T> {
        return try {
            logger.info("Querying all records from table: $tableName")
            
            val result = supabaseClient
                .from(tableName)
                .select(columns)
                .decodeList<T>()

            logger.info("Successfully retrieved ${result.size} records from $tableName")
            result
        } catch (e: Exception) {
            logger.error("Error querying table $tableName", e)
            throw e
        }
    }
    
    /**
     * Insert a single record
     */
    suspend inline fun <reified T : Any> insert(
        tableName: String,
        data: T
    ): T {
        return try {
            logger.info("Inserting record into table: $tableName")
            
            val result = supabaseClient
                .from(tableName)
                .insert(data) {
                    select()
                }
                .decodeSingle<T>()
            
            logger.info("Successfully inserted record into $tableName")
            result
        } catch (e: Exception) {
            logger.error("Error inserting into table $tableName", e)
            throw e
        }
    }
    
    /**
     * Update records with filter
     */
    suspend inline fun <reified T : Any> update(
        tableName: String,
        updates: Map<String, Any>
    ): List<T> {
        return try {
            logger.info("Updating records in table: $tableName")
            
            val result = supabaseClient
                .from(tableName)
                .update(updates) {
                    select()
                }
                .decodeList<T>()
            
            logger.info("Successfully updated ${result.size} records in $tableName")
            result
        } catch (e: Exception) {
            logger.error("Error updating table $tableName", e)
            throw e
        }
    }
    
    /**
     * Delete records from table
     */
    suspend fun delete(tableName: String) {
        try {
            logger.info("Deleting records from table: $tableName")

            supabaseClient
                .from(tableName)
                .delete()
            
            logger.info("Successfully deleted records from $tableName")
        } catch (e: Exception) {
            logger.error("Error deleting from table $tableName", e)
            throw e
        }
    }
    
    companion object {
        @Volatile
        private var instance: SupabaseService? = null
        
        fun getInstance(): SupabaseService {
            return instance ?: synchronized(this) {
                instance ?: SupabaseService().also { instance = it }
            }
        }
    }
}