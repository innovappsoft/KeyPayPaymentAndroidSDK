package com.innovappsoft.keypay.payment

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

/**
 * Rounded rectangle drawable with optional stroke.
 *
 * The SDK uses this to keep the native payment sheet lightweight and independent
 * from heavy UI frameworks while still looking polished.
 */
internal class KeyPayRoundedDrawable(
    private val fillColor: Int,
    private val radius: Float,
    private val strokeColor: Int? = null,
    private val strokeWidth: Float = 0f
) : Drawable() {
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = strokeColor ?: fillColor
        style = Paint.Style.STROKE
        strokeWidth = this@KeyPayRoundedDrawable.strokeWidth
    }

    override fun draw(canvas: Canvas) {
        val halfStroke = strokeWidth / 2f
        val rect = RectF(bounds).apply {
            inset(halfStroke, halfStroke)
        }
        canvas.drawRoundRect(rect, radius, radius, fillPaint)
        if (strokeColor != null && strokeWidth > 0f) {
            canvas.drawRoundRect(rect, radius, radius, strokePaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        fillPaint.alpha = alpha
        strokePaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        fillPaint.colorFilter = colorFilter
        strokePaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSPARENT
}
