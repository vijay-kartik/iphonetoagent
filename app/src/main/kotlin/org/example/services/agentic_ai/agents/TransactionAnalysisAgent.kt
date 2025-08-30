package org.example.services.agentic_ai.agents

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.structure.StructuredData
import ai.koog.prompt.structure.json.JsonSchemaGenerator
import ai.koog.prompt.structure.json.JsonStructuredData
import org.example.services.agentic_ai.data.Transaction
import org.example.services.agentic_ai.prompts.TxnPrompts.TXN_SYSTEM_PROMPT
import org.example.services.agentic_ai.strategy.TransactionAnalysisStrategy
import org.example.services.agentic_ai.tools.TransactionValidator
import org.example.services.config.ConfigService

/**
 * Dedicated agent builder for transaction analysis from SMS messages.
 * This class encapsulates all the configuration and setup specific to transaction analysis.
 */
class TransactionAnalysisAgent : AgentBuilder<String, Transaction> {
    
    /**
     * Creates and configures a transaction analysis AI agent
     */
    override fun create(config: ConfigService): AIAgent<String, Transaction> {
            val transactionStructure = createTransactionStructure()
            val strategy = TransactionAnalysisStrategy(transactionStructure)
            
            return AIAgent<String, Transaction>(
                promptExecutor = simpleAnthropicExecutor(config.anthropicApiKey),
                strategy = strategy.create(),
                agentConfig = createAgentConfig(),
                toolRegistry = createToolRegistry()
            ) {
                handleEvents {
                    onToolCall { eventContext ->
                        println("Transaction Analysis Tool ${eventContext.tool} called with args: ${eventContext.toolArgs}")
                    }
                    onAgentFinished { eventContext ->
                        println("Transaction Analysis Agent finished with result: ${eventContext.result}")
                    }
                }
            }
        }
    
    private fun createTransactionStructure(): StructuredData<Transaction> {
        return JsonStructuredData.createJsonStructure<Transaction>(
            schemaFormat = JsonSchemaGenerator.SchemaFormat.JsonSchema,
            schemaType = JsonStructuredData.JsonSchemaType.SIMPLE,
            examples = listOf()
        )
    }
    
    private fun createAgentConfig(): AIAgentConfig {
        return AIAgentConfig(
            model = AnthropicModels.Haiku_3_5,
            maxAgentIterations = 10,
            prompt = prompt("transaction-analysis-prompt") { 
                system(TXN_SYSTEM_PROMPT) 
            }
        )
    }
    
    private fun createToolRegistry(): ToolRegistry {
        return ToolRegistry { 
            tool(TransactionValidator) 
        }
    }
}