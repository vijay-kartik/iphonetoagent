package org.example.models

import ai.koog.agents.core.tools.ToolArgs
import ai.koog.agents.core.tools.ToolResult
import com.google.gson.Gson
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val amount: String,
    val name: String,
    val account: String
): ToolArgs, ToolResult {
    override fun toStringDefault(): String {
        return Gson().toJson(this, Expense::class.java).toString()
    }
}
