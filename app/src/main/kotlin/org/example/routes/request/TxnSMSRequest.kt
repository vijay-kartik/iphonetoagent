package org.example.routes.request

import kotlinx.serialization.Serializable

@Serializable
data class TxnSMSRequest(
    val content: String
)