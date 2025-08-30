package org.example.services.agentic_ai.factory

import org.example.services.agentic_ai.data.Transaction
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AIAgentFactoryTest {

    @Test
    fun `factory should be singleton`() {
        val factory1 = AIAgentFactory.getInstance()
        val factory2 = AIAgentFactory.getInstance()
        
        assertTrue(factory1 === factory2, "Factory should return the same instance")
    }

    @Test
    fun `should create transaction analysis agent`() {
        val factory = AIAgentFactory.getInstance()
        val agent = factory.createTransactionAnalysisAgent()
        
        assertNotNull(agent, "Agent should not be null")
    }
    
    @Test
    fun `should create agent using generic method`() {
        val factory = AIAgentFactory.getInstance()
        val agent = factory.createAgent<String, Transaction>(AgentType.TRANSACTION_ANALYSIS)
        
        assertNotNull(agent, "Agent should not be null")
    }
}