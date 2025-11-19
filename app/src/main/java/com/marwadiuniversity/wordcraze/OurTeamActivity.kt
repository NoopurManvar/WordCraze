package com.marwadiuniversity.wordcraze

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton

class OurTeamActivity : AppCompatActivity() {

    // Team member cards
    private lateinit var memberCard1: CardView
    private lateinit var memberCard2: CardView
    private lateinit var memberCard3: CardView
    private lateinit var backButton: ImageView  // Change from MaterialButton
    // Decorative elements
    private lateinit var star1: TextView
    private lateinit var star2: TextView
    private lateinit var star3: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_our_team)

        initializeViews()
        setupAnimations()
        setupBackButton()
    }

    private fun initializeViews() {
        memberCard1 = findViewById(R.id.memberCard1)
        memberCard2 = findViewById(R.id.memberCard2)
        memberCard3 = findViewById(R.id.memberCard3)
        backButton = findViewById(R.id.back)  // Now correctly an ImageView

        star1 = findViewById(R.id.star1)
        star2 = findViewById(R.id.star2)
        star3 = findViewById(R.id.star3)
    }

    private fun setupAnimations() {
        // Animate decorative stars
        animateFloatingStars()

        // Animate team cards entrance
        animateCardsEntrance()
    }

    private fun animateFloatingStars() {
        animateTwinkle(star1, 2000L, 0L)
        animateTwinkle(star2, 2500L, 500L)
        animateTwinkle(star3, 2200L, 1000L)

        // Floating animation for stars
        animateFloat(star1, 0f, -15f, 3000L, 0L)
        animateFloat(star2, 0f, -20f, 3500L, 200L)
        animateFloat(star3, 0f, -10f, 2800L, 400L)
    }

    private fun animateCardsEntrance() {
        val cards = listOf(memberCard1, memberCard2, memberCard3)

        cards.forEachIndexed { index, card ->
            // Initial state
            card.alpha = 0f
            card.translationY = 100f
            card.scaleX = 0.8f
            card.scaleY = 0.8f

            // Animate entrance with delay
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600L)
                .setStartDelay((index * 200L))
                .setInterpolator(DecelerateInterpolator())
                .start()

            // Add subtle floating animation after entrance
            card.postDelayed({
                animateCardFloat(card, index * 500L)
            }, 800L + (index * 200L))
        }
    }

    private fun animateCardFloat(card: CardView, delay: Long) {
        val animator = ObjectAnimator.ofFloat(card, "translationY", 0f, -8f, 0f)
        animator.duration = 2500L
        animator.repeatCount = ValueAnimator.INFINITE
        animator.startDelay = delay
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun animateTwinkle(view: TextView, duration: Long, delay: Long) {
        val animator = ObjectAnimator.ofFloat(view, "alpha", 0.3f, 1f, 0.3f)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.startDelay = delay
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun animateFloat(view: TextView, fromY: Float, toY: Float, duration: Long, delay: Long) {
        val animator = ObjectAnimator.ofFloat(view, "translationY", fromY, toY, fromY)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.startDelay = delay
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            // No need to cast anymore
            finish()
        }
    }

    private fun animateButtonPress(button: MaterialButton, onComplete: () -> Unit) {
        // Press down animation
        button.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(100L)
            .withEndAction {
                // Release animation
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100L)
                    .withEndAction {
                        // Delay before action for visual feedback
                        button.postDelayed({ onComplete() }, 50L)
                    }
                    .start()
            }
            .start()
    }

    override fun onPause() {
        super.onPause()
        // Clean up animations to save battery
        star1.clearAnimation()
        star2.clearAnimation()
        star3.clearAnimation()
    }
}