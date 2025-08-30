package org.example.services.business

import org.example.services.agentic_ai.data.Transaction
import org.example.services.supabase.SupabaseService

class StorageService(
    private val supabaseService: SupabaseService = SupabaseService(),
) {
    suspend fun insertTransaction(txn: Transaction) {
        supabaseService.insert("transactions", txn)
    }
}