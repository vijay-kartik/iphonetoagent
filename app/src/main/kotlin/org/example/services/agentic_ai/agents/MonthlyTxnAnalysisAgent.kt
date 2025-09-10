package org.example.services.agentic_ai.agents

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import org.example.services.agentic_ai.prompts.TxnPrompts
import org.example.services.agentic_ai.strategy.MonthlyTxnAnalysisStrategy
import org.example.services.config.ConfigService

class MonthlyTxnAnalysisAgent: AgentBuilder<String, String> {
    override fun create(config: ConfigService): AIAgent<String, String> {
        return AIAgent<String, String>(
            promptExecutor = simpleAnthropicExecutor(config.anthropicApiKey),
            strategy = MonthlyTxnAnalysisStrategy().create(),
            agentConfig = createAgentConfig()
        )
    }

    private fun createAgentConfig(): AIAgentConfig {
        return AIAgentConfig(
            model = AnthropicModels.Haiku_3_5,
            maxAgentIterations = 10,
            prompt = prompt("monthly-txn-analysis-prompt") {
                system(TxnPrompts.MONTHLY_TXN_ANALYSIS_SYSTEM_PROMPT)
            }
        )
    }
}