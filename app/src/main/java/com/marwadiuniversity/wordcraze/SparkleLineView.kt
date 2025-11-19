package com.marwadiuniversity.wordcraze

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.animation.ValueAnimator
import kotlin.math.*
import kotlin.random.Random

class SparkleLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val path = Path()

    // Main line paint with glow
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF69B4") // Hot pink
        style = Paint.Style.STROKE
        strokeWidth = 8f
        pathEffect = CornerPathEffect(8f)
        setShadowLayer(15f, 0f, 0f, Color.parseColor("#FF1493"))
    }

    private val outerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        pathEffect = CornerPathEffect(8f)
        alpha = 80
    }

    private val innerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        pathEffect = CornerPathEffect(8f)
        alpha = 120
    }

    private val sparkleGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val points = ArrayList<PointF>()
    private val glowSparkles = ArrayList<GlowSparkle>()
    private var animator: ValueAnimator? = null
    private var animationTime = 0f

    private val glowColors = intArrayOf(
        Color.WHITE,
        Color.parseColor("#FFD700"), // Gold
        Color.parseColor("#FF69B4"), // Hot Pink
        Color.CYAN,
        Color.YELLOW
    )

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    data class GlowSparkle(
        var x: Float,
        var y: Float,
        var baseRadius: Float,
        var currentRadius: Float,
        var alpha: Float,
        var color: Int,
        var pulsePhase: Float = Random.nextFloat() * 2 * PI.toFloat(),
        var lifeTime: Float = 0f,
        val maxLifeTime: Float = Random.nextFloat() * 1500f + 800f
    )

    fun addPoint(x: Float, y: Float) {
        points.add(PointF(x, y))
        rebuildPath()
        addGlowSparkles()
        startAnimation()
    }

    fun clearPath() {
        points.clear()
        glowSparkles.clear()
        path.reset()
        stopAnimation()
        invalidate()
    }

    private fun rebuildPath() {
        path.reset()
        if (points.isNotEmpty()) {
            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
        }
    }

    private fun addGlowSparkles() {
        if (points.size < 2) return

        val lastPoint = points[points.size - 1]
        val secondLastPoint = points[points.size - 2]

        val distance = sqrt(
            (lastPoint.x - secondLastPoint.x).pow(2) +
                    (lastPoint.y - secondLastPoint.y).pow(2)
        )

        val numGlows = (distance / 25f).toInt().coerceAtLeast(1)

        for (i in 0..numGlows) {
            val ratio = i.toFloat() / numGlows
            val x = secondLastPoint.x + (lastPoint.x - secondLastPoint.x) * ratio
            val y = secondLastPoint.y + (lastPoint.y - secondLastPoint.y) * ratio

            val offsetX = (Random.nextFloat() - 0.5f) * 15f
            val offsetY = (Random.nextFloat() - 0.5f) * 15f

            val baseRadius = Random.nextFloat() * 8f + 6f

            glowSparkles.add(
                GlowSparkle(
                    x = x + offsetX,
                    y = y + offsetY,
                    baseRadius = baseRadius,
                    currentRadius = baseRadius,
                    alpha = 1f,
                    color = glowColors[Random.nextInt(glowColors.size)]
                )
            )
        }

        while (glowSparkles.size > 80) {
            glowSparkles.removeAt(0)
        }
    }

    private fun startAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = Long.MAX_VALUE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                animationTime += 16f
                updateGlowSparkles()
                invalidate()
            }
            start()
        }
    }

    private fun stopAnimation() {
        animator?.cancel()
        animator = null
    }

    private fun updateGlowSparkles() {
        val iterator = glowSparkles.iterator()
        while (iterator.hasNext()) {
            val sparkle = iterator.next()
            sparkle.lifeTime += 16f

            sparkle.pulsePhase += 0.1f
            val pulseFactor = (sin(sparkle.pulsePhase) + 1f) / 2f
            sparkle.currentRadius = sparkle.baseRadius * (0.7f + pulseFactor * 0.6f)

            val fadeRatio = 1f - (sparkle.lifeTime / sparkle.maxLifeTime)
            sparkle.alpha = (fadeRatio * (0.6f + pulseFactor * 0.4f)).coerceAtLeast(0f)

            sparkle.y -= 0.3f

            if (sparkle.alpha <= 0f) {
                iterator.remove()
            }
        }

        val glowIntensity = (sin(animationTime * 0.005f) + 1f) / 2f
        outerGlowPaint.color = Color.parseColor("#FF69B4")
        outerGlowPaint.alpha = (60 + glowIntensity * 40).toInt()

        innerGlowPaint.color = Color.parseColor("#FFB6C1")
        innerGlowPaint.alpha = (100 + glowIntensity * 60).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (points.isNotEmpty()) {
            canvas.drawPath(path, outerGlowPaint)
            canvas.drawPath(path, innerGlowPaint)
            canvas.drawPath(path, linePaint)
        }

        glowSparkles.forEach { sparkle ->
            drawGlowCircle(canvas, sparkle.x, sparkle.y, sparkle.currentRadius, sparkle.color, sparkle.alpha)
        }
    }

    private fun drawGlowCircle(
        canvas: Canvas,
        x: Float,
        y: Float,
        radius: Float,
        color: Int,
        alpha: Float
    ) {
        val gradientRadius = radius * 1.5f
        val gradient = RadialGradient(
            x, y, gradientRadius,
            intArrayOf(
                Color.argb((alpha * 255).toInt(), Color.red(color), Color.green(color), Color.blue(color)),
                Color.argb((alpha * 285).toInt(), Color.red(color), Color.green(color), Color.blue(color)),
                Color.argb((alpha * 80).toInt(), Color.red(color), Color.green(color), Color.blue(color)),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )

        sparkleGlowPaint.shader = gradient
        canvas.drawCircle(x, y, gradientRadius, sparkleGlowPaint)

        sparkleGlowPaint.shader = null
        sparkleGlowPaint.color = Color.argb((alpha * 255).toInt(), 255, 255, 255)
        canvas.drawCircle(x, y, radius * 0.3f, sparkleGlowPaint)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        clearPath() // keep it empty until user starts dragging
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}
