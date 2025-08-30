package org.example.domain.usecase

import org.example.services.agentic_ai.data.Transaction

interface AnalyseSMSUseCase {
    fun execute(sms: String): Transaction
}