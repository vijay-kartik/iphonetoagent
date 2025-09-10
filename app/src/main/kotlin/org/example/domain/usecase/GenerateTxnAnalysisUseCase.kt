package org.example.domain.usecase

interface GenerateTxnAnalysisUseCase {
    suspend fun execute(query: String): String
}