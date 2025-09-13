// ui/screens/GameScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geoguessr.data.MapillaryViewer
import com.example.geoguessr.ui.map.OsmdroidMap
import com.example.geoguessr.util.GeoUtils
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun GameScreen(
    accessToken: String,
    imageId: String,
    bbox: DoubleArray?,
    trueLocation: Pair<Double, Double>?,   // (lat, lon)
    roundSeconds: Int = 60,                // aus VM durchreichen
    isHintMode: Boolean = false,           // ← Hinweis-Modus an/aus
    hints: List<String> = emptyList(),     // ← bis zu 5 Hinweise
    onConfirmGuess: (points: Int) -> Unit
) {
    var tab by remember { mutableStateOf(0) } // 0 Streetview, 1 Karte

    var guess by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var truth by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var lastPoints by rememberSaveable { mutableStateOf<Int?>(null) }
    var lastDistanceKm by rememberSaveable { mutableStateOf<Double?>(null) }

    // ⏱️ Timer-State
    var timeLeft by remember(imageId) { mutableStateOf(roundSeconds) } // reset bei neuem Bild
    var timerRunning by remember(imageId) { mutableStateOf(true) }

    // 💡 Hints-State (0..5)
    var hintsUsed by rememberSaveable(imageId) { mutableStateOf(0) }

    // ⏱️ Countdown – läuft, bis 0 oder bestätigt
    LaunchedEffect(imageId, timerRunning, lastPoints) {
        if (!timerRunning || lastPoints != null) return@LaunchedEffect
        while (timeLeft > 0 && lastPoints == null && timerRunning) {
            delay(1000)
            timeLeft -= 1
        }
        if (timeLeft <= 0 && lastPoints == null) {
            // Auto-Bestätigen: Truth = BBox-Mittelpunkt, Punkte berechnen
            val center = bbox?.let { GeoUtils.bboxCenter(it) }
            truth = center
            val target = trueLocation ?: center
            val basePoints = if (guess != null && target != null) {
                val dKm = GeoUtils.haversineKm(
                    guess!!.first, guess!!.second,
                    target.first, target.second
                )
                lastDistanceKm = dKm
                GeoUtils.scoreFromDistanceKm(dKm)
            } else {
                lastDistanceKm = null
                0
            }
            val mult = if (isHintMode) GeoUtils.hintMultiplier(hintsUsed) else 1
            lastPoints = (basePoints * mult).coerceAtMost(50000)
            timerRunning = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Tabs
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Streetview") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Karte") })
        }

        // Kopfzeile: Timer + (optional) Bonus
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Zeit: ${timeLeft}s")
            if (isHintMode) {
                val mult = GeoUtils.hintMultiplier(hintsUsed)
                Text("Bonus x$mult")
            } else if (lastPoints != null) {
                Text("Punkte: $lastPoints")
            }
        }

        // Hinweis-Panel (nur im Hinweis-Modus)
        if (isHintMode) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val shown = min(hintsUsed, hints.size)
                for (i in 0 until shown) {
                    Text("Tipp ${i + 1}: ${hints[i]}")
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (hintsUsed < 5 && hintsUsed < hints.size) hintsUsed++ },
                        enabled = lastPoints == null && hintsUsed < min(5, hints.size),
                    ) { Text("Tipp anzeigen") }
                    if (hintsUsed > 0) {
                        OutlinedButton(
                            onClick = { if (lastPoints == null) hintsUsed = (hintsUsed - 1).coerceAtLeast(0) },
                            enabled = lastPoints == null
                        ) { Text("Letzten Tipp zurücknehmen") }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Inhalt
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> MapillaryViewer(
                    accessToken = accessToken,
                    imageId = imageId,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> OsmdroidMap(
                    modifier = Modifier.fillMaxSize(),
                    bbox = bbox,
                    guessPoint = guess,
                    truthPoint = truth,
                    onTap = { lat, lon -> guess = Pair(lat, lon) }
                )
            }
        }

        // Aktionen
        if (lastPoints == null) {
            Button(
                onClick = {
                    val center = bbox?.let { GeoUtils.bboxCenter(it) }
                    truth = center
                    val target = trueLocation ?: center
                    val basePoints = if (guess != null && target != null) {
                        val dKm = GeoUtils.haversineKm(
                            guess!!.first, guess!!.second,
                            target.first, target.second
                        )
                        lastDistanceKm = dKm
                        GeoUtils.scoreFromDistanceKm(dKm)
                    } else {
                        lastDistanceKm = null
                        0
                    }
                    val mult = if (isHintMode) GeoUtils.hintMultiplier(hintsUsed) else 1
                    lastPoints = (basePoints * mult).coerceAtMost(50000)
                    timerRunning = false
                },
                enabled = timeLeft > 0, // nach Ablauf nicht mehr klickbar
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("Bestätigen") }
        } else {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                lastDistanceKm?.let { Text("Distanz: ${"%.1f".format(it)} km") }
                if (isHintMode) {
                    val mult = GeoUtils.hintMultiplier(hintsUsed)
                    Text("Bonus: x$mult")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onConfirmGuess(lastPoints!!)
                        // lokalen State resetten (nächste Runde)
                        guess = null
                        truth = null
                        lastPoints = null
                        lastDistanceKm = null
                        timeLeft = roundSeconds
                        timerRunning = true
                        hintsUsed = 0
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Weiter") }
            }
        }
    }
}
