package org.example.services.agentic_ai.tools

import ai.koog.agents.core.tools.*
import kotlinx.serialization.KSerializer
import org.example.services.agentic_ai.data.TransactionArgs

object TransactionValidator: Tool<TransactionArgs, ToolResult.Boolean>() {
    override val argsSerializer: KSerializer<TransactionArgs>
        get() = TransactionArgs.serializer()

    override val descriptor: ToolDescriptor = ToolDescriptor(
        name = "transaction-detail-validator",
        description = "Tool for verifying that extracted details from a transaction sms are valid",
        requiredParameters = listOf(
            ToolParameterDescriptor(name = "date", description = "Date on which the transaction is performed", type = ToolParameterType.String),
            ToolParameterDescriptor(name = "detail", description = "Person or entity name the transaction is credited to or done for", type = ToolParameterType.String),
            ToolParameterDescriptor(name = "amount_inr", description = "Amount of transaction done in currency INR.", type = ToolParameterType.String),
            ToolParameterDescriptor(name = "amount_usd", description = "Amount of transaction done in currency USD", type = ToolParameterType.String),
            ToolParameterDescriptor(name = "type", description = "Type of transaction", type = ToolParameterType.String),
            ToolParameterDescriptor(name = "category", description = "Category the transaction is supposedly done in according to the type of transaction and sms text", type = ToolParameterType.String)
        )
    )

    override suspend fun execute(args: TransactionArgs): ToolResult.Boolean {
        return ToolResult.Boolean(args.date.length == 10)
    }
}