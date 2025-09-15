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
    roundSeconds: Int = 60,
    isHintMode: Boolean = false,
    hints: List<String> = emptyList(),
    onConfirmGuess: (points: Int) -> Unit
) {
    // Tabs: 0 Streetview, 1 Tipps (nur im Hint-Mode), 2 Karte
    var tab by remember { mutableStateOf(0) }

    var guess by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var truth by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var lastPoints by rememberSaveable { mutableStateOf<Int?>(null) }
    var lastDistanceKm by rememberSaveable { mutableStateOf<Double?>(null) }

    // ⏱️ Timer
    var timeLeft by remember(imageId) { mutableStateOf(roundSeconds) }
    var timerRunning by remember(imageId) { mutableStateOf(true) }

    // 💡 Hints (0..5)
    var hintsUsed by rememberSaveable(imageId) { mutableStateOf(0) }

    // Falls Hinweis-Tab verschwindet, zurück zu Streetview
    LaunchedEffect(isHintMode) {
        if (!isHintMode && tab == 1) tab = 0
        if (!isHintMode && tab == 2) tab = 1 // falls jemand noch auf 2 war
    }

    // ⏱️ Countdown
    LaunchedEffect(imageId, timerRunning, lastPoints) {
        if (!timerRunning || lastPoints != null) return@LaunchedEffect
        while (timeLeft > 0 && lastPoints == null && timerRunning) {
            delay(1000)
            timeLeft -= 1
        }
        if (timeLeft <= 0 && lastPoints == null) {
            val center = bbox?.let { GeoUtils.bboxCenter(it) }
            truth = center
            val target = trueLocation ?: center
            val basePoints = if (guess != null && target != null) {
                val dKm = GeoUtils.haversineKm(guess!!.first, guess!!.second, target.first, target.second)
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

        // Tabs: Streetview, (Tipps), Karte
        val tabTitles = if (isHintMode)
            listOf("Streetview", "Tipps", "Karte")
        else
            listOf("Streetview", "Karte")

        TabRow(selectedTabIndex = tab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
            }
        }

        // Kopfzeile
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Zeit: ${timeLeft}s")
            when {
                isHintMode -> {
                    val mult = GeoUtils.hintMultiplier(hintsUsed)
                    Text("Bonus x$mult")
                }
                lastPoints != null -> Text("Punkte: $lastPoints")
            }
        }

        // Inhalt
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> MapillaryViewer(
                    accessToken = accessToken,
                    imageId = imageId,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> {
                    if (isHintMode) {
                        // Tipps
                        Column(
                            modifier = Modifier.fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            val shown = min(hintsUsed, hints.size)
                            if (hints.isEmpty()) {
                                Text("Keine Tipps verfügbar.")
                            } else {
                                repeat(shown) { i ->
                                    Text("Tipp ${i + 1}: ${hints[i]}")
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (hintsUsed < 5 && hintsUsed < hints.size) hintsUsed++
                                },
                                enabled = lastPoints == null && hintsUsed < min(5, hints.size)
                            ) { Text("Tipp anzeigen") }
                        }
                    } else {
                        // Kein Hint-Mode: Tab 1 ist die Karte
                        OsmdroidMap(
                            modifier = Modifier.fillMaxSize(),
                            bbox = bbox,
                            guessPoint = guess,
                            truthPoint = truth,
                            onTap = { lat, lon -> guess = Pair(lat, lon) }
                        )
                    }
                }
                2 -> {
                    // Nur im Hint-Mode vorhanden: Karte
                    OsmdroidMap(
                        modifier = Modifier.fillMaxSize(),
                        bbox = bbox,
                        guessPoint = guess,
                        truthPoint = truth,
                        onTap = { lat, lon -> guess = Pair(lat, lon) }
                    )
                }
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
                        val dKm = GeoUtils.haversineKm(guess!!.first, guess!!.second, target.first, target.second)
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
                enabled = timeLeft > 0,
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
                        // Reset
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
