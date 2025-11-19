package com.marwadiuniversity.wordcraze

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // ✅ Start background music once from here
        MusicManager.playBackgroundMusic(this, R.raw.background_music)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }, 2000) // sec
    }
}
