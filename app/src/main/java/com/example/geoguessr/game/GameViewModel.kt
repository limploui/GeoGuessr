// game/GameViewModel.kt
package com.example.geoguessr.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel

enum class GameMode { NORMAL, HINT }

data class RoundResult(
    val points: Int,
    val distanceKm: Double
)

// Das GameViewModel merkt sich alles, was zum Spielstand gehört:
//Welcher Modus läuft (Normal oder Hinweis)?
//Wieviele Runden gibt’s insgesamt, welche ist gerade dran?
//Wie viele Punkte und wie viele Kilometer Distanz hast du gesammelt?
//Was war das Ergebnis der letzten Runde?
//Gibt’s schon alle Ergebnisse als Liste?

class GameViewModel(app: Application) : AndroidViewModel(app) {

    //Variablen im GameViewModel
    //mode: aktueller Spielmodus, Standard = NORMAL.
    var mode: GameMode = GameMode.NORMAL
        private set
    //totalRounds: wie viele Runden das Spiel insgesamt hat.
    var totalRounds: Int = 1
        private set
    //currentRound: Zähler für die aktuelle Runde (fängt bei 1 an).
    var currentRound: Int = 1
        private set
    //totalPoints: alle Punkte bisher summiert.
    var totalPoints: Int = 0
        private set
    //lastRoundPoints: Punkte nur für die letzte Runde.
    var lastRoundPoints: Int = 0
        private set
    // Sekunden pro Runde
    var roundSeconds: Int = 60
        private set
    // NEU: Distanzen & Rundensammlung
    //lastRoundDistanceKm: Distanz in km für die letzte Runde.
    var lastRoundDistanceKm: Double = 0.0
        private set
    //totalDistanceKm: Gesamtdistanz über alle Runden.
    var totalDistanceKm: Double = 0.0
        private set
    //_results: private MutableList mit allen RoundResult-Objekten.
    private val _results = mutableListOf<RoundResult>()
    //results: öffentlich nur lesbar (List), damit die UI darauf zugreifen kann.
    val results: List<RoundResult> get() = _results



    // WICHTIG: Um JVM-Namenskonflikte zu vermeiden (Property-Setter vs. Fun-Namen),
    // keine Methoden "setXxx" benutzen, sondern andere Namen:
    //setGameMode(m): setzt den Modus (ohne Property-Namenskonflikt, deshalb nicht setMode).
    fun setGameMode(m: GameMode) { mode = m }

    //updateTotalRounds(n): setzt die Rundenzahl, aber begrenzt auf 1–10 (coerceIn).
    fun updateTotalRounds(n: Int) { totalRounds = n.coerceIn(1, 10) }

    //updateRoundSeconds(sec): setzt die Zeit, aber begrenzt auf 10–600s.
    fun updateRoundSeconds(sec: Int) {
        roundSeconds = if (sec == Int.MAX_VALUE) {
            Int.MAX_VALUE           // ∞ zulassen
        } else {
            sec.coerceIn(10, 600)   // normales Limit
        }
    }
    //startGame(): Spiel wird „resettet“:
    fun startGame() {
        currentRound = 1
        totalPoints = 0
        lastRoundPoints = 0
        lastRoundDistanceKm = 0.0
        totalDistanceKm = 0.0
        _results.clear()
    }

    // NEU: nimmt RoundResult entgegen (Punkte + Distanz)
    fun finishRound(summary: RoundResult) {
        lastRoundPoints = summary.points
        lastRoundDistanceKm = summary.distanceKm
        totalPoints += summary.points
        totalDistanceKm += summary.distanceKm
        _results.add(summary)
        currentRound++
    }

    //Alle Runden gespielt, Spiel vorbei.
    fun isFinished(): Boolean = currentRound > totalRounds
}
