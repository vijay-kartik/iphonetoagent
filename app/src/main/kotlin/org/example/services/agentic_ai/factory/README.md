# AI Agent Factory Pattern

This package implements the Factory Pattern for creating different types of AI agents based on use cases.

## Architecture

### Components

1. **`AgentType`** - Enum defining available agent types
2. **`AIAgentFactory`** - Factory class for creating agents
3. **`KoogService`** - Service class that uses the factory

### Benefits

- **Separation of Concerns**: Agent creation logic is isolated from business logic
- **Extensibility**: Easy to add new agent types without modifying existing code
- **Type Safety**: Enum-based agent type selection prevents errors
- **Singleton Pattern**: Factory instance is shared across the application
- **Lazy Loading**: Agents are created only when needed

## Usage Examples

### Current Usage (Transaction Analysis)
```kotlin
val koogService = KoogService.getInstance()

// Using the dedicated method
val transaction = koogService.analyseSMS("Your SMS content here")

// Using the generic method
val transaction = koogService.processWithAgent<String, Transaction>(
    AgentType.TRANSACTION_ANALYSIS, 
    "Your SMS content here"
)
```

### Future Usage Examples

```kotlin
// Document Analysis (when implemented)
val documentSummary = koogService.processWithAgent<String, DocumentAnalysis>(
    AgentType.DOCUMENT_ANALYSIS,
    "Document content here"
)

// Code Review (when implemented)
val codeReview = koogService.processWithAgent<String, CodeReview>(
    AgentType.CODE_REVIEW,
    "Code changes here"
)

// Customer Service (when implemented)
val response = koogService.processWithAgent<String, CustomerResponse>(
    AgentType.CUSTOMER_SERVICE,
    "Customer query here"
)
```

## Adding New Agent Types

### Step 1: Add to AgentType enum
```kotlin
enum class AgentType {
    TRANSACTION_ANALYSIS,
    DOCUMENT_ANALYSIS,  // Add new type
    // ...
}
```

### Step 2: Add creation method to factory
```kotlin
fun createDocumentAnalysisAgent(): AIAgent<String, DocumentAnalysis> {
    // Create strategy, tools, prompts specific to document analysis
    return AIAgent(
        promptExecutor = simpleAnthropicExecutor(config.anthropicApiKey),
        strategy = documentAnalysisStrategy.create(),
        agentConfig = AIAgentConfig(
            model = AnthropicModels.Haiku_3_5,
            prompt = prompt("doc-analysis") { system("Document analysis prompt...") }
        ),
        toolRegistry = ToolRegistry { tool(DocumentAnalyzer) }
    )
}
```

### Step 3: Update factory's createAgent method
```kotlin
@Suppress("UNCHECKED_CAST")
fun <Input, Output> createAgent(agentType: AgentType): AIAgent<Input, Output> {
    return when (agentType) {
        AgentType.TRANSACTION_ANALYSIS -> createTransactionAnalysisAgent() as AIAgent<Input, Output>
        AgentType.DOCUMENT_ANALYSIS -> createDocumentAnalysisAgent() as AIAgent<Input, Output>
        // Add more cases as needed
    }
}
```

## Design Patterns Used

1. **Factory Pattern** - `AIAgentFactory` creates different agent types
2. **Singleton Pattern** - Single factory instance across application
3. **Builder Pattern** - Used within agent creation for configuration
4. **Strategy Pattern** - Different strategies for different agent types
5. **Lazy Initialization** - Agents created only when first accessed

## Thread Safety

- Factory uses double-checked locking for thread-safe singleton
- Individual agents are synchronized during execution
- Each agent type can have its own synchronization strategy if needed

## Performance Considerations

- Agents are cached and reused (lazy initialization)
- Factory instance is cached globally
- Synchronization is applied per agent instance, not globally
- Agent creation is one-time cost per type

This pattern provides a clean, extensible architecture for managing multiple AI agents in the application.