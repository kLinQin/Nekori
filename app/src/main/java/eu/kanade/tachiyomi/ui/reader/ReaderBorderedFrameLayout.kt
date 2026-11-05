package eu.kanade.tachiyomi.ui.reader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import com.google.android.material.color.MaterialColors

class ReaderBorderedFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val borderWidthPx = dpToPx(1f)
    private val borderRadiusPx = dpToPx(2f)

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = borderWidthPx
    }
    private val borderRect = RectF()

    private var extraBottomInsetPx = 0

    init {
        setWillNotDraw(false)
        updateBorderColor()
    }

    /**
     * Naikkan border bawah sebesar [px] (mis. tinggi status bar reader di bawah) tanpa mengubah
     * layout child. Draw-only: tidak memicu perubahan tinggi WebView / status bar.
     */
    fun setExtraBottomInset(px: Int) {
        if (extraBottomInsetPx == px) return
        extraBottomInsetPx = px
        invalidate()
    }

    fun setBorderColor(argb: Int) {
        if (borderPaint.color == argb) return
        borderPaint.color = argb
        invalidate()
    }

    private var hasOverrideColor = false

    private fun updateBorderColor() {
        if (hasOverrideColor) return
        val fallback = 0x1F000000
        val color = runCatching {
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutlineVariant, fallback)
        }.getOrElse { fallback }
        borderPaint.color = if (color == 0) Color.TRANSPARENT else color
    }

    fun setBorderColorFromTheme(argb: Int) {
        hasOverrideColor = true
        setBorderColor(argb)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateBorderColor()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        val strokeInset = borderWidthPx / 2f
        val left = paddingLeft + strokeInset
        val top = paddingTop + strokeInset
        val right = width - paddingRight - strokeInset
        val bottom = height - paddingBottom - strokeInset - extraBottomInsetPx
        if (right <= left || bottom <= top) return
        borderRect.set(left, top, right, bottom)
        canvas.drawRoundRect(borderRect, borderRadiusPx, borderRadiusPx, borderPaint)
    }

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}
