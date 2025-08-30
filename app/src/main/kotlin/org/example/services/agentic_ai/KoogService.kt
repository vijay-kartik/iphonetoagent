package org.example.services.agentic_ai

import ai.koog.agents.core.agent.AIAgent
import kotlinx.coroutines.runBlocking
import org.example.services.agentic_ai.factory.AIAgentFactory
import org.example.services.agentic_ai.factory.AgentType

class KoogService {
    private val agentFactory = AIAgentFactory.getInstance()

    private fun <Input, Output> getAgent(agentType: AgentType): AIAgent<Input, Output> {
        return agentFactory.createAgent(agentType)
    }

    fun <Input, Output> processWithAgent(agentType: AgentType, input: Input): Output {
        val agent = getAgent<Input, Output>(agentType)
        return synchronized(agent) {
            runBlocking {
                agent.run(input)
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