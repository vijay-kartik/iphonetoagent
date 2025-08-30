package org.example.domain.usecase.impl

import org.example.services.agentic_ai.KoogService
import org.example.services.agentic_ai.data.Transaction
import org.example.services.agentic_ai.data.TransactionType
import org.example.services.agentic_ai.data.TxnCategory
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class AnalyseSMSUseCaseImplTest {

    private val mockKoogService = mock<KoogService>()
    private val useCase = AnalyseSMSUseCaseImpl(mockKoogService)

    @Test
    fun `execute should call KoogService analyseSMS and return transaction`() {
        // Given
        val smsContent = "Test SMS content"
        val expectedTransaction = Transaction(
            date = "01/01/2024",
            detail = "Test transaction",
            amount_inr = 100.0,
            amount_usd = 0.0,
            type = TransactionType.OUTFLOW,
            category = TxnCategory.Food
        )
        whenever(mockKoogService.analyseSMS(smsContent)).thenReturn(expectedTransaction)

        // When
        val result = useCase.execute(smsContent)

        // Then
        verify(mockKoogService).analyseSMS(smsContent)
        assertEquals(expectedTransaction, result)
    }
}