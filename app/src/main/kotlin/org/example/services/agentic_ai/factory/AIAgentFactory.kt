package org.example.services.agentic_ai.factory

import ai.koog.agents.core.agent.AIAgent
import org.example.services.agentic_ai.agents.MonthlyTxnAnalysisAgent
import org.example.services.agentic_ai.agents.TransactionAnalysisAgent
import org.example.services.agentic_ai.data.Transaction
import org.example.services.config.ConfigService

/**
 * Factory for creating different types of AI agents based on use cases
 */
class AIAgentFactory private constructor() {
    
    private val config = ConfigService.getInstance()
    
    /**
     * Creates an AI agent based on the specified type
     */
    @Suppress("UNCHECKED_CAST")
    fun <Input, Output> createAgent(agentType: AgentType): AIAgent<Input, Output> {
        return when (agentType) {
            AgentType.TRANSACTION_ANALYSIS -> createTransactionAnalysisAgent() as AIAgent<Input, Output>
            AgentType.MONTHLY_TXN_ANALYSIS -> createMonthlyTxnAnalysisAgent() as AIAgent<Input, Output>
        }
    }

    fun createTransactionAnalysisAgent(): AIAgent<String, Transaction> {
        return TransactionAnalysisAgent().create(config)
    }

    fun createMonthlyTxnAnalysisAgent(): AIAgent<String, String> {
        return MonthlyTxnAnalysisAgent().create(config)
    }
    
    companion object {
        @Volatile
        private var instance: AIAgentFactory? = null
        
        fun getInstance(): AIAgentFactory {
            return instance ?: synchronized(this) {
                instance ?: AIAgentFactory().also { instance = it }
            }
        }
    }
}