package com.innovappsoft.keypay.payment

import android.app.Activity
import android.os.Bundle

/**
 * Optional return Activity for checkout callbacks.
 *
 * Developers add an intent-filter for their custom return URL scheme pointing to
 * this Activity. The SDK stores the URL and the checkout Activity checks status.
 */
class KeyPayReturnActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.let { KeyPay.handleReturnUri(it) }
        finish()
    }
}
