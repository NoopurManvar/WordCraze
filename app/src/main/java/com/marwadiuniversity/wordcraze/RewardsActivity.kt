package com.marwadiuniversity.wordcraze

import android.animation.ObjectAnimator
import android.animation.Animator
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.floor
import kotlin.random.Random
import android.content.Intent
import android.widget.ImageView

class RewardsActivity : AppCompatActivity() {

    private lateinit var spinWheel: SpinWheelView
    private lateinit var spinButton: Button
    private lateinit var coinText: TextView

    private val prefs by lazy { getSharedPreferences(LevelsActivity.PREFS_NAME, MODE_PRIVATE) }
    private var currentRotation = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)

        spinWheel = findViewById(R.id.spinWheel)
        spinButton = findViewById(R.id.spinButton)
        coinText = findViewById(R.id.coinText)

        coinText.text = "Coins: ${getCoinBalance()}"

        spinButton.setOnClickListener {
            if (canSpin()) {
                spinWheel.isEnabled = false
                spinButton.isEnabled = false
                spin()
            } else {
               Toast.makeText(this, "You can spin only once a day!", Toast.LENGTH_SHORT).show()
            }
        }
        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            val intent = Intent(this, LevelsActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun getCoinBalance(): Int {
        return prefs.getInt("coins", 0)   // <- use "coins" consistently
    }

    private fun addCoins(coins: Int) {
        val newBalance = getCoinBalance() + coins
        prefs.edit().putInt("coins", newBalance).apply()
        coinText.text = "Coins: $newBalance"
    }


    private fun canSpin(): Boolean {
        val lastSpin = prefs.getString("lastSpinDate", null) ?: return true
        val today = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
        return lastSpin != today
    }

    private fun markSpunToday() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
        prefs.edit().putString("lastSpinDate", today).apply()
    }

    private fun spin() {
        val spinRounds = 5 // full rotations
        val randomAngle = Random.nextInt(360)
        val targetRotation = currentRotation + spinRounds * 360 + randomAngle

        val animator = ObjectAnimator.ofFloat(spinWheel, "rotationAngle", currentRotation, targetRotation)
        animator.duration = 4000
        animator.interpolator = DecelerateInterpolator()
        animator.start()

        animator.addUpdateListener {
            spinWheel.setRotationAngle(it.animatedValue as Float)
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                currentRotation = targetRotation % 360f
                val winnerIndex = calculateWinner(spinWheel.items.size, currentRotation)
                val prize = spinWheel.items[winnerIndex].toInt()

                addCoins(prize) // update shared prefs and UI
                markSpunToday()

                Toast.makeText(this@RewardsActivity, "You won $prize coins!", Toast.LENGTH_LONG).show()
                spinButton.isEnabled = true
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun calculateWinner(itemCount: Int, rotation: Float): Int {
        val normalized = (rotation % 360f)
        val anglePerItem = 360f / itemCount
        // Pointer at top (12 o'clock), rotation clockwise
        return floor((itemCount - (normalized / anglePerItem)) % itemCount).toInt()
    }
    override fun onBackPressed() {
        // Open LevelsActivity when back button is pressed
        val intent = Intent(this, LevelsActivity::class.java)
        startActivity(intent)
        finish() // close RewardsActivity
        // Do NOT call super.onBackPressed() here because it would finish the activity again
    }

}
