package com.innovappsoft.keypay.payment

import android.net.Uri

/**
 * In-memory checkout session bridge used by the SDK Activity and return Activity.
 */
internal object KeyPayCheckoutSession {
    var callback: KeyPayResultCallback? = null
    var latestReturnUri: Uri? = null
    var latestIntent: KeyPayPaymentIntent? = null

    fun complete(result: KeyPayPaymentResult) {
        val pendingCallback = callback
        callback = null
        latestReturnUri = null
        latestIntent = null
        pendingCallback?.onResult(result)
    }
}
