package org.example.routes.response

import kotlinx.serialization.Serializable

@Serializable
data class TxnSmsResponse(
    val status: String,
    val message: String
)