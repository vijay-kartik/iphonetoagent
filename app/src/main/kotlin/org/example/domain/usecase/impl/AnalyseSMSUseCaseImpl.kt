package org.example.domain.usecase.impl

import org.example.domain.usecase.AnalyseSMSUseCase
import org.example.services.agentic_ai.KoogService
import org.example.services.agentic_ai.data.Transaction

class AnalyseSMSUseCaseImpl(
    private val koogService: KoogService
) : AnalyseSMSUseCase {
    
    override fun execute(sms: String): Transaction {
        return koogService.analyseSMS(sms)
    }
}