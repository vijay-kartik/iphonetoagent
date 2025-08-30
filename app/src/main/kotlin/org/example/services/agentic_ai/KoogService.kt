package org.example.services.agentic_ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.message.Message
import ai.koog.prompt.structure.json.JsonSchemaGenerator
import ai.koog.prompt.structure.json.JsonStructuredData
import kotlinx.coroutines.runBlocking
import org.example.services.agentic_ai.data.Transaction
import org.example.services.agentic_ai.data.TransactionType
import org.example.services.agentic_ai.data.TxnCategory
import org.example.services.agentic_ai.tools.ExpenseExtractor
import org.example.services.config.ConfigService

class KoogService {
    private val config = ConfigService.getInstance()

    private val transactionStructure = JsonStructuredData.createJsonStructure<Transaction>(
        schemaFormat = JsonSchemaGenerator.SchemaFormat.JsonSchema,
        schemaType = JsonStructuredData.JsonSchemaType.SIMPLE,
        examples = listOf()
    )

    val transactionAgentStrategy: AIAgentStrategy<String, Transaction> = strategy("sms-analyser") {
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
                    prompt = prompt("extract-transaction") {
                        system("Extract transaction data from the following response and return structured JSON.")
                        user("Response: $content")
                    }
                    this.requestLLMStructured(
                        structure = transactionStructure,
                        fixingModel = AnthropicModels.Haiku_3_5
                    )
                }
                
                if (result.isSuccess) {
                    result.getOrNull()?.structure as Transaction
                } else {
                    Transaction("", "", Double.NaN, Double.NaN, TransactionType.INFLOW, TxnCategory.Miscellaneous)
                }
            } catch (e: Exception) {
                Transaction("", "", Double.NaN, Double.NaN, TransactionType.INFLOW, TxnCategory.Miscellaneous)
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

    // Create the agent once and reuse it
    private val agent: AIAgent<String, Transaction> by lazy {
        AIAgent<String, Transaction>(
            promptExecutor = simpleAnthropicExecutor(config.anthropicApiKey),
            strategy = transactionAgentStrategy,
            agentConfig = AIAgentConfig(
                model = AnthropicModels.Haiku_3_5,
                maxAgentIterations = 10,
                prompt = prompt("system-prompt") {
                    system(
                        """You are an expert in understanding Indian bank and credit card transaction SMS.
                        |
                        |When you receive an SMS text, you must:
                        |1. ALWAYS use the expense_extractor tool to extract transaction information
                        |2. Extract these details from the SMS:
                        |   - date: Transaction date (if not mentioned, use today's date in DD/MM/YYYY format)
                        |   - detail: Merchant, person, or transaction description
                        |   - amount_inr: Amount in Indian Rupees (convert from text, remove currency symbols)
                        |   - amount_usd: Amount in USD (usually 0.0 for Indian transactions)
                        |   - type: Transaction type - determine from context as INFLOW, OUTFLOW, or CC_USAGE
                        |   - category: The category of transaction. It derives from both the SMS text and the type of transaction it is classified into:
                        |   if type is OUTFLOW, which means it is an expense transaction, then category must lie into one of Food, Clothing, Flights, Transportation, Miscellaneous
                        |   else if type is INFLOW, which means it is an income transaction, then category must lie into one of Salary, Dividend, Transfer
                        |
                        |ALWAYS call the expense_extractor tool first before providing any other response.""".trimMargin()
                    )
                }),
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

    fun analyseSMS(sms: String): Transaction {
        // Use synchronized block to handle potential concurrency issues
        return synchronized(agent) {
            runBlocking {
                agent.run(sms)
            }
        }
    }

    companion object {
        private val SYSTEM_PROMPT =
            "You are an expert in understanding text of sms related to indian banks and credit card transactions"


        private var instance: KoogService? = null

        fun getInstance(): KoogService {
            if (instance == null) {
                instance = KoogService()
            }
            return instance!!
        }
    }
}