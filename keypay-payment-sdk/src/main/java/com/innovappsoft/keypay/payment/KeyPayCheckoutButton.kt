package com.innovappsoft.keypay.payment

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

/**
 * Drop-in checkout button for Kotlin, Java or XML based Android screens.
 *
 * Developers can configure the button once and let it open the full KeyPay
 * payment sheet when tapped.
 */
class KeyPayCheckoutButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {
    private var hostActivity: Activity? = context as? Activity
    private var paymentRequest: KeyPayPaymentRequest? = null
    private var resultCallback: KeyPayResultCallback? = null

    init {
        text = context.getString(R.string.keypay_button_default)
        textSize = 17f
        typeface = Typeface.DEFAULT_BOLD
        transformationMethod = null
        setTextColor(Color.WHITE)
        setPadding(dp(18), 0, dp(18), 0)
        minHeight = dp(56)
        background = KeyPayRoundedDrawable(Color.BLACK, dp(28).toFloat())
        super.setOnClickListener { startCheckoutOrFail() }
    }

    /**
     * Configures the button in one call.
     */
    @JvmOverloads
    fun configure(
        activity: Activity,
        request: KeyPayPaymentRequest,
        callback: KeyPayResultCallback,
        title: String? = null
    ) {
        hostActivity = activity
        paymentRequest = request
        resultCallback = callback
        title?.let { text = it }
    }

    /**
     * Sets or replaces the Activity used to launch the checkout sheet.
     */
    fun setHostActivity(activity: Activity) {
        hostActivity = activity
    }

    /**
     * Sets or replaces the payment request.
     */
    fun setPaymentRequest(request: KeyPayPaymentRequest) {
        paymentRequest = request
    }

    /**
     * Sets or replaces the checkout completion callback.
     */
    fun setResultCallback(callback: KeyPayResultCallback) {
        resultCallback = callback
    }

    private fun startCheckoutOrFail() {
        val activity = hostActivity
        val request = paymentRequest
        val callback = resultCallback

        if (activity == null || request == null || callback == null) {
            resultCallback?.onResult(
                KeyPayPaymentResult.failed(
                    KeyPayCheckoutException(
                        "button_not_ready",
                        context.getString(R.string.keypay_error_button_not_ready)
                    )
                )
            )
            return
        }

        KeyPay.startCheckout(activity, request, callback)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
