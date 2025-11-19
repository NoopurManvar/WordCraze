package com.marwadiuniversity.wordcraze

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var tvCoins: TextView
    private lateinit var tvLevelsCompleted: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var ivProfilePic: ImageView

    companion object {
        private const val PICK_IMAGE_REQUEST = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvUsername = findViewById(R.id.tvUsername)
        tvCoins = findViewById(R.id.tvCoins)
        tvLevelsCompleted = findViewById(R.id.tvLevelsCompleted)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        ivProfilePic = findViewById(R.id.ivProfilePic)

        val prefs = getSharedPreferences(LevelsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        // Load saved values
        val username = prefs.getString("username", "Player")
        val coins = prefs.getInt("coins", 0)
        val highestUnlockedLevel = prefs.getInt(LevelsActivity.HIGHEST_UNLOCKED_LEVEL, 1)
        val totalLevels = LevelsActivity.TOTAL_LEVELS
        val completedLevels = highestUnlockedLevel - 1
        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish() // close ProfileActivity so user doesn’t return here with back button
        }

        tvUsername.text = username
        tvCoins.text = coins.toString()
        tvLevelsCompleted.text = "$completedLevels/$totalLevels"

        // Load saved profile picture
        val savedUri = prefs.getString("profile_pic", null)
        if (savedUri != null) {
            ivProfilePic.setImageURI(Uri.parse(savedUri))
        }

        // Edit profile name
        btnEditProfile.setOnClickListener {
            val editText = EditText(this).apply { setText(username) }
            AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(editText)
                .setPositiveButton("Save") { _, _ ->
                    val newName = editText.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        prefs.edit().putString("username", newName).apply()
                        tvUsername.text = newName
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()

        }
    }

    override fun onResume() {
        super.onResume()
        // Update coins whenever this activity resumes
        val prefs = getSharedPreferences(LevelsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val coins = prefs.getInt("coins", 0)
        tvCoins.text = coins.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                ivProfilePic.setImageURI(imageUri)
                val prefs = getSharedPreferences(LevelsActivity.PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putString("profile_pic", imageUri.toString()).apply()
            }
        }
    }


}
