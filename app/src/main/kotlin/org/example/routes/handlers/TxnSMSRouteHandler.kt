package org.example.routes.handlers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.example.domain.usecase.AnalyseSMSUseCase
import org.example.domain.usecase.impl.AnalyseSMSUseCaseImpl
import org.example.routes.common.runCatching
import org.example.routes.request.TxnSMSRequest
import org.example.routes.response.TxnSmsResponse
import org.example.services.agentic_ai.KoogService
import org.example.services.auth.AuthService
import org.example.services.business.TxnSMSAnalyserService
import org.example.types.ErrorResponse
import org.slf4j.LoggerFactory

class TxnSMSRouteHandler(
    private val authService: AuthService = AuthService(),
    private val txnSMSAnalyserService: TxnSMSAnalyserService = TxnSMSAnalyserService(),
    private val analyseSMSUseCase: AnalyseSMSUseCase = AnalyseSMSUseCaseImpl(KoogService.getInstance())
) {
    private val logger = LoggerFactory.getLogger(TxnSMSRouteHandler::class.java)
    suspend fun handleTxnSMSRequest(call: ApplicationCall) {
        call.runCatching(
            logger = logger,
            errorMessage = "Failed to process table request",
            errorCode = "HANDLE_TXN_SMS_ERROR"
        ) {
            // Validate API key
            if (!authService.validateApiKey(this)) {
                respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid or missing API key"))
                return@runCatching
            }

            // Parse and validate request
            val request = receive<TxnSMSRequest>()
            validateRequest(request)

            logger.info("Processing txn sms request: ${request.content}")
//            val result = txnSMSAnalyserService.processTxnSmsRequest(request)
            val transaction = analyseSMSUseCase.execute(request.content)
            val result = txnSMSAnalyserService.processTxnSmsRequest(transaction)

            if (result.isSuccess) {
                respond(HttpStatusCode.OK, result.getOrNull() ?: "No Data")
            } else {
                respond(HttpStatusCode.ExpectationFailed, "Request failed with exception: ${result.exceptionOrNull()}")
            }
        }
    }

    private fun validateRequest(request: TxnSMSRequest) {
        if (request.content.isBlank()) throw IllegalArgumentException("Content cannot be empty")
    }
}