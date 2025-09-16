// game/GameViewModel.kt
package com.example.geoguessr.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel

enum class GameMode { NORMAL, HINT }

data class RoundResult(
    val points: Int,
    val distanceKm: Double
)

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

    // ⏱ Sekunden pro Runde
    var roundSeconds: Int = 60
        private set

    // 🔢 NEU: Distanzen & Rundensammlung
    var lastRoundDistanceKm: Double = 0.0
        private set

    var totalDistanceKm: Double = 0.0
        private set

    private val _results = mutableListOf<RoundResult>()
    val results: List<RoundResult> get() = _results

    // WICHTIG: Um JVM-Namenskonflikte zu vermeiden (Property-Setter vs. Fun-Namen),
    // keine Methoden "setXxx" benutzen, sondern andere Namen:
    fun setGameMode(m: GameMode) { mode = m }
    fun updateTotalRounds(n: Int) { totalRounds = n.coerceIn(1, 10) }
    fun updateRoundSeconds(sec: Int) { roundSeconds = sec.coerceIn(10, 600) } // 10s..10min

    fun startGame() {
        currentRound = 1
        totalPoints = 0
        lastRoundPoints = 0
        lastRoundDistanceKm = 0.0
        totalDistanceKm = 0.0
        _results.clear()
    }

    // ⬇️ NEU: nimmt RoundResult entgegen (Punkte + Distanz)
    fun finishRound(summary: RoundResult) {
        lastRoundPoints = summary.points
        lastRoundDistanceKm = summary.distanceKm
        totalPoints += summary.points
        totalDistanceKm += summary.distanceKm
        _results.add(summary)
        currentRound++
    }

    fun isFinished(): Boolean = currentRound > totalRounds
}
