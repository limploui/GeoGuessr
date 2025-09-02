// game/GameViewModel.kt
package com.example.geoguessr.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel

enum class GameMode { NORMAL, HINT }

data class RoundResult(val points: Int)

class GameViewModel(app: Application) : AndroidViewModel(app) {
    var mode: GameMode = GameMode.NORMAL
        private set

    var totalRounds: Int = 1
        private set

    var currentRound: Int = 1
        private set

    var totalPoints: Int = 0
        private set

    var lastRoundPoints: Int = 0
        private set

    fun setMode(m: GameMode) { mode = m }
    fun setTotalRounds(n: Int) { totalRounds = n.coerceIn(1, 10) }

    fun startGame() {
        currentRound = 1
        totalPoints = 0
        lastRoundPoints = 0
        // Hier könntest du auch gleich das erste Bild laden
    }

    fun finishRound(points: Int) {
        lastRoundPoints = points
        totalPoints += points
        currentRound++
    }

    fun isFinished(): Boolean = currentRound > totalRounds
}
