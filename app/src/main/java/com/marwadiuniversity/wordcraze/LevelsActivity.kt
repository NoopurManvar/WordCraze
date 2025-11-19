package com.marwadiuniversity.wordcraze

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.res.Resources
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.cardview.widget.CardView
import androidx.activity.OnBackPressedCallback


fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

class LevelsActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "crossword_prefs"
        const val HIGHEST_UNLOCKED_LEVEL = "highest_unlocked_level"
        const val TOTAL_LEVELS = 50
    }

    private lateinit var levelGrid: GridLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var tvCoins: TextView
    private lateinit var titleCard: CardView
    private lateinit var coinsCard: CardView
    private lateinit var progressCard: CardView

    // Decoration elements
    private lateinit var starDeco1: TextView
    private lateinit var starDeco2: TextView
    private lateinit var rainbowDeco: TextView
    private lateinit var balloonDeco: TextView
    private lateinit var cloudDeco: TextView

    private var highestUnlockedLevel = 1
    private var totalCoins = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)

        initializeViews()
        setupAnimations()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@LevelsActivity, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
        val resetBtn: Button = findViewById(R.id.btnResetProgress)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Reset button with fun feedback
        resetBtn.setOnClickListener {
            animateButtonPress(resetBtn) {
                resetProgress()
            }
        }
        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        loadGameProgress()
        totalCoins = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt("coins", 0)
        updateCoinsDisplay()

        setupLevelButtons()
        updateProgressDisplay()
    }

    private fun initializeViews() {
        levelGrid = findViewById(R.id.levelGrid)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        tvCoins = findViewById(R.id.tvCoins)
        titleCard = findViewById(R.id.titleCard)
        coinsCard = findViewById(R.id.coinsCard)
        progressCard = findViewById(R.id.progressCard)

        // Decoration elements
        starDeco1 = findViewById(R.id.star_deco1)
        starDeco2 = findViewById(R.id.star_deco2)
        rainbowDeco = findViewById(R.id.rainbow_deco)
        balloonDeco = findViewById(R.id.balloon_deco)
        cloudDeco = findViewById(R.id.cloud_deco)
    }

    private fun setupAnimations() {
        // Animate entrance of main elements
        animateCardEntrance(titleCard, 300L)
        animateCardEntrance(coinsCard, 500L)
        animateCardEntrance(progressCard, 700L)

        // Floating decorations
        animateFloatingDecorations()
    }

    private fun animateCardEntrance(card: CardView, delay: Long) {
        card.alpha = 0f
        card.scaleX = 0.8f
        card.scaleY = 0.8f

        card.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(delay)
            .setDuration(600L)
            .setInterpolator(BounceInterpolator())
            .start()
    }

    private fun animateFloatingDecorations() {
        // Twinkling stars
        animateTwinkle(starDeco1, 1200L, 0L)
        animateTwinkle(starDeco2, 1500L, 300L)

        // Floating movements
        animateFloat(rainbowDeco, -8f, 8f, 2000L, 100L)
        animateFloat(balloonDeco, -15f, 15f, 2500L, 200L)
        animateFloat(cloudDeco, -10f, 10f, 3000L, 400L)

        // Gentle rotation for some elements
        animateRotation(rainbowDeco, 3000L)
    }

    private fun animateTwinkle(view: TextView, duration: Long, delay: Long) {
        val animator = ObjectAnimator.ofFloat(view, "alpha", 0.3f, 1f, 0.3f)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.startDelay = delay
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun animateFloat(
        view: TextView,
        fromY: Float,
        toY: Float,
        duration: Long,
        delay: Long
    ) {
        val animator = ObjectAnimator.ofFloat(view, "translationY", fromY, toY, fromY)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.startDelay = delay
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun animateRotation(view: TextView, duration: Long) {
        val animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun loadGameProgress() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        highestUnlockedLevel = prefs.getInt(HIGHEST_UNLOCKED_LEVEL, 1)
    }

    private fun updateCoinsDisplay() {
        tvCoins.text = totalCoins.toString()

        // Animate coin count changes
        val scaleAnimator = ObjectAnimator.ofFloat(coinsCard, "scaleX", 1f, 1.1f, 1f)
        scaleAnimator.duration = 300L
        scaleAnimator.start()
    }

    private fun setupLevelButtons() {
        levelGrid.removeAllViews()

        for (level in 1..TOTAL_LEVELS) {
            val button = createLevelButton(level)
            levelGrid.addView(button)
        }
    }

    private fun createLevelButton(level: Int): Button {
        return Button(this).apply {
            textSize = 18f
            isAllCaps = false
            typeface = Typeface.DEFAULT_BOLD
            stateListAnimator = null

            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 80.dpToPx()
                setMargins(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }

            elevation = 8f

            // Fun touch animation
            setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        v.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .setDuration(150L)
                            .start()
                        v.elevation = 16f
                    }

                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL -> {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150L)
                            .start()
                        v.elevation = 8f
                    }
                }
                false
            }

            // Style based on level status
            when {
                level < highestUnlockedLevel -> {
                    // Completed level - green and sparkly
                    text = "⭐ $level"
                    setTextColor(Color.WHITE)
                    background = createButtonBackground("#4ADE80") // bright green
                    isEnabled = true
                }

                level == highestUnlockedLevel -> {
                    // Current level - blue and pulsing
                    text = "🎯 $level"
                    setTextColor(Color.WHITE)
                    background = createButtonBackground("#3B82F6") // bright blue
                    isEnabled = true

                    // Pulsing animation for current level
                    val pulse = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.7f, 1f)
                    pulse.duration = 1500L
                    pulse.repeatCount = ObjectAnimator.INFINITE
                    pulse.start()

                    // Gentle bounce
                    val bounce = ObjectAnimator.ofFloat(this, "translationY", 0f, -5f, 0f)
                    bounce.duration = 2000L
                    bounce.repeatCount = ObjectAnimator.INFINITE
                    bounce.start()
                }

                else -> {
                    // Locked level - gray
                    text = "🔒 $level"
                    setTextColor(Color.parseColor("#9CA3AF"))
                    background = createButtonBackground("#E5E7EB") // light gray
                    isEnabled = false
                    alpha = 0.7f
                }
            }

            setOnClickListener {
                if (isEnabled) {
                    animateButtonPress(this) {
                        startLevel(level)
                    }
                }
            }
        }
    }

    private fun createButtonBackground(color: String): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = 20f
            setColor(Color.parseColor(color))
            setStroke(2.dpToPx(), Color.parseColor("#FFFFFF"))
        }
    }

    private fun animateButtonPress(button: Button, onComplete: () -> Unit) {
        button.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100L)
            .withEndAction {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100L)
                    .withEndAction {
                        onComplete()
                    }
                    .start()
            }
            .start()
    }

    private fun resetProgress() {
        val gamePrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        gamePrefs.edit().clear().apply()

        highestUnlockedLevel = 1
        totalCoins = 0
        updateCoinsDisplay()
        setupLevelButtons()
        updateProgressDisplay()

        // Fun celebration animation
        val celebrationText =
            Toast.makeText(this, "🎉 Progress Reset! Let's start fresh! 🎉", Toast.LENGTH_SHORT)
        celebrationText.show()

        // Animate progress card
        progressCard.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200L)
            .withEndAction {
                progressCard.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200L)
                    .start()
            }
            .start()
    }

    private fun startLevel(levelNumber: Int) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("level", levelNumber)
        intent.putExtra("coins", totalCoins)
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val completedLevel = data?.getIntExtra("level", 0) ?: 0
            val newCoins = data?.getIntExtra("total_coins", totalCoins) ?: totalCoins

            // Animate coin increase if any
            if (newCoins > totalCoins) {
                animateCoinIncrease(newCoins)
            }

            totalCoins = newCoins
            if (completedLevel > 0) unlockNextLevel(completedLevel)
            updateProgressDisplay()
        }
    }

    private fun animateCoinIncrease(newCoins: Int) {
        // Coin celebration animation
        val coinDifference = newCoins - totalCoins

        coinsCard.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(300L)
            .withEndAction {
                coinsCard.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300L)
                    .start()
            }
            .start()

        Toast.makeText(this, "🪙 +$coinDifference coins! Amazing! 🎉", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        totalCoins = prefs.getInt("coins", 0)
        updateCoinsDisplay()
    }

    private fun unlockNextLevel(completedLevel: Int) {
        val wasNewLevelUnlocked = completedLevel >= highestUnlockedLevel

        highestUnlockedLevel = maxOf(highestUnlockedLevel, completedLevel + 1)
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(HIGHEST_UNLOCKED_LEVEL, highestUnlockedLevel)
            .putInt("coins", totalCoins)
            .apply()

        setupLevelButtons()

        // Celebration for new level unlock
        if (wasNewLevelUnlocked && highestUnlockedLevel <= TOTAL_LEVELS) {
            Toast.makeText(this, "🎉 New level unlocked! Keep going! 🚀", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateProgressDisplay() {
        val completedLevels = maxOf(0, highestUnlockedLevel - 1)
        progressBar.max = TOTAL_LEVELS
        progressText.text = "$completedLevels/$TOTAL_LEVELS Levels"

        // Animate progress bar
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, completedLevels).apply {
            duration = 1000L
            interpolator = DecelerateInterpolator()
            start()
        }

        // Animate progress text
        progressText.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200L)
            .withEndAction {
                progressText.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200L)
                    .start()
            }
            .start()
    }

}