package com.marwadiuniversity.wordcraze

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var musicIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Prepare the music service intent
        musicIntent = Intent(this, BackgroundMusicService::class.java)
    }

    override fun onStart() {
        super.onStart()
        // App comes to foreground → start/resume music
        startService(musicIntent)
    }

    override fun onStop() {
        super.onStop()
        // App goes to background → stop music
        stopService(musicIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(musicIntent)
    }
}
