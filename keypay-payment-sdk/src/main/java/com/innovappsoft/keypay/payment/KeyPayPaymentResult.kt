package com.innovappsoft.keypay.payment

/**
 * Java-friendly result object returned by the SDK checkout flow.
 */
class KeyPayPaymentResult private constructor(
    val status: Status,
    val intent: KeyPayPaymentIntent?,
    val error: Throwable?
) {
    enum class Status {
        SUCCEEDED,
        PENDING,
        CANCELLED,
        FAILED
    }

    companion object {
        @JvmStatic
        fun succeeded(intent: KeyPayPaymentIntent) = KeyPayPaymentResult(Status.SUCCEEDED, intent, null)

        @JvmStatic
        fun pending(intent: KeyPayPaymentIntent) = KeyPayPaymentResult(Status.PENDING, intent, null)

        @JvmStatic
        fun cancelled() = KeyPayPaymentResult(Status.CANCELLED, null, null)

        @JvmStatic
        fun failed(error: Throwable) = KeyPayPaymentResult(Status.FAILED, null, error)
    }
}
