package com.marwadiuniversity.wordcraze

import android.content.Context
import android.media.MediaPlayer

object MusicManager {
    private var bgPlayer: MediaPlayer? = null
    private var suppressBackground = false

    fun playBackgroundMusic(context: Context, resId: Int) {
        if (suppressBackground) return

        if (bgPlayer == null) {
            bgPlayer = MediaPlayer.create(context.applicationContext, resId)
            bgPlayer?.isLooping = true
            bgPlayer?.start()   // start only once when created
        } else if (bgPlayer?.isPlaying == false) {
            bgPlayer?.start()
        }
    }


    fun stopBackgroundMusic() {
        bgPlayer?.pause()
    }

    fun release() {
        bgPlayer?.release()
        bgPlayer = null
    }

    fun setSuppressBackgroundMusic(suppress: Boolean) {
        suppressBackground = suppress
        if (suppress) stopBackgroundMusic()
    }
}
