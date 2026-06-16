package com.innovappsoft.keypay.payment

/**
 * SDK checkout error with a stable code developers can inspect.
 */
class KeyPayCheckoutException(
    val code: String,
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause)
