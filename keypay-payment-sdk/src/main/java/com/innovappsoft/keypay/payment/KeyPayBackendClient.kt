package com.innovappsoft.keypay.payment

import android.net.Uri
import org.json.JSONObject

/**
 * Talks to the developer backend.
 *
 * The developer backend is responsible for authenticating with KeyPay using its
 * private API key and returning the PaymentIntent response to this SDK.
 */
internal class KeyPayBackendClient(private val configuration: KeyPayConfiguration) {
    private val httpClient = KeyPayHttpClient(configuration.requestTimeoutMillis)

    fun createPaymentIntent(request: KeyPayPaymentRequest): KeyPayPaymentIntent {
        val body = JSONObject().apply {
            put("amount", request.amount)
            put("currency", request.currency)
            put("title", request.title)
            request.description?.let { put("description", it) }
            request.externalReference?.let { put("external_reference", it) }
            put("return_url", "${configuration.returnUrlScheme}://keypay-return")
            put("platform", "android")
            put("metadata", JSONObject(request.metadata))
        }

        val response = httpClient.postJson(configuration.paymentIntentEndpoint, body.toString())
        return parseIntentEnvelope(response)
    }

    fun fetchPaymentIntent(intentId: String): KeyPayPaymentIntent {
        val endpoint = configuration.paymentStatusEndpoint.trimEnd('/') + "/" + Uri.encode(intentId)
        val response = httpClient.get(endpoint)
        return parseIntentEnvelope(response)
    }

    private fun parseIntentEnvelope(response: String): KeyPayPaymentIntent {
        val root = JSONObject(response)
        val success = root.optBoolean("success", true)
        if (!success) {
            val message = root.optString("message", "Unable to process KeyPay response")
            throw KeyPayCheckoutException("backend_error", message)
        }

        val data = root.optJSONObject("data") ?: root
        return KeyPayPaymentIntent(
            id = data.optString("id"),
            status = data.optString("status"),
            amount = data.optString("amount"),
            currency = data.optString("currency", "KCOIN"),
            checkoutUrl = data.optString("checkout_url"),
            clientSecret = data.optStringOrNull("client_secret"),
            paymentLinkId = data.optStringOrNull("payment_link_id"),
            externalReference = data.optStringOrNull("external_reference"),
            title = data.optStringOrNull("title"),
            description = data.optStringOrNull("description"),
            paidTransactionId = data.optStringOrNull("paid_transaction_id")
        )
    }

    private fun JSONObject.optStringOrNull(name: String): String? {
        if (isNull(name)) return null
        return optString(name).takeIf { it.isNotBlank() }
    }
}
