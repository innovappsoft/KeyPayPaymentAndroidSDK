package com.innovappsoft.keypay.payment

import android.app.Activity
import android.content.Intent
import android.net.Uri

/**
 * Main public entry point for the KeyPay Android SDK.
 *
 * Kotlin:
 * KeyPay.configure(configuration)
 * KeyPay.startCheckout(activity, request) { result -> ... }
 *
 * Java:
 * KeyPay.configure(configuration);
 * KeyPay.startCheckout(activity, request, result -> { ... });
 */
object KeyPay {
    @Volatile
    private var configuration: KeyPayConfiguration? = null

    @JvmStatic
    fun configure(configuration: KeyPayConfiguration) {
        this.configuration = configuration
    }

    @JvmStatic
    fun getConfiguration(): KeyPayConfiguration? = configuration

    @JvmStatic
    fun requireConfiguration(): KeyPayConfiguration {
        return configuration ?: throw KeyPayCheckoutException(
            "not_configured",
            "KeyPay.configure(...) must be called before starting checkout."
        )
    }

    @JvmStatic
    fun startCheckout(
        activity: Activity,
        request: KeyPayPaymentRequest,
        callback: KeyPayResultCallback
    ) {
        requireConfiguration()
        KeyPayCheckoutSession.callback = callback
        KeyPayCheckoutSession.latestReturnUri = null
        KeyPayCheckoutSession.latestIntent = null

        val intent = Intent(activity, KeyPayCheckoutActivity::class.java).apply {
            putExtra(KeyPayCheckoutActivity.EXTRA_AMOUNT, request.amount)
            putExtra(KeyPayCheckoutActivity.EXTRA_TITLE, request.title)
            putExtra(KeyPayCheckoutActivity.EXTRA_DESCRIPTION, request.description)
            putExtra(KeyPayCheckoutActivity.EXTRA_EXTERNAL_REFERENCE, request.externalReference)
            putExtra(KeyPayCheckoutActivity.EXTRA_CURRENCY, request.currency)
            putExtra(KeyPayCheckoutActivity.EXTRA_METADATA_KEYS, request.metadata.keys.toTypedArray())
            putExtra(KeyPayCheckoutActivity.EXTRA_METADATA_VALUES, request.metadata.values.toTypedArray())
        }
        activity.startActivity(intent)
    }

    @JvmStatic
    fun handleReturnUri(uri: Uri) {
        KeyPayCheckoutSession.latestReturnUri = uri
    }
}
