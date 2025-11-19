    package com.marwadiuniversity.wordcraze

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class SpinWheelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var rotationAngle = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var items = listOf("15", "0", "20", "5", "0", "10")

    fun setRotationAngle(angle: Float) {
        rotationAngle = angle % 360
        invalidate()
    }

    fun getRotationAngle(): Float = rotationAngle

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val radius = min(w, h) / 2f
        val cx = w / 2
        val cy = h / 2

        val sweepAngle = 360f / items.size

        paint.textSize = radius / 6

        items.forEachIndexed { i, label ->
            val startAngle = i * sweepAngle + rotationAngle

            // Slice
            paint.style = Paint.Style.FILL
            paint.color = if (i % 2 == 0) Color.parseColor("#4CAF50") else Color.parseColor("#2196F3")
            canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius, startAngle, sweepAngle, true, paint)

            // Border
            paint.style = Paint.Style.STROKE
            paint.color = Color.WHITE
            paint.strokeWidth = 4f
            canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius, startAngle, sweepAngle, true, paint)

            // Label
            val angle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val textRadius = radius * 0.65f
            val x = (cx + textRadius * cos(angle)).toFloat()
            val y = (cy + textRadius * sin(angle)).toFloat() + paint.textSize / 3

            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.strokeWidth = 0f
            canvas.drawText(label, x, y, paint)
        }

        // Center circle
        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy, radius / 6, paint)

        // Pointer
        drawPointer(canvas, cx, cy, radius)
    }

    private fun drawPointer(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val pointerHeight = radius / 8
        val pointerWidth = radius / 10
        paint.color = Color.RED
        paint.style = Paint.Style.FILL

        val path = Path()
        path.moveTo(cx, cy - radius)
        path.lineTo(cx - pointerWidth, cy - radius + pointerHeight)
        path.lineTo(cx + pointerWidth, cy - radius + pointerHeight)
        path.close()

        canvas.drawPath(path, paint)
    }

    fun getSelectedItem(): String {
        val sweepAngle = 360f / items.size
        val normalizedRotation = ((rotationAngle % 360) + 360) % 360
        val pointerAngle = (270f - normalizedRotation + 360) % 360
        val selectedIndex = (pointerAngle / sweepAngle).toInt() % items.size
        return items[selectedIndex]
    }

    fun getSelectedPoints(): Int {
        return try {
            getSelectedItem().toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }
}
