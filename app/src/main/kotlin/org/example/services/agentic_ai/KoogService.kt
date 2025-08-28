package org.example.services.agentic_ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
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
import org.example.services.agentic_ai.data.TransactionTypeVO
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
        // this method sends the user message to LLM for getting response or calling appropriate tool next.
        val setup by nodeLLMRequest()
        val categorizeSMS by node<Message, Pair<TransactionType, String>> { smsText ->
            // Use the llm object to classify the smsText into a TransactionType
            val txnType: TransactionType = llm.writeSession {
                prompt = prompt("categorise-sms") {
                    system(SYSTEM_PROMPT)
                    user("Classify the following SMS as INFLOW, OUTFLOW, or CC_USAGE.\n\nSMS: $smsText")

                }
                val response = this.requestLLMStructured(
                    structure = JsonStructuredData.createJsonStructure<TransactionTypeVO>(),                // Structure expected: TransactionType
                    fixingModel = AnthropicModels.Opus_4            // Or your preferred LLM model
                )
                // Expecting something like: { "type": "INFLOW" }
                if (response.isSuccess) TransactionType.fromString(response.getOrNull()!!.structure.type)
                else TransactionType.NONE
            }
            Pair(txnType, smsText.content)
        }

        val appendTransactionType by node<Pair<TransactionType, String>, String> { smsWithType ->
            """You are an expert in extracting transaction information from Indian bank and credit card SMS.
                |
                |INSTRUCTIONS:
                |1. Use the expense_extractor tool to extract structured transaction details
                |2. Extract: date, detail (merchant/person), amount_inr, amount_usd, type
                |3. If date is not mentioned, use today's date in DD/MM/YYYY format
                |4. Convert amounts to proper numbers (remove currency symbols)
                |5. The transaction type is: ${smsWithType.first}
                |
                |SMS TEXT:
                |${smsWithType.second}
                |
                |Please use the expense_extractor tool to extract this information.""".trimMargin()
        }

        val extractExpense by nodeLLMRequest()

        val getStructuredExpense by node<Message.Response, Transaction> { smsWithCategory ->
            try {
                val result = llm.writeSession {
                    this.requestLLMStructured(
                        structure = transactionStructure,
                        fixingModel = AnthropicModels.Opus_4
                    )
                }
                if (result.isSuccess) {
                    result.getOrNull()?.structure as Transaction
                } else Transaction("", "", Double.NaN, Double.NaN, TransactionType.INFLOW)
            } catch (e: Exception) {
                Transaction("", "", Double.NaN, Double.NaN, TransactionType.INFLOW)
            }
        }
        edge(nodeStart forwardTo setup)
        edge(setup forwardTo categorizeSMS)
        edge(categorizeSMS forwardTo appendTransactionType)
        edge(appendTransactionType forwardTo extractExpense)
        edge(extractExpense forwardTo getStructuredExpense)
        edge(getStructuredExpense forwardTo nodeFinish)
    }

    // Create the agent once and reuse it
    private val agent: AIAgent<String, Transaction> by lazy {
        AIAgent<String, Transaction>(
            promptExecutor = simpleAnthropicExecutor(config.anthropicApiKey),
            strategy = transactionAgentStrategy,
            agentConfig = AIAgentConfig(
                model = AnthropicModels.Opus_4,
                maxAgentIterations = 10,
                prompt = prompt("system-prompt") {
                    system(
                        """You are an expert in understanding Indian bank and credit card transaction SMS.
                        |
                        |You have access to an expense_extractor tool that helps you extract structured transaction information.
                        |When asked to extract transaction details, always use the expense_extractor tool.
                        |
                        |The tool requires these parameters:
                        |- date: Transaction date in DD/MM/YYYY format
                        |- detail: Merchant or person name
                        |- amount_inr: Amount in Indian Rupees
                        |- amount_usd: Amount in US Dollars (0.0 if not applicable)
                        |- type: Transaction type (INFLOW, OUTFLOW, CC_USAGE)""".trimMargin()
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