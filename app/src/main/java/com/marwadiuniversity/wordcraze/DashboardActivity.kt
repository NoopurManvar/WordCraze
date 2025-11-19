package com.marwadiuniversity.wordcraze

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import android.net.Uri
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ImageView
import android.widget.LinearLayout

import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat


class DashboardActivity : AppCompatActivity() {

    private lateinit var playButton: MaterialButton
    private lateinit var rewardsButton: MaterialButton
    private lateinit var ourTeamButton: MaterialButton
    private lateinit var profileButton: MaterialButton
    private lateinit var logoCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // ✅ must come first
        setContentView(R.layout.activity_dashboard)

        // ✅ force status bar to show with your purple color
        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_700)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.purple_700)
        window.decorView.systemUiVisibility = 0
        WindowCompat.setDecorFitsSystemWindows(window, true)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false


        // Animations
        val logo = findViewById<ImageView>(R.id.dashboardLogo)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val buttonContainer = findViewById<LinearLayout>(R.id.buttonContainer)
        val footer = findViewById<LinearLayout>(R.id.footerContainer)

        // Load animations
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_bounce)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val popIn = AnimationUtils.loadAnimation(this, R.anim.pop_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Apply animations
        logo.startAnimation(slideDown)
        welcomeText.startAnimation(fadeIn)
        footer.startAnimation(slideUp)

        // Animate each button in sequence
        for (i in 0 until buttonContainer.childCount) {
            val child = buttonContainer.getChildAt(i)
            child.startAnimation(popIn)
            child.startDelayAnimation(150L * i) // slight delay between buttons
        }

        initializeViews()
        setupAnimations()

        setupButtonClickListeners()
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val popup = PopupMenu(this, settingsButton)
            popup.menuInflater.inflate(R.menu.settings_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.share_game -> {
                        shareGame()
                        true
                    }

                    R.id.contact_us -> {
                        contactUs()
                        true
                    }

                    R.id.rate_us -> {
                        rateUs()
                        true
                    }
                    R.id.about -> {
                        showAboutDialog()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
    private fun android.view.View.startDelayAnimation(delay: Long) {
        postDelayed({ this.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pop_in)) }, delay)
    }
    private fun animateButtonPress(button: MaterialButton, onComplete: () -> Unit) {
        // Slightly darken and shrink the button when pressed
        button.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(100L)
            .withStartAction {
                // Make it a bit darker when pressed
                button.background.alpha = 200
            }
            .withEndAction {
                // Restore size and brightness
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100L)
                    .withEndAction {
                        button.background.alpha = 255
                        button.postDelayed({ onComplete() }, 80L)
                    }
                    .start()
            }
            .start()
    }

    private fun applyFloatingAnimation(button: MaterialButton) {
        // Gentle up-down floating motion
        val floatAnimator = ObjectAnimator.ofFloat(button, "translationY", 0f, -10f, 0f)
        floatAnimator.duration = 3000L
        floatAnimator.repeatCount = ValueAnimator.INFINITE
        floatAnimator.interpolator = AccelerateDecelerateInterpolator()
        floatAnimator.start()
    }
    private fun shareGame() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this fun game!")
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Play WordCraze now! 🎮 Download here: <your_playstore_link>")
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
    private fun showAboutDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("About WordCraze 🎮")
        builder.setMessage(
            """
        Version: 1.0.0
        Developed by: Noopur Manvar and Sasmita Das
        
        WordCraze is a fun, educational word puzzle game designed to make learning enjoyable!
        
        ✨ Thank you for playing and supporting us! ✨
        """.trimIndent()
        )
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun contactUs() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:ce.apps@marwadieducation.edu.in")
            putExtra(Intent.EXTRA_SUBJECT, "Contact from WordCraze App")
        }
        startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }

    private fun rateUs() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    private fun initializeViews() {
        playButton = findViewById(R.id.playButton)
        rewardsButton = findViewById(R.id.rewardsButton)
        ourTeamButton = findViewById(R.id.ourTeamButton)
        profileButton = findViewById(R.id.profileButton)

    }

    private fun setupAnimations() {

        listOf(playButton, rewardsButton, ourTeamButton, profileButton).forEach { button ->
            applyButtonAnimations(button)
        }
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

    private fun setupButtonClickListeners() {
        playButton.setOnClickListener {
            animateButtonPress(it as MaterialButton) {
                startActivity(Intent(this, LevelsActivity::class.java))
            }
        }

        rewardsButton.setOnClickListener {
            animateButtonPress(it as MaterialButton) {
                startActivity(Intent(this, SpinWheelActivity::class.java))
            }
        }

        ourTeamButton.setOnClickListener {
            animateButtonPress(it as MaterialButton) {
                startActivity(Intent(this, OurTeamActivity::class.java))
            }
        }

        profileButton.setOnClickListener {
            animateButtonPress(it as MaterialButton) {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }
    }


    private fun applyButtonAnimations(button: MaterialButton) {
        // Gentle bounce effect
        val bounceAnimator = ObjectAnimator.ofFloat(button, "translationY", 0f, -8f, 0f)
        bounceAnimator.duration = 2000L
        bounceAnimator.repeatCount = ValueAnimator.INFINITE
        bounceAnimator.interpolator = AccelerateDecelerateInterpolator()
        bounceAnimator.start()

        // Subtle scale animation on buttons
        val scaleAnimator = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.02f, 1f)
        scaleAnimator.duration = 3000L
        scaleAnimator.repeatCount = ValueAnimator.INFINITE
        scaleAnimator.start()
    }


    override fun onResume() {
        super.onResume()
        // Restart animations when returning to dashboard
        setupAnimations()
    }

    override fun onPause() {
        super.onPause()
    }
}
