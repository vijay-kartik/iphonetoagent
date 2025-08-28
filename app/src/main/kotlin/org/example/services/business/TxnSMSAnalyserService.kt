package org.example.services.business

import org.example.routes.request.TxnSMSRequest
import org.example.routes.response.TxnSmsResponse
import org.example.services.notion.NotionService
import org.slf4j.LoggerFactory

class TxnSMSAnalyserService(
    private val notionService: NotionService = NotionService()
) {
    private val logger = LoggerFactory.getLogger(TxnSMSAnalyserService::class.java)

    suspend fun processTxnSmsRequest(request: TxnSMSRequest): Result<TxnSmsResponse> {
        try {
            val response = notionService.processTxnSmsRequest(request)
            val objectType = response["object"]?.toString()?.replace("\"", "")

            // Check if response indicates success (could be page, block, or list)
            val success = objectType in listOf("page", "block", "list") || response.containsKey("results")
            return Result.success(TxnSmsResponse(status = "success", success.toString()))

        } catch (e: Exception) {
            logger.error("Error processing table ingest request ", e)
            return Result.failure<TxnSmsResponse>(TableIngestException("Error processing table ingest request $e"))
        }
    }
}