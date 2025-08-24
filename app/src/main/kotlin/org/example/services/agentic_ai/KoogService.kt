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
import org.example.models.Expense
import org.example.services.agentic_ai.tools.ExpenseExtractor
import org.example.services.config.ConfigService

class KoogService {
    private val config = ConfigService.getInstance()

    private val expenseStructure = JsonStructuredData.createJsonStructure<Expense>(
        schemaFormat = JsonSchemaGenerator.SchemaFormat.JsonSchema,
        schemaType = JsonStructuredData.JsonSchemaType.SIMPLE,
        examples = EXAMPLE_EXPENSES
    )

    val agentStrategy: AIAgentStrategy<String, Expense> = strategy("expense-extractor") {
        val setup by nodeLLMRequest()
        val getStructuredExpense by node<Message.Response, Expense> { _ ->
            try {
                val result = llm.writeSession {
                    this.requestLLMStructured(
                        structure = expenseStructure,
                        fixingModel = AnthropicModels.Opus_4
                    )
                }
                if (result.isSuccess) {
                    val structuredResponse = result.getOrNull()?.structure
                    structuredResponse?.let {
                        Expense(structuredResponse.amount, structuredResponse.name, structuredResponse.account)
                    }?: Expense("", "", "")
                } else Expense("", "", "")
            } catch (e: Exception) {
                Expense("", "", "")
            }
        }
        edge(nodeStart forwardTo setup)
        edge(setup forwardTo getStructuredExpense)
        edge(getStructuredExpense forwardTo nodeFinish)
    }
    
    private fun createAgent(): AIAgent<String, Expense> = AIAgent<String, Expense>(
        promptExecutor = simpleAnthropicExecutor(config.anthropicApiKey),
        strategy = agentStrategy,
        agentConfig = AIAgentConfig(model = AnthropicModels.Opus_4, maxAgentIterations = 10, prompt = prompt("system-prompt") { system("You are a helpful assistant. You can perfectly understand any text and can extract useful information from them in the format" +
            "requested by the user.") }),
        toolRegistry = ToolRegistry { tool(ExpenseExtractor()) }
    ) {
        handleEvents {
            onToolCall { eventContext ->
                println("Tool ${eventContext.tool} called with args: ${eventContext.toolArgs}")
            }
            onAgentFinished {eventContext ->
                println("Agent finished with result ${eventContext.result}")
            }
        }
    }

    suspend fun analyseSMS(sms: String): Expense = createAgent().run(sms)

    companion object {
        private val EXAMPLE_EXPENSES = listOf<Expense>()


        private var instance: KoogService? = null

        fun getInstance(): KoogService {
            if (instance == null) {
                instance = KoogService()
            }
            return instance!!
        }
    }
}