package org.example.services.agentic_ai.agents

import ai.koog.agents.core.agent.AIAgent
import org.example.services.config.ConfigService

/**
 * Base interface for all agent builders.
 * Each specific agent type should implement this interface to ensure consistent creation patterns.
 */
interface AgentBuilder<Input, Output> {
    /**
     * Creates and configures an AI agent of the specific type
     * @param config Configuration service instance (optional, defaults to singleton)
     * @return Configured AI agent ready for use
     */
    fun create(config: ConfigService = ConfigService.getInstance()): AIAgent<Input, Output>
}