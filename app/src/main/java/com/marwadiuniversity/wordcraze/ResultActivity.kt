package com.marwadiuniversity.wordcraze

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class ResultActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var tvCoinsEarned: TextView
    private lateinit var tvTotalCoins: TextView
    private lateinit var btnNextLevel: Button
    private lateinit var btnHome: Button
    private lateinit var star1: ImageView
    private lateinit var star2: ImageView
    private lateinit var star3: ImageView
    private lateinit var konfettiView: KonfettiView
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        tvResult = findViewById(R.id.tvResult)
        tvCoinsEarned = findViewById(R.id.tvCoinsEarned)
        tvTotalCoins = findViewById(R.id.tvTotalCoins)
        btnNextLevel = findViewById(R.id.btnNextLevel)
        btnHome = findViewById(R.id.btnHome)
        star1 = findViewById(R.id.star1)
        star2 = findViewById(R.id.star2)
        star3 = findViewById(R.id.star3)
        konfettiView = findViewById(R.id.konfettiResult)

        val level = intent.getIntExtra("level", 1)
        val coinsEarned = intent.getIntExtra("coins_earned", 0)
        val totalCoins = intent.getIntExtra("total_coins", 0)
        val attempts = intent.getIntExtra("attempts", 0)
        val lost = intent.getBooleanExtra("lost", false)

        if (lost) {
            // ❌ Failed screen
            tvResult.text = "Level $level Failed 😢"
            tvCoinsEarned.text = "💰 +0 coins"
            tvTotalCoins.text = "💎 Total: $totalCoins coins"

            // Hide stars
            findViewById<android.widget.LinearLayout>(R.id.starContainer).visibility =
                android.view.View.GONE

            // Change button text
            btnNextLevel.text = "🔄 Play Again"

            btnNextLevel.setOnClickListener {
                val replayIntent = Intent(this, GameActivity::class.java)
                replayIntent.putExtra("level", level)
                startActivity(replayIntent)
                finish()
            }

            btnHome.setOnClickListener {
                startActivity(Intent(this, LevelsActivity::class.java))
                finish()
            }

        } else {
            // ✅ Win screen
            tvResult.text = "Level $level Complete!"
            tvCoinsEarned.text = "💰 +$coinsEarned coins"
            tvTotalCoins.text = "💎 Total: $totalCoins coins"

            // Play victory music
            MusicManager.stopBackgroundMusic()
            MusicManager.setSuppressBackgroundMusic(true)
            mediaPlayer = MediaPlayer.create(this, R.raw.victory_music)
            mediaPlayer?.isLooping = false
            mediaPlayer?.start()

            // Stars based on attempts
            val starsEarned = when (attempts) {
                in 1..4 -> 3
                in 5..8 -> 2
                in 9..15 -> 1
                else -> 0
            }
            animateStars(starsEarned)

            showConfetti()

            btnNextLevel.text = "🚀 Next Level"
            btnNextLevel.setOnClickListener {
                val nextLevelIntent = Intent(this, GameActivity::class.java)
                nextLevelIntent.putExtra("level", level + 1)
                nextLevelIntent.putExtra("total_coins", totalCoins)
                startActivity(nextLevelIntent)
                finish()
            }

            btnHome.setOnClickListener {
                startActivity(Intent(this, LevelsActivity::class.java))
                finish()
            }
        }
    }

    private fun showConfetti() {
        konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 40f,
                spread = 360,
                angle = 270,
                timeToLive = 3000L,
                colors = listOf(Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GREEN, Color.RED),
                emitter = Emitter(duration = 1000, TimeUnit.MILLISECONDS).max(300),
                position = Position.Relative(0.5, 0.0)
            )
        )
    }

    private fun animateStars(starsEarned: Int) {
        val stars = listOf(star1, star2, star3)
        for (i in stars.indices) {
            stars[i].setImageResource(
                if (i < starsEarned) R.drawable.ic_star_filled else R.drawable.ic_star_border
            )
            if (i < starsEarned) {
                stars[i].scaleX = 0f
                stars[i].scaleY = 0f
                stars[i].animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(400)
                    .setStartDelay((i * 300).toLong())
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
        if (isFinishing) {
            mediaPlayer?.release()
            MusicManager.setSuppressBackgroundMusic(false)
            MusicManager.playBackgroundMusic(this, R.raw.background_music)
        }
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, LevelsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
