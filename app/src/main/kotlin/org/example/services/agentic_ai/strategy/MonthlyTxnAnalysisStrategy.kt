package org.example.services.agentic_ai.strategy

import ai.koog.agents.core.agent.entity.AIAgentStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.prompt.message.Message

class MonthlyTxnAnalysisStrategy {
    fun create(): AIAgentStrategy<String, String> = strategy("monthly-txn-analysis") {
        val nodeCallLLM by nodeLLMRequest()
        val processResponse by node<Message.Response, String> { response ->
            response.content
        }
        edge(nodeStart forwardTo nodeCallLLM)
        edge(nodeCallLLM forwardTo processResponse)
        edge(processResponse forwardTo nodeFinish)
    }
}