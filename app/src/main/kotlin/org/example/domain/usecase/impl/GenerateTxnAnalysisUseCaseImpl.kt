package org.example.domain.usecase.impl

import org.example.domain.usecase.GenerateTxnAnalysisUseCase
import org.example.services.agentic_ai.KoogService
import org.example.services.agentic_ai.factory.AgentType
import org.example.services.business.StorageService

class GenerateTxnAnalysisUseCaseImpl(
    private val koogService: KoogService = KoogService.getInstance(),
    private val storageService: StorageService = StorageService()
): GenerateTxnAnalysisUseCase {
    override suspend fun execute(query: String): String {
        val response = koogService.processWithAgent<String, String>(AgentType.MONTHLY_TXN_ANALYSIS, query)
        return response
    }
}