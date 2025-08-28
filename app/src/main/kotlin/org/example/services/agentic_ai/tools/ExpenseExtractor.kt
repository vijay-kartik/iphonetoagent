package org.example.services.agentic_ai.tools

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.KSerializer
import org.example.services.agentic_ai.data.Transaction
import org.example.services.agentic_ai.data.TransactionArgs

object ExpenseExtractor: Tool<TransactionArgs, Transaction>() {
    override val argsSerializer: KSerializer<TransactionArgs>
        get() = TransactionArgs.serializer()

    override val descriptor: ToolDescriptor = ToolDescriptor(
        name = "expense_extractor",
        description = "Tool for extracting transaction details from SMS text",
        requiredParameters = listOf(
            ToolParameterDescriptor(name = "date", description = "Date on which the transaction is performed", type = ToolParameterType.String),
            ToolParameterDescriptor(name = "detail", description = "Person or entity name the transaction is credited to or done for", type = ToolParameterType.String),
            ToolParameterDescriptor(name = "amount_inr", description = "Amount of transaction done in currency INR.", type = ToolParameterType.String),
            ToolParameterDescriptor(name = "amount_usd", description = "Amount of transaction done in currency USD", type = ToolParameterType.String),
            ToolParameterDescriptor(name = "type", description = "Type of transaction", type = ToolParameterType.String),
        )
    )

    override suspend fun execute(args: TransactionArgs): Transaction {
        val expense = Transaction(amount_inr = args.amount_inr, amount_usd = args.amount_usd, detail = args.detail, date = args.date, type = args.type)
        return expense
    }
}