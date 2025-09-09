package org.example.services.agentic_ai.data

import ai.koog.agents.core.tools.ToolArgs
import ai.koog.agents.core.tools.ToolResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val date: String,
    val detail: String,
    @SerialName("Amount INR")
    val amount_inr: Double,
    @SerialName("Amount USD")
    val amount_usd: Double,
    val type: TransactionType,
    val category: TxnCategory,
    val account_name: String
): ToolResult {
    override fun toStringDefault(): String {
        return this.toString()
    }
}


@Serializable
data class TransactionArgs(
    val date: String,
    val detail: String,
    val amount_inr: Double,
    val amount_usd: Double,
    val type: TransactionType,
    val category: TxnCategory,
    val account_name: String
): ToolArgs

@Serializable
data class TransactionTypeVO(
    val type: String
): ToolResult {
    override fun toStringDefault(): String {
        return this.toString()
    }
}
