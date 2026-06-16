package com.innovappsoft.keypay.payment

/**
 * Global SDK configuration.
 *
 * Developers should point these URLs to their own backend, never directly place
 * the KeyPay developer secret key inside an Android application.
 */
class KeyPayConfiguration @JvmOverloads constructor(
    val paymentIntentEndpoint: String,
    val paymentStatusEndpoint: String,
    val returnUrlScheme: String,
    val merchantDisplayName: String,
    val requestTimeoutMillis: Int = 30_000
) {
    init {
        require(paymentIntentEndpoint.isNotBlank()) { "paymentIntentEndpoint is required" }
        require(paymentStatusEndpoint.isNotBlank()) { "paymentStatusEndpoint is required" }
        require(returnUrlScheme.isNotBlank()) { "returnUrlScheme is required" }
        require(merchantDisplayName.isNotBlank()) { "merchantDisplayName is required" }
    }
}
