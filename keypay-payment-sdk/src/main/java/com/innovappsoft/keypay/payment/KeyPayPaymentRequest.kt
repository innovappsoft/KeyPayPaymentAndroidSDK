package com.innovappsoft.keypay.payment

/**
 * Payment request sent to the developer backend.
 *
 * The backend receives this payload, creates a KeyPay PaymentIntent with its
 * private dk_live key, then returns the PaymentIntent payload to the SDK.
 */
class KeyPayPaymentRequest @JvmOverloads constructor(
    val amount: String,
    val title: String,
    val description: String? = null,
    val externalReference: String? = null,
    val currency: String = "KCOIN",
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(amount.isNotBlank()) { "amount is required" }
        require(title.isNotBlank()) { "title is required" }
        require(currency.isNotBlank()) { "currency is required" }
    }
}
