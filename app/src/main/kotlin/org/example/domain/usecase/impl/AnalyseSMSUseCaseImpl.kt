package org.example.domain.usecase.impl

import org.example.domain.usecase.AnalyseSMSUseCase
import org.example.services.agentic_ai.KoogService
import org.example.services.agentic_ai.data.Transaction
import org.example.services.agentic_ai.factory.AgentType

class AnalyseSMSUseCaseImpl(
    private val koogService: KoogService
) : AnalyseSMSUseCase {
    
    override fun execute(sms: String): Transaction {
        return koogService.processWithAgent<String, Transaction>(AgentType.TRANSACTION_ANALYSIS, sms)
    }
}