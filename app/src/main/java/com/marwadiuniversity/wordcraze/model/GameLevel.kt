package com.marwadiuniversity.wordcraze.model


data class GameLevel(
    val levelNumber: Int,
    val letters: List<Char>,
    val targetWords: List<String>,
    val gridSize: List<Int>,
    val maxTrials: Int, // 👈 new property
    val cwords: List<String> = emptyList() // default to empty list

)


data class LevelsWrapper(
    val levels: List<GameLevel>
)
