package org.example.services.business

import org.example.services.agentic_ai.data.Transaction
import org.example.services.supabase.SupabaseService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StorageService(
    private val supabaseService: SupabaseService = SupabaseService(),
) {
    suspend fun insertTransaction(txn: Transaction) {
        supabaseService.insert("transactions", txn)
    }

    /**
     * Fetch transactions for a specific month
     */
    suspend fun getMonthlyTransactions(year: Int, month: Int): List<Transaction> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())
        
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val startDateStr = startDate.format(formatter)
        val endDateStr = endDate.format(formatter)
        
        return supabaseService.selectByDateRange<Transaction>(
            tableName = "transactions",
            dateColumn = "date",
            startDate = startDateStr,
            endDate = endDateStr
        )
    }

    /**
     * Fetch transactions with custom filter
     */
    suspend fun getTransactionsByFilter(
        filterColumn: String,
        filterValue: Any,
        operator: String = "eq"
    ): List<Transaction> {
        return supabaseService.selectWithFilter<Transaction>(
            tableName = "transactions",
            filterColumn = filterColumn,
            filterValue = filterValue,
            operator = operator
        )
    }
}