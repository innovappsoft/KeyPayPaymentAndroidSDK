package com.innovappsoft.keypay.payment

/**
 * Callback interface intentionally kept simple for both Kotlin and Java callers.
 */
fun interface KeyPayResultCallback {
    fun onResult(result: KeyPayPaymentResult)
}
