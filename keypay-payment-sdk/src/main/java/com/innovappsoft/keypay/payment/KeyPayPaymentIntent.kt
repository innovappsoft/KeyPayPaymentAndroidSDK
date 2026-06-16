package com.innovappsoft.keypay.payment

/**
 * PaymentIntent returned by the developer backend.
 */
class KeyPayPaymentIntent @JvmOverloads constructor(
    val id: String,
    val status: String,
    val amount: String,
    val currency: String,
    val checkoutUrl: String,
    val clientSecret: String? = null,
    val paymentLinkId: String? = null,
    val externalReference: String? = null,
    val title: String? = null,
    val description: String? = null,
    val paidTransactionId: String? = null
)
