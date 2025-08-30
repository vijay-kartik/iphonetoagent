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
}