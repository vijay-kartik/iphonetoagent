package org.example.services.agentic_ai.factory

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.structure.json.JsonSchemaGenerator
import ai.koog.prompt.structure.json.JsonStructuredData
import org.example.services.agentic_ai.data.Transaction
import org.example.services.agentic_ai.prompts.TxnPrompts.TXN_SYSTEM_PROMPT
import org.example.services.agentic_ai.strategy.TransactionAnalysisStrategy
import org.example.services.agentic_ai.tools.TransactionValidator
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
            // Future agent types can be handled here
        }
    }
    
    /**
     * Creates a transaction analysis agent for SMS processing
     */
    fun createTransactionAnalysisAgent(): AIAgent<String, Transaction> {
        val transactionStructure = JsonStructuredData.createJsonStructure<Transaction>(
            schemaFormat = JsonSchemaGenerator.SchemaFormat.JsonSchema,
            schemaType = JsonStructuredData.JsonSchemaType.SIMPLE,
            examples = listOf()
        )
        
        val strategy = TransactionAnalysisStrategy(transactionStructure)
        
        return AIAgent<String, Transaction>(
            promptExecutor = simpleAnthropicExecutor(config.anthropicApiKey),
            strategy = strategy.create(),
            agentConfig = AIAgentConfig(
                model = AnthropicModels.Haiku_3_5,
                maxAgentIterations = 10,
                prompt = prompt("system-prompt") { system(TXN_SYSTEM_PROMPT) }
            ),
            toolRegistry = ToolRegistry { tool(TransactionValidator) }
        ) {
            handleEvents {
                onToolCall { eventContext ->
                    println("Tool ${eventContext.tool} called with args: ${eventContext.toolArgs}")
                }
                onAgentFinished { eventContext ->
                    println("Agent finished with result ${eventContext.result}")
                }
            }
        }
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