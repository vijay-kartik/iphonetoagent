package org.example.services.agentic_ai.data

enum class TransactionType {
    INFLOW, OUTFLOW, CC_USAGE, NONE;

    companion object {
        fun fromString(value: String): TransactionType {
            return when(value) {
                "INFLOW" -> INFLOW
                "OUTFLOW" -> OUTFLOW
                "CC_USAGE" -> CC_USAGE
                else -> NONE
            }
        }
    }


}