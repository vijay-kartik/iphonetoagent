package org.example.services.agentic_ai.tools

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.KSerializer
import org.example.models.Expense

class ExpenseExtractor: ai.koog.agents.core.tools.Tool<Expense, Expense>() {
    override val argsSerializer: KSerializer<Expense>
        get() = Expense.serializer()

    //    override val argsSerializer: KSerializer<Expense>,
    override val descriptor: ToolDescriptor = ToolDescriptor(
        name = "expense_extractor",
        description = "Tool for extracting expense information from a transaction SMS",
        requiredParameters = listOf(
            ToolParameterDescriptor(name = "amount", description = "Amount mentioned in the text along with currency", type = ToolParameterType.String),
            ToolParameterDescriptor(name = "name", description = "Person or entity name the transaction is credited to or done for.", type = ToolParameterType.String),
            ToolParameterDescriptor(name = "account", description = "Account from which money is debited or transaction is done", type = ToolParameterType.String),
        )
    )

    override suspend fun execute(args: Expense): Expense {
        val expense = Expense(amount = args.amount, account = args.account, name = args.name)
        return expense
    }
}