package com.innovappsoft.keypay.payment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Native Android payment sheet.
 *
 * This mirrors the iOS SDK flow: create PaymentIntent through the developer
 * backend, present a clean confirmation screen, open KeyPay checkout, then check
 * the final status when the user returns.
 */
class KeyPayCheckoutActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var titleView: TextView
    private lateinit var merchantView: TextView
    private lateinit var amountView: TextView
    private lateinit var statusPillView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var primaryButton: Button
    private lateinit var helperView: TextView
    private lateinit var amountRowValue: TextView
    private lateinit var feeRowValue: TextView
    private lateinit var totalRowValue: TextView
    private lateinit var idRowValue: TextView

    private var request: KeyPayPaymentRequest? = null
    private var paymentIntent: KeyPayPaymentIntent? = null
    private var openedCheckout = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        request = buildRequestFromIntent(intent)
        buildLayout()
        createPaymentIntent()
    }

    override fun onResume() {
        super.onResume()
        if (openedCheckout && paymentIntent != null) {
            refreshPaymentStatus()
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun buildRequestFromIntent(intent: Intent): KeyPayPaymentRequest {
        val keys = intent.getStringArrayExtra(EXTRA_METADATA_KEYS).orEmpty()
        val values = intent.getStringArrayExtra(EXTRA_METADATA_VALUES).orEmpty()
        val metadata = keys.zip(values).toMap()

        return KeyPayPaymentRequest(
            amount = intent.getStringExtra(EXTRA_AMOUNT).orEmpty(),
            title = intent.getStringExtra(EXTRA_TITLE).orEmpty(),
            description = intent.getStringExtra(EXTRA_DESCRIPTION),
            externalReference = intent.getStringExtra(EXTRA_EXTERNAL_REFERENCE),
            currency = intent.getStringExtra(EXTRA_CURRENCY) ?: "KCOIN",
            metadata = metadata
        )
    }

    private fun buildLayout() {
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(colorSurface)
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(24))
            gravity = Gravity.CENTER_HORIZONTAL
            minimumHeight = resources.displayMetrics.heightPixels
        }
        scrollView.addView(root, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ))

        root.addView(buildHeader())
        root.addView(buildHeroCard(), matchWidthParams(top = dp(22)))
        root.addView(buildSummaryCard(), matchWidthParams(top = dp(18)))

        val spacer = View(this)
        root.addView(spacer, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))

        primaryButton = Button(this).apply {
            text = getString(R.string.keypay_continue)
            transformationMethod = null
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = rounded(Color.BLACK, 28)
            isEnabled = false
            setOnClickListener { openCheckout() }
        }
        root.addView(primaryButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(58)
        ).apply { topMargin = dp(22) })

        helperView = TextView(this).apply {
            text = getString(R.string.keypay_slide_helper)
            textSize = 14f
            setTextColor(colorSecondary)
            gravity = Gravity.CENTER
        }
        root.addView(helperView, matchWidthParams(top = dp(10)))

        setContentView(scrollView)
    }

    private fun buildHeader(): View {
        val header = FrameLayout(this)

        val label = TextView(this).apply {
            text = getString(R.string.keypay_confirm_payment)
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        header.addView(label, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            dp(64)
        ))

        val close = TextView(this).apply {
            text = getString(R.string.keypay_close_symbol)
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            background = rounded(Color.WHITE, 28, colorBorder, 1)
            setOnClickListener { cancelCheckout() }
        }
        header.addView(close, FrameLayout.LayoutParams(dp(56), dp(56), Gravity.END or Gravity.CENTER_VERTICAL))

        return header
    }

    private fun buildHeroCard(): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(30), dp(24), dp(30))
            background = rounded(Color.WHITE, 30)
        }

        val iconCircle = FrameLayout(this).apply {
            background = rounded(Color.rgb(238, 238, 240), 42, colorBorder, 1)
        }
        val icon = TextView(this).apply {
            text = getString(R.string.keypay_card_icon)
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
        }
        iconCircle.addView(icon, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        card.addView(iconCircle, LinearLayout.LayoutParams(dp(84), dp(84)))

        val secureLabel = TextView(this).apply {
            text = getString(R.string.keypay_secure_payment).uppercase(Locale.getDefault())
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
            setTextColor(colorSecondary)
            gravity = Gravity.CENTER
        }
        card.addView(secureLabel, matchWidthParams(top = dp(24)))

        titleView = TextView(this).apply {
            text = request?.title.orEmpty()
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
        }
        card.addView(titleView, matchWidthParams(top = dp(8)))

        merchantView = TextView(this).apply {
            text = KeyPay.requireConfiguration().merchantDisplayName
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colorSecondary)
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        card.addView(merchantView, matchWidthParams(top = dp(6)))

        amountView = TextView(this).apply {
            text = formatAmount(request?.amount.orEmpty(), request?.currency.orEmpty())
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        card.addView(amountView, matchWidthParams(top = dp(24)))

        val totalLabel = TextView(this).apply {
            text = getString(R.string.keypay_total_to_pay)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colorSecondary)
            gravity = Gravity.CENTER
        }
        card.addView(totalLabel, matchWidthParams(top = dp(2)))

        statusPillView = TextView(this).apply {
            text = getString(R.string.keypay_status_preparing)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colorOrange)
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(8), dp(18), dp(8))
            background = rounded(Color.rgb(255, 243, 229), 22)
        }
        card.addView(statusPillView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(18) })

        progressBar = ProgressBar(this).apply { isIndeterminate = true }
        card.addView(progressBar, LinearLayout.LayoutParams(dp(36), dp(36)).apply { topMargin = dp(18) })

        return card
    }

    private fun buildSummaryCard(): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(8), dp(18), dp(8))
            background = rounded(Color.WHITE, 24)
        }

        amountRowValue = addSummaryRow(card, getString(R.string.keypay_amount), formatAmount(request?.amount.orEmpty(), request?.currency.orEmpty()))
        feeRowValue = addSummaryRow(card, getString(R.string.keypay_fee), getString(R.string.keypay_no_fee))
        totalRowValue = addSummaryRow(card, getString(R.string.keypay_total), formatAmount(request?.amount.orEmpty(), request?.currency.orEmpty()))
        idRowValue = addSummaryRow(card, getString(R.string.keypay_payment_id), getString(R.string.keypay_waiting_id), isLast = true)

        return card
    }

    private fun addSummaryRow(parent: LinearLayout, label: String, value: String, isLast: Boolean = false): TextView {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(15), 0, dp(15))
        }

        val labelView = TextView(this).apply {
            text = label
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
        }
        row.addView(labelView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val valueView = TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(colorSecondary)
            gravity = Gravity.END
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.MIDDLE
        }
        row.addView(valueView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.15f))

        parent.addView(row)
        if (!isLast) {
            parent.addView(View(this).apply { setBackgroundColor(colorDivider) }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ))
        }
        return valueView
    }

    private fun createPaymentIntent() {
        setLoading(true, getString(R.string.keypay_loading_payment), getString(R.string.keypay_status_preparing))
        val currentRequest = request ?: return fail(KeyPayCheckoutException("invalid_request", getString(R.string.keypay_error_missing_request)))
        val client = KeyPayBackendClient(KeyPay.requireConfiguration())

        executor.execute {
            runCatching { client.createPaymentIntent(currentRequest) }
                .onSuccess { intent ->
                    mainHandler.post {
                        paymentIntent = intent
                        KeyPayCheckoutSession.latestIntent = intent
                        updateIntentViews(intent)
                        setLoading(false, getString(R.string.keypay_ready_to_pay), getString(R.string.keypay_status_pending))
                        primaryButton.text = getString(R.string.keypay_pay)
                        primaryButton.isEnabled = true
                        primaryButton.setOnClickListener { openCheckout() }
                    }
                }
                .onFailure { error -> mainHandler.post { fail(error) } }
        }
    }

    private fun openCheckout() {
        val checkoutUrl = paymentIntent?.checkoutUrl.orEmpty()
        if (checkoutUrl.isBlank()) {
            fail(KeyPayCheckoutException("missing_checkout_url", getString(R.string.keypay_error_missing_checkout_url)))
            return
        }

        openedCheckout = true
        setLoading(false, getString(R.string.keypay_waiting_return), getString(R.string.keypay_status_pending))
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl)))
    }

    private fun refreshPaymentStatus() {
        val intentId = paymentIntent?.id ?: return
        setLoading(true, getString(R.string.keypay_checking_status), getString(R.string.keypay_status_pending))
        val client = KeyPayBackendClient(KeyPay.requireConfiguration())

        executor.execute {
            runCatching { client.fetchPaymentIntent(intentId) }
                .onSuccess { updatedIntent ->
                    mainHandler.post {
                        paymentIntent = updatedIntent
                        KeyPayCheckoutSession.latestIntent = updatedIntent
                        updateIntentViews(updatedIntent)
                        completeIfFinished(updatedIntent)
                    }
                }
                .onFailure {
                    mainHandler.post {
                        setLoading(false, getString(R.string.keypay_status_pending_long), getString(R.string.keypay_status_pending))
                        primaryButton.text = getString(R.string.keypay_check_again)
                        primaryButton.isEnabled = true
                        primaryButton.setOnClickListener { refreshPaymentStatus() }
                    }
                }
        }
    }

    private fun completeIfFinished(intent: KeyPayPaymentIntent) {
        when (intent.status.lowercase()) {
            "succeeded", "paid", "completed", "complete" -> {
                setLoading(false, getString(R.string.keypay_status_paid), getString(R.string.keypay_status_paid_short), isSuccess = true)
                KeyPayCheckoutSession.complete(KeyPayPaymentResult.succeeded(intent))
                finish()
            }
            "cancelled", "canceled" -> {
                KeyPayCheckoutSession.complete(KeyPayPaymentResult.cancelled())
                finish()
            }
            "failed", "error" -> fail(KeyPayCheckoutException("payment_failed", getString(R.string.keypay_status_failed)))
            else -> {
                setLoading(false, getString(R.string.keypay_status_pending_long), getString(R.string.keypay_status_pending))
                primaryButton.text = getString(R.string.keypay_check_again)
                primaryButton.isEnabled = true
                primaryButton.setOnClickListener { refreshPaymentStatus() }
            }
        }
    }

    private fun updateIntentViews(intent: KeyPayPaymentIntent) {
        val displayAmount = formatAmount(intent.amount.ifBlank { request?.amount.orEmpty() }, intent.currency.ifBlank { request?.currency.orEmpty() })
        amountView.text = displayAmount
        amountRowValue.text = displayAmount
        totalRowValue.text = displayAmount
        idRowValue.text = intent.paymentLinkId ?: intent.id
    }

    private fun setLoading(loading: Boolean, message: String, pill: String, isSuccess: Boolean = false) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        helperView.text = message
        statusPillView.text = pill
        statusPillView.setTextColor(if (isSuccess) colorGreen else colorOrange)
        statusPillView.background = if (isSuccess) rounded(Color.rgb(232, 248, 236), 22) else rounded(Color.rgb(255, 243, 229), 22)
        primaryButton.isEnabled = !loading && paymentIntent != null
    }

    private fun cancelCheckout() {
        KeyPayCheckoutSession.complete(KeyPayPaymentResult.cancelled())
        finish()
    }

    private fun fail(error: Throwable) {
        progressBar.visibility = View.GONE
        helperView.text = error.message ?: getString(R.string.keypay_status_failed)
        statusPillView.text = getString(R.string.keypay_status_failed_short)
        statusPillView.setTextColor(colorRed)
        statusPillView.background = rounded(Color.rgb(255, 236, 238), 22)
        primaryButton.text = getString(R.string.keypay_close)
        primaryButton.isEnabled = true
        primaryButton.setOnClickListener { finish() }
        KeyPayCheckoutSession.complete(KeyPayPaymentResult.failed(error))
    }

    private fun formatAmount(amount: String, currency: String): String {
        val cleanAmount = amount.trim().ifBlank { "0.00" }
        val cleanCurrency = currency.trim().ifBlank { "KCOIN" }
        return "$cleanAmount $cleanCurrency"
    }

    private fun rounded(fill: Int, radiusDp: Int, stroke: Int? = null, strokeWidthDp: Int = 0): KeyPayRoundedDrawable {
        return KeyPayRoundedDrawable(fill, dp(radiusDp).toFloat(), stroke, dp(strokeWidthDp).toFloat())
    }

    private fun matchWidthParams(top: Int = 0): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = top }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private val colorSurface = Color.rgb(245, 245, 249)
        private val colorSecondary = Color.rgb(142, 142, 147)
        private val colorBorder = Color.rgb(224, 224, 230)
        private val colorDivider = Color.rgb(229, 229, 234)
        private val colorOrange = Color.rgb(242, 142, 43)
        private val colorGreen = Color.rgb(52, 168, 83)
        private val colorRed = Color.rgb(214, 67, 82)

        internal const val EXTRA_AMOUNT = "keypay.extra.AMOUNT"
        internal const val EXTRA_TITLE = "keypay.extra.TITLE"
        internal const val EXTRA_DESCRIPTION = "keypay.extra.DESCRIPTION"
        internal const val EXTRA_EXTERNAL_REFERENCE = "keypay.extra.EXTERNAL_REFERENCE"
        internal const val EXTRA_CURRENCY = "keypay.extra.CURRENCY"
        internal const val EXTRA_METADATA_KEYS = "keypay.extra.METADATA_KEYS"
        internal const val EXTRA_METADATA_VALUES = "keypay.extra.METADATA_VALUES"
    }
}
