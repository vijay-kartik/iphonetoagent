package org.example.services.agentic_ai.strategy

import ai.koog.agents.core.agent.entity.AIAgentStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.message.Message
import ai.koog.prompt.structure.StructuredData
import org.example.services.agentic_ai.data.Transaction
import org.example.services.agentic_ai.data.TransactionType
import org.example.services.agentic_ai.data.TxnCategory
import org.example.services.agentic_ai.prompts.TxnPrompts

class TransactionAnalysisStrategy(
    private val transactionStructure: StructuredData<Transaction>
) {
    
    fun create(): AIAgentStrategy<String, Transaction> = strategy("sms-analyser") {
        // Initial LLM request node
        val nodeCallLLM by nodeLLMRequest()
        
        // Tool execution nodes
        val validateResult by nodeExecuteTool("transaction-detail-validator")
        val sendToolResult by nodeLLMSendToolResult()
        
        // Processing node to handle validated results
        val processValidatedResult by node<Message.Response, Transaction> { response ->
            try {
                // Parse the response which contains both original LLM output and validation result
                val content = response.content
                
                // Extract structured Transaction data
                val result = llm.writeSession {
                    prompt = TxnPrompts.extractStructured("Response: $content")
                    this.requestLLMStructured(
                        structure = transactionStructure,
                        fixingModel = AnthropicModels.Haiku_3_5
                    )
                }
                
                if (result.isSuccess) {
                    result.getOrNull()?.structure as Transaction
                } else {
                    createFallbackTransaction()
                }
            } catch (e: Exception) {
                createFallbackTransaction()
            }
        }
        
        // Fallback processing for invalid data
        val processInvalidResult by node<Message.Response, Transaction> { response ->
            // Return a failed transaction for invalid data
            Transaction("INVALID", "VALIDATION_FAILED", 0.0, 0.0, TransactionType.NONE, TxnCategory.Miscellaneous)
        }
        
        // Define the flow with proper validation routing
        edge(nodeStart forwardTo nodeCallLLM)
        edge(nodeCallLLM forwardTo validateResult onToolCall { true })
        edge(nodeCallLLM forwardTo processValidatedResult) // Direct path if no tool call
        edge(validateResult forwardTo sendToolResult)
        edge(sendToolResult forwardTo processValidatedResult) // Process if validation passes
        edge(processValidatedResult forwardTo nodeFinish)
        edge(processInvalidResult forwardTo nodeFinish)
    }
    
    private fun createFallbackTransaction(): Transaction {
        return Transaction("", "", Double.NaN, Double.NaN, TransactionType.INFLOW, TxnCategory.Miscellaneous)
    }
}