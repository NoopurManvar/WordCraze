package com.marwadiuniversity.wordcraze.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

object JsonUtils {
    fun loadLevels(context: Context): List<GameLevel> {
        return try {
            val inputStream = context.assets.open("levels.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<GameLevel>>() {}.type
            val levels: List<GameLevel> = Gson().fromJson(reader, type)
            reader.close()
            levels
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
