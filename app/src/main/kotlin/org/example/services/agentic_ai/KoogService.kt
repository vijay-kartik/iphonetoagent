package org.example.services.agentic_ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.structure.json.JsonSchemaGenerator
import ai.koog.prompt.structure.json.JsonStructuredData
import kotlinx.coroutines.runBlocking
import org.example.services.agentic_ai.data.Transaction
import org.example.services.agentic_ai.prompts.TxnPrompts.TXN_SYSTEM_PROMPT
import org.example.services.agentic_ai.strategy.TransactionAnalysisStrategy
import org.example.services.agentic_ai.tools.ExpenseExtractor
import org.example.services.config.ConfigService

class KoogService {
    private val config = ConfigService.getInstance()

    private val transactionStructure = JsonStructuredData.createJsonStructure<Transaction>(
        schemaFormat = JsonSchemaGenerator.SchemaFormat.JsonSchema,
        schemaType = JsonStructuredData.JsonSchemaType.SIMPLE,
        examples = listOf()
    )

    // Create strategy using the extracted strategy class
    private val transactionAnalysisStrategy = TransactionAnalysisStrategy(transactionStructure)

    // Create the agent once and reuse it
    private val txnAgent: AIAgent<String, Transaction> by lazy {
        AIAgent<String, Transaction>(
            promptExecutor = simpleAnthropicExecutor(config.anthropicApiKey),
            strategy = transactionAnalysisStrategy.create(),
            agentConfig = AIAgentConfig(
                model = AnthropicModels.Haiku_3_5,
                maxAgentIterations = 10,
                prompt = prompt("system-prompt") { system(TXN_SYSTEM_PROMPT) }
            ),
            toolRegistry = ToolRegistry { tool(ExpenseExtractor) }
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

    internal fun analyseSMS(sms: String): Transaction {
        return synchronized(txnAgent) {
            runBlocking {
                txnAgent.run(sms)
            }
        }
    }

    companion object {
        private var instance: KoogService? = null

        fun getInstance(): KoogService {
            if (instance == null) {
                instance = KoogService()
            }
            return instance!!
        }
    }
}