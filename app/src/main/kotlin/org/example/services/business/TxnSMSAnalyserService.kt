package org.example.services.business

import org.example.routes.response.TxnSmsResponse
import org.example.services.agentic_ai.data.Transaction
import org.example.services.notion.NotionService
import org.slf4j.LoggerFactory

class TxnSMSAnalyserService(
    private val notionService: NotionService = NotionService(),
    private val storageService: StorageService = StorageService(),
) {
    private val logger = LoggerFactory.getLogger(TxnSMSAnalyserService::class.java)

    suspend fun processTxnSmsRequest(txn: Transaction): Result<TxnSmsResponse> {
        try {
            val response = notionService.saveTransaction(txn)
            saveTxnIntoSupabase(txn)
            val objectType = response["object"]?.toString()?.replace("\"", "")

            // Check if response indicates success (could be page, block, or list)
            val success = objectType in listOf("page", "block", "list") || response.containsKey("results")
            return Result.success(TxnSmsResponse(status = "success", success.toString()))

        } catch (e: Exception) {
            logger.error("Error processing table ingest request ", e)
            return Result.failure<TxnSmsResponse>(TableIngestException("Error processing table ingest request $e"))
        }
    }

    private suspend fun saveTxnIntoSupabase(txn: Transaction): Result<TxnSmsResponse> {
        return try {
            storageService.insertTransaction(txn)
            Result.success(TxnSmsResponse("success", "true"))
        } catch (e: java.lang.Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch user's monthly transactions for analysis
     * @param year Year (e.g., 2025)
     * @param month Month (1-12)
     * @return List of transactions for the specified month
     */
    suspend fun getMonthlyTransactions(year: Int, month: Int): Result<List<Transaction>> {
        return try {
            logger.info("Fetching monthly transactions for $year-$month")
            
            val transactions = storageService.getMonthlyTransactions(year, month)
            
            logger.info("Successfully retrieved ${transactions.size} transactions for $year-$month")
            Result.success(transactions)
        } catch (e: Exception) {
            logger.error("Error fetching monthly transactions for $year-$month", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch user's transactions by custom filter
     * @param filterColumn Column to filter by (e.g., "amount", "category", "merchant")
     * @param filterValue Value to filter for
     * @param operator Comparison operator (eq, gte, lte, gt, lt, like, ilike)
     * @return List of matching transactions
     */
    suspend fun getTransactionsByFilter(
        filterColumn: String,
        filterValue: Any,
        operator: String = "eq"
    ): Result<List<Transaction>> {
        return try {
            logger.info("Fetching transactions with filter: $filterColumn $operator $filterValue")
            
            val transactions = storageService.getTransactionsByFilter(filterColumn, filterValue, operator)
            
            logger.info("Successfully retrieved ${transactions.size} transactions matching filter")
            Result.success(transactions)
        } catch (e: Exception) {
            logger.error("Error fetching transactions with filter: $filterColumn $operator $filterValue", e)
            Result.failure(e)
        }
    }
}