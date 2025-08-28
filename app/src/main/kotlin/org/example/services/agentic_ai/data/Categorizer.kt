package org.example.services.agentic_ai.data

import ai.koog.agents.core.tools.ToolArgs
import kotlinx.serialization.Serializable


@Serializable
data class CategorizerArgs(
    val sms: String
): ToolArgs