package com.marwadiuniversity.wordcraze

// ... your existing imports
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.marwadiuniversity.wordcraze.model.GameLevel
import com.marwadiuniversity.wordcraze.model.JsonUtils
import kotlin.math.*
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit
import android.view.animation.AnimationUtils

class GameActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var lettersContainer: RelativeLayout
    private lateinit var tvLevel: TextView
    private lateinit var tvCoins: TextView
    private lateinit var tvWordsFound: TextView
    private lateinit var btnShuffle: ImageView
    private lateinit var btnHint: ImageView
    private lateinit var sparkleLineView: SparkleLineView
    private lateinit var konfettiView: KonfettiView
    private lateinit var tvTrials: TextView
    private lateinit var btnExtraWords: ImageButton

    // New list to store correct "bonus words" (from cwords)
    private val bonusWords = mutableListOf<String>()

    private val letterButtons = mutableListOf<TextView>()
    private val gridCells = mutableListOf<TextView>()
    private var selectedLetters = mutableListOf<TextView>()
    private var currentWord = StringBuilder()
    private var shownHintTutorial = false
    private var shownExtraWordsTutorial = false

    private lateinit var levels: List<GameLevel>
    private lateinit var currentLevel: GameLevel
    private val foundWords = mutableSetOf<String>()
    private var coins = 0
    private var currentWordAttempts = 0
    private var trialsLeft = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        tvTrials = findViewById(R.id.tv_trials)
        gridLayout = findViewById(R.id.grid_layout)
        lettersContainer = findViewById(R.id.letters_container)
        tvLevel = findViewById(R.id.tv_level)
        tvCoins = findViewById(R.id.tv_coins)
        tvWordsFound = findViewById(R.id.tv_words_found)
        btnShuffle = findViewById(R.id.btn_shuffle)
        btnHint = findViewById(R.id.btn_hint)
        sparkleLineView = findViewById(R.id.sparkle_line_view)
        sparkleLineView.visibility = View.VISIBLE
        konfettiView = findViewById(R.id.konfettiView)
        btnExtraWords = findViewById(R.id.btnExtraWords)
        btnExtraWords.setOnClickListener {
            showExtraWordsDialog()
        }


        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            val intent = Intent(this, LevelsActivity::class.java)
            startActivity(intent)
            finish() // close ProfileActivity so user doesn’t return here with back button
        }

        // ---- Load levels from assets/levels.json ----
        try {
            levels = JsonUtils.loadLevels(this)
            Log.d("DEBUG", "Loaded levels: ${levels.size}")
        } catch (ex: Exception) {
            Log.e("GameActivity", "Failed to load levels.json: ${ex.message}", ex)
            Toast.makeText(this, "Failed to load game data.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (levels.isEmpty()) {
            Log.e("GameActivity", "levels list is empty - check assets/levels.json")
            Toast.makeText(this, "Game data missing.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupLevel()
        setupClickListeners()
    }

    private fun setupLevel() {
        val levelNumber = intent.getIntExtra("level", 1)
        currentLevel = levels.getOrNull(levelNumber - 1) ?: levels[0]

        trialsLeft = currentLevel.maxTrials
        tvTrials.text = "Trials: $trialsLeft"

        val prefs = getSharedPreferences(LevelsActivity.PREFS_NAME, MODE_PRIVATE)
        coins = prefs.getInt("coins", 0)

        tvLevel.text = "Level ${currentLevel.levelNumber}"
        tvCoins.text = coins.toString()
        updateWordsFound()

        setupGrid()
        setupLetterButtons()
    }

    private fun setupGrid() {
        val (rows, cols) = currentLevel.gridSize
        gridCells.clear()
        gridLayout.removeAllViews()
        gridLayout.rowCount = rows
        gridLayout.columnCount = cols

        for (i in 0 until rows * cols) {
            val cell = TextView(this)
            val size = dpToPx(50)
            val params = GridLayout.LayoutParams().apply {
                width = size
                height = size
                setMargins(4, 4, 4, 4)
            }
            cell.layoutParams = params
            cell.background = createRoundedBackground(Color.WHITE).apply {
                setStroke(dpToPx(1), Color.parseColor("#E0E0E0"))
            }
            cell.gravity = android.view.Gravity.CENTER
            cell.textSize = 18f
            gridCells.add(cell)
            gridLayout.addView(cell)
        }
    }
    private fun showTutorialPopup(anchorView: View, message: String) {
        // Main container that holds popup, arrow, and pointer
        val rootContainer = FrameLayout(this)

        // Create the popup bubble
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(16))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#FFF9E6"))
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(3), Color.parseColor("#FFD700"))
            }
            elevation = dpToPx(12).toFloat()
        }

        // Create message text (no icon, centered)
        val tvMessage = TextView(this).apply {
            text = message
            setTextColor(Color.parseColor("#333333"))
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            maxWidth = dpToPx(260)
            setLineSpacing(dpToPx(4).toFloat(), 1f)
            setPadding(dpToPx(8), 0, dpToPx(8), 0)
        }

        container.addView(tvMessage)

        // Create white arrow pointing to the button
        val arrowContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, dpToPx(12), 0, 0)
        }

        val arrow = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(60), dpToPx(60))
            setImageDrawable(object : android.graphics.drawable.Drawable() {
                override fun draw(canvas: android.graphics.Canvas) {
                    val paint = android.graphics.Paint().apply {
                        color = Color.WHITE
                        style = android.graphics.Paint.Style.FILL
                        isAntiAlias = true
                        setShadowLayer(8f, 0f, 2f, Color.parseColor("#40000000"))
                    }

                    // Draw arrow pointing left (to the button)
                    val path = android.graphics.Path().apply {
                        moveTo(bounds.width() * 0.7f, bounds.height() * 0.2f) // Top right
                        lineTo(bounds.width() * 0.2f, bounds.height() * 0.5f) // Arrow tip (left)
                        lineTo(bounds.width() * 0.7f, bounds.height() * 0.8f) // Bottom right
                        lineTo(bounds.width() * 0.55f, bounds.height() * 0.5f) // Inner point
                        close()
                    }
                    canvas.drawPath(path, paint)

                    // Draw border
                    val strokePaint = android.graphics.Paint().apply {
                        color = Color.parseColor("#FFD700")
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = dpToPx(2).toFloat()
                        isAntiAlias = true
                    }
                    canvas.drawPath(path, strokePaint)
                }

                override fun setAlpha(alpha: Int) {}
                override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
                override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
            })
        }

        arrowContainer.addView(arrow)

        // Add views to main container
        rootContainer.addView(container)
        rootContainer.addView(arrowContainer)

        val popupWindow = PopupWindow(
            rootContainer,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = dpToPx(16).toFloat()
        popupWindow.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))

        // Position the popup to the left of the anchor button
        rootContainer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val offsetX = -rootContainer.measuredWidth - dpToPx(8)
        val offsetY = -(anchorView.height / 2 + rootContainer.measuredHeight / 2)

        popupWindow.showAsDropDown(anchorView, offsetX, offsetY)

        // Auto dismiss after 3 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            popupWindow.dismiss()
        }, 3000)

        // Also allow tap to dismiss
        rootContainer.setOnClickListener {
            popupWindow.dismiss()
        }
    }

    private fun setupLetterButtons() {
        letterButtons.clear()
        lettersContainer.removeAllViews()
        sparkleLineView.clearPath()

        lettersContainer.post {
            val containerWidth = lettersContainer.width
            val containerHeight = lettersContainer.height
            val buttonSize = dpToPx(60)

            currentLevel.letters.forEachIndexed { index, letter ->
                val button = TextView(this).apply {
                    text = letter.toString()
                    gravity = android.view.Gravity.CENTER
                    textSize = 20f
                    setTextColor(Color.WHITE)
                    background = createCircularBackground(Color.BLUE)
                    tag = letter
                }
                letterButtons.add(button)

                val angle = 2 * PI * index / currentLevel.letters.size
                val radius = min(containerWidth, containerHeight) / 3f
                val centerX = containerWidth / 2f
                val centerY = containerHeight / 2f

                val x = (centerX + radius * cos(angle) - buttonSize / 2).toInt()
                val y = (centerY + radius * sin(angle) - buttonSize / 2).toInt()

                val params = RelativeLayout.LayoutParams(buttonSize, buttonSize)
                params.leftMargin = x
                params.topMargin = y
                button.layoutParams = params

                lettersContainer.addView(button)
            }
            setupLetterTouchHandling()
        }
    }
    private fun showExtraWordsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_extra_words, null)
        val listView = dialogView.findViewById<ListView>(R.id.listExtraWords)
        val progress = dialogView.findViewById<ProgressBar>(R.id.progressExtra)
        val tvReward = dialogView.findViewById<TextView>(R.id.tvReward)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bonusWords)
        listView.adapter = adapter

        progress.progress = bonusWords.size.coerceAtMost(5)

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun setupLetterTouchHandling() {
        var isSelecting = false
        lettersContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isSelecting = true; clearSelection(); handleLetterTouch(event.x, event.y); true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isSelecting) handleLetterTouch(event.x, event.y); true
                }
                MotionEvent.ACTION_UP -> {
                    if (isSelecting) { isSelecting = false; checkWord() }; true
                }
                else -> false
            }
        }
    }

    private fun handleLetterTouch(x: Float, y: Float) {
        letterButtons.forEach { btn ->
            if (!selectedLetters.contains(btn)) {
                val radius = btn.width / 2f
                val centerX_parent = btn.left + radius
                val centerY_parent = btn.top + radius
                if (sqrt((x - centerX_parent).pow(2) + (y - centerY_parent).pow(2)) <= radius) {
                    selectedLetters.add(btn)
                    currentWord.append(btn.tag as Char)
                    btn.background = createCircularBackground(Color.YELLOW, Color.parseColor("#FFD700"))
                    btn.setTextColor(Color.BLACK)

                    val btnLoc = IntArray(2)
                    btn.getLocationOnScreen(btnLoc)
                    val btnCenterX_screen = btnLoc[0] + btn.width / 2f
                    val btnCenterY_screen = btnLoc[1] + btn.height / 2f

                    val sparkleLoc = IntArray(2)
                    sparkleLineView.getLocationOnScreen(sparkleLoc)

                    val localX = btnCenterX_screen - sparkleLoc[0]
                    val localY = btnCenterY_screen - sparkleLoc[1]

                    sparkleLineView.addPoint(localX, localY)
                }
            }
        }
    }

    private fun createCircularBackground(color: Int, strokeColor: Int? = null): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            strokeColor?.let { setStroke(dpToPx(3), it) }
        }
    }

    private fun clearSelection() {
        selectedLetters.forEach {
            it.background = createCircularBackground(Color.BLUE)
            it.setTextColor(Color.WHITE)
        }
        selectedLetters.clear()
        currentWord.clear()
        sparkleLineView.clearPath()
    }

    private fun checkWord() {
        // If no letters were selected, skip counting this as a trial
        if (currentWord.isEmpty()) {
            clearSelection()
            return
        }

        currentWordAttempts++
        val word = currentWord.toString().uppercase()

        when {
            // ✅ Found a correct target word
            currentLevel.targetWords.contains(word) && !foundWords.contains(word) -> {
                foundWords.add(word)
                updateWordsFound()
                placeWordInGrid(word)
                showConfetti()
                checkLevelComplete()
            }

            // ✅ Found a bonus/extra word (from cwords)
            currentLevel.cwords.contains(word) && !bonusWords.contains(word) -> {
                bonusWords.add(word)
                updateCoins(coins + 1)

                btnExtraWords.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse))

                // 👇 Show tutorial popup only once (first extra word)
                if (currentLevel.levelNumber == 1 && !shownExtraWordsTutorial) {
                    shownExtraWordsTutorial = true
                    showTutorialPopup(
                        btnExtraWords,
                        "You can see all your extra words here!"
                    )
                }
            }

            // ❌ Wrong word (count as a failed attempt)
            else -> {
                trialsLeft--
                tvTrials.text = "Trials: $trialsLeft"

                // 👇 After 2 failed attempts, show hint tutorial (only in Level 1)
                if (currentLevel.levelNumber == 1 &&
                    trialsLeft == currentLevel.maxTrials - 2 &&
                    !shownHintTutorial
                ) {
                    shownHintTutorial = true
                    showTutorialPopup(
                        btnHint,
                        "Stuck? Tap the hint button to get help!"
                    )
                }

                if (trialsLeft <= 0) {
                    showOutOfTrialsDialog()
                    return
                }
            }
        }

        clearSelection()
    }

    private fun showExtraWordDialog(newWord: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_extra_words, null)
        val listView = dialogView.findViewById<ListView>(R.id.listExtraWords)
        val progress = dialogView.findViewById<ProgressBar>(R.id.progressExtra)
        val tvReward = dialogView.findViewById<TextView>(R.id.tvReward)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bonusWords)
        listView.adapter = adapter
        progress.progress = bonusWords.size.coerceAtMost(5)

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
        val dialog = builder.create()

        dialog.show()

        // Auto-dismiss after 2 seconds like in the game
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        listView.postDelayed({ dialog.dismiss() }, 2000)
    }

    private fun checkLevelComplete() {
        if (foundWords.size == currentLevel.targetWords.size) {
            // Add coins for bonus words
            val bonusCoins = bonusWords.size * 1
            val totalCoinsEarned = 50 + bonusCoins
            updateCoins(coins + totalCoinsEarned)
            Toast.makeText(this, "Level Complete! +50 coins +$bonusCoins bonus coins", Toast.LENGTH_SHORT).show()

            val prefs = getSharedPreferences(LevelsActivity.PREFS_NAME, MODE_PRIVATE)
            prefs.edit()
                .putInt(LevelsActivity.HIGHEST_UNLOCKED_LEVEL, currentLevel.levelNumber + 1)
                .putInt("coins", coins)
                .apply()

            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("level", currentLevel.levelNumber)
                putExtra("words_found", foundWords.size)
                putExtra("total_words", currentLevel.targetWords.size)
                putExtra("coins_earned", totalCoinsEarned)
                putExtra("total_coins", coins)
                putExtra("attempts", currentWordAttempts)
                putExtra("lost", false)
            }
            startActivityForResult(intent, 200)
        }
    }

    private fun showOutOfTrialsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Out of Trials!")
        builder.setMessage("Do you want to spend 50 coins to get 5 more trials or continue?")

        builder.setPositiveButton("Use 50 Coins") { dialog, _ ->
            if (coins >= 50) {
                updateCoins(coins - 50)
                trialsLeft += 5
                tvTrials.text = "Trials: $trialsLeft"
                Toast.makeText(this, "You got 5 more trials!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT).show()
                openResultPage() // 👈 directly go to result if no coins
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Continue") { dialog, _ ->
            openResultPage()
            dialog.dismiss()
        }

        builder.setCancelable(false)
        builder.show()
    }

    private fun openResultPage() {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("level", currentLevel.levelNumber)
            putExtra("coins_earned", 0)
            putExtra("total_coins", coins)
            putExtra("attempts", currentWordAttempts)
            putExtra("lost", true)
        }
        startActivity(intent)
        finish()
    }

    private fun showConfetti() {
        konfettiView.visibility = View.VISIBLE
        konfettiView.bringToFront()
        konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                angle = 270,
                timeToLive = 2000L,
                colors = listOf(Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GREEN),
                emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).max(150),
                position = Position.Relative(0.5, 0.0)
            )
        )

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            konfettiView.visibility = View.GONE
        }, 2500)
    }

    private fun placeWordInGrid(word: String) {
        val (rows, cols) = currentLevel.gridSize
        var placed = false

        for (row in 0 until rows) {
            for (col in 0..cols - word.length) {
                var canPlace = true
                for (i in word.indices) {
                    val cellText = gridCells[row * cols + col + i].text.toString()
                    if (cellText.isNotEmpty() && cellText != word[i].toString()) {
                        canPlace = false
                        break
                    }
                }
                if (canPlace) {
                    word.forEachIndexed { i, c -> animateCell(gridCells[row * cols + col + i], c, i) }
                    placed = true
                    break
                }
            }
            if (placed) break
        }

        if (!placed) {
            for (col in 0 until cols) {
                for (row in 0..rows - word.length) {
                    var canPlace = true
                    for (i in word.indices) {
                        val cellText = gridCells[(row + i) * cols + col].text.toString()
                        if (cellText.isNotEmpty() && cellText != word[i].toString()) {
                            canPlace = false
                            break
                        }
                    }
                    if (canPlace) {
                        word.forEachIndexed { i, c -> animateCell(gridCells[(row + i) * cols + col], c, i) }
                        placed = true
                        break
                    }
                }
                if (placed) break
            }
        }
    }

    private fun animateCell(cell: TextView, letter: Char, index: Int) {
        cell.text = letter.toString()
        cell.setTextColor(Color.BLACK)
        cell.background = createRoundedBackground(Color.parseColor("#E8F5E8")).apply {
            setStroke(dpToPx(2), Color.parseColor("#4CAF50"))
        }
        cell.alpha = 0f
        cell.scaleX = 0.5f
        cell.scaleY = 0.5f
        cell.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setStartDelay((index * 100).toLong())
            .start()
    }

    private fun updateWordsFound() {
        tvWordsFound.text = "${foundWords.size}/${currentLevel.targetWords.size}"
    }

    private fun setupClickListeners() {
        btnShuffle.setOnClickListener { shuffleLetters() }
        btnHint.setOnClickListener { showHint() }
    }

    private fun shuffleLetters() {
        val shuffled = currentLevel.letters.shuffled()
        letterButtons.forEachIndexed { i, btn ->
            btn.text = shuffled[i].toString()
            btn.tag = shuffled[i]
        }
    }

    private fun showHint() {
        if (coins >= 20) {
            updateCoins(coins - 20)

            val remaining = currentLevel.targetWords - foundWords
            if (remaining.isNotEmpty()) {
                Toast.makeText(this, "Hint: starts with '${remaining.random().first()}'", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCoins(value: Int) {
        coins = max(0, value)
        tvCoins.text = coins.toString()
        val prefs = getSharedPreferences(LevelsActivity.PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putInt("coins", coins).apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    private fun createRoundedBackground(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = dpToPx(8).toFloat()
    }

    override fun onBackPressed() {
        val intent = Intent(this, LevelsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun createCircularBackground(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }
}
