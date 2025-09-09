package org.example.services.agentic_ai.prompts

import ai.koog.prompt.dsl.prompt

object TxnPrompts {
    private const val TXN_STRUCTURED_RESPONSE = "Extract transaction data from the following response and return structured JSON."
    const val TXN_SYSTEM_PROMPT =
        """You are an expert in understanding Indian bank and credit card transaction SMS.
                        |
                        |When you receive an SMS text, you must:
                        |1. ALWAYS use the expense_extractor tool to extract transaction information
                        |2. Extract these details from the SMS:
                        |   - date: Transaction date (if not mentioned, use today's date) in dd/mm/yyyy format
                        |   - detail: Merchant, person, or transaction description
                        |   - amount_inr: Amount in Indian Rupees (if sms contains amount in any other category, convert it into INR)
                        |   - amount_usd: Amount in USD (if sms contains amount in any other category, convert it into USD)
                        |   - type: Transaction type - determine from context as INFLOW, OUTFLOW, or CC_USAGE
                        |   - category: The category of transaction. It derives from both the SMS text and the type of transaction it is classified into:
                        |   if type is OUTFLOW, which means it is an expense transaction, then category must lie into one of Food, Clothing, Flights, Transportation, Miscellaneous
                        |   else if type is INFLOW, which means it is an income transaction, then category must lie into one of Salary, Dividend, Transfer
                        |   
                        |   - account_name: The name of account which the transaction is done in.  
                        |   if type is OUTFLOW/CC_USAGE, then it is the account the amount is debited from, else it is the account to which amount is credited into.
                        |
                        |ALWAYS call the expense_extractor tool first before providing any other response."""
    fun extractStructured(user: String) = prompt(PromptIds.EXTRACT_TRANSACTION_STRUCTURED) {
        system(TXN_STRUCTURED_RESPONSE)
        user(user)
    }
}