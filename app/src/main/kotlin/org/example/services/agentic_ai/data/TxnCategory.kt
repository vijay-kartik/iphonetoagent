package org.example.services.agentic_ai.data

enum class TxnCategory {
    Food, Clothing, Flights, Transportation, Miscellaneous, //Income
    Salary, Dividend, Transfer; //Expense

    companion object {
        fun from(value: String): TxnCategory {
            return when (value) {
                "Food" -> Food
                "Clothing" -> Clothing
                "Flights" -> Flights
                "Transportation" -> Transportation
                "Salary" -> Salary
                "Dividend" -> Dividend
                "Transfer" -> Transfer
                else -> Miscellaneous
            }
        }
    }
}