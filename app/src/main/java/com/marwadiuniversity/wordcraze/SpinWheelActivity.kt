package com.marwadiuniversity.wordcraze

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SpinWheelActivity : AppCompatActivity() {

    private lateinit var spinWheel: SpinWheelView
    private lateinit var btnSpin: Button
    private lateinit var tvResult: TextView
    private lateinit var tvTimer: TextView

    private var isSpinning = false
    private var countDownTimer: CountDownTimer? = null

    companion object {
        private const val SPIN_COOLDOWN_MS = 6 * 60 * 60 * 1000L // 6 hours
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spin_wheel)

        initViews()
        setupClickListeners()
        checkCooldown()
    }

    private fun initViews() {
        spinWheel = findViewById(R.id.spinWheel)
        btnSpin = findViewById(R.id.btnSpin)
        tvResult = findViewById(R.id.tvResult)
        tvTimer = findViewById(R.id.tvTimer)

        val coins = getCoins()
        tvResult.text = "Total Coins: $coins"
    }

    private fun setupClickListeners() {
        btnSpin.setOnClickListener {
            if (!isSpinning) {
                if (canSpin()) {
                    spinWheel()
                } else {
                    Toast.makeText(this, "Wait for cooldown!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun spinWheel() {
        isSpinning = true
        btnSpin.isEnabled = false
        btnSpin.text = "SPINNING..."

        val startAngle = spinWheel.getRotationAngle()
        val randomAngle = (360 * 3..360 * 6).random() + (0..360).random()

        val animator = ObjectAnimator.ofFloat(
            spinWheel,
            "rotationAngle",
            startAngle,
            startAngle + randomAngle.toFloat()
        )
        animator.duration = 3000
        animator.interpolator = DecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val currentAngle = animation.animatedValue as Float
            spinWheel.setRotationAngle(currentAngle)
        }

        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                super.onAnimationEnd(animation)
                handleSpinResult()
                saveLastSpinTime()
                checkCooldown() // restart timer when spin ends
            }
        })

        animator.start()
    }

    private fun saveCoins(coins: Int) {
        val prefs = getSharedPreferences(LevelsActivity.PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putInt("coins", coins).apply()
    }

    private fun getCoins(): Int {
        val prefs = getSharedPreferences(LevelsActivity.PREFS_NAME, MODE_PRIVATE)
        return prefs.getInt("coins", 0)
    }

    private fun handleSpinResult() {
        val points = spinWheel.getSelectedPoints()
        val currentCoins = getCoins()
        val newCoins = currentCoins + points

        saveCoins(newCoins)

        tvResult.text = "You got $points coins!\nTotal Coins: $newCoins"

        isSpinning = false
        btnSpin.isEnabled = false
        btnSpin.text = "WAIT"
    }

    // --- Cooldown handling ---
    private fun saveLastSpinTime() {
        val prefs = getSharedPreferences(LevelsActivity.PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putLong("lastSpinTime", System.currentTimeMillis()).apply()
    }

    private fun getLastSpinTime(): Long {
        val prefs = getSharedPreferences(LevelsActivity.PREFS_NAME, MODE_PRIVATE)
        return prefs.getLong("lastSpinTime", 0L)
    }

    private fun canSpin(): Boolean {
        val lastSpin = getLastSpinTime()
        return System.currentTimeMillis() - lastSpin >= SPIN_COOLDOWN_MS
    }

    private fun checkCooldown() {
        val lastSpin = getLastSpinTime()
        val elapsed = System.currentTimeMillis() - lastSpin

        if (elapsed >= SPIN_COOLDOWN_MS || lastSpin == 0L) {
            // Spin available
            btnSpin.isEnabled = true
            btnSpin.text = "SPIN"
            tvTimer.text = ""
        } else {
            // Spin still locked, show countdown
            val remaining = SPIN_COOLDOWN_MS - elapsed
            startCooldownTimer(remaining)
            btnSpin.isEnabled = false
            btnSpin.text = "WAIT"
        }
    }

    private fun startCooldownTimer(remainingTime: Long) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                val minutes = (millisUntilFinished / (1000 * 60)) % 60
                val seconds = (millisUntilFinished / 1000) % 60

                tvTimer.text = String.format("Next spin in: %02d:%02d:%02d", hours, minutes, seconds)
            }

            override fun onFinish() {
                tvTimer.text = ""
                btnSpin.isEnabled = true
                btnSpin.text = "SPIN"
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
