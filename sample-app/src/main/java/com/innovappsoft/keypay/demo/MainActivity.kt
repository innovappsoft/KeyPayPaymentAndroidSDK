package com.innovappsoft.keypay.demo

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.innovappsoft.keypay.payment.KeyPay
import com.innovappsoft.keypay.payment.KeyPayCheckoutButton
import com.innovappsoft.keypay.payment.KeyPayConfiguration
import com.innovappsoft.keypay.payment.KeyPayPaymentRequest
import com.innovappsoft.keypay.payment.KeyPayPaymentResult

/**
 * Minimal sample app showing how a developer integrates the KeyPay SDK.
 */
class MainActivity : Activity() {
    private lateinit var statusView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KeyPay.configure(
            KeyPayConfiguration(
                paymentIntentEndpoint = "https://your-backend.com/api/keypay/payment-intents",
                paymentStatusEndpoint = "https://your-backend.com/api/keypay/payment-intents",
                returnUrlScheme = "keypaydemo",
                merchantDisplayName = "Demo Store"
            )
        )

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.rgb(245, 245, 249))
        }

        val title = TextView(this).apply {
            text = "KeyPay Android SDK"
            textSize = 26f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        root.addView(title)

        val request = KeyPayPaymentRequest(
            amount = "10.00",
            title = "Premium Plan",
            description = "Android SDK demo payment",
            externalReference = "order_1001",
            metadata = mapOf("order_id" to "1001")
        )

        val checkoutButton = KeyPayCheckoutButton(this).apply {
            configure(
                activity = this@MainActivity,
                request = request,
                callback = { result -> updateStatus(result) },
                title = "Pay 10 KCOIN"
            )
        }
        root.addView(checkoutButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(58)
        ).apply { topMargin = dp(28) })

        statusView = TextView(this).apply {
            text = "Waiting for payment…"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
        }
        root.addView(statusView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(18) })

        setContentView(root)
    }

    private fun updateStatus(result: KeyPayPaymentResult) {
        statusView.text = when (result.status) {
            KeyPayPaymentResult.Status.SUCCEEDED -> "Paid: ${result.intent?.id.orEmpty()}"
            KeyPayPaymentResult.Status.PENDING -> "Pending: ${result.intent?.id.orEmpty()}"
            KeyPayPaymentResult.Status.CANCELLED -> "Cancelled"
            KeyPayPaymentResult.Status.FAILED -> "Failed: ${result.error?.message.orEmpty()}"
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
