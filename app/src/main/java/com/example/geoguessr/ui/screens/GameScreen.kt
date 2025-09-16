// ui/screens/GameScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
        if (!isHintMode && tab == 2) tab = 1
    }

    // ⏱️ Countdown
    LaunchedEffect(imageId, timerRunning, lastPoints) {
        if (!timerRunning || lastPoints != null) return@LaunchedEffect
        while (timeLeft > 0 && lastPoints == null && timerRunning) {
            delay(1000)
            timeLeft -= 1
        }
        if (timeLeft <= 0 && lastPoints == null) {
            // Timeout: nur werten, wenn ein Tipp gesetzt wurde
            val center = bbox?.let { GeoUtils.bboxCenter(it) }
            val target = trueLocation ?: center
            if (guess != null && target != null) {
                truth = center
                val dKm = GeoUtils.haversineKm(
                    guess!!.first, guess!!.second,
                    target.first, target.second
                )
                lastDistanceKm = dKm
                val base = GeoUtils.distanceScore(dKm, maxScore = 5000)
                val penalty = if (isHintMode) GeoUtils.hintPenalty(hintsUsed) else 1.0
                lastPoints = (base * penalty).toInt()
            } else {
                truth = null
                lastDistanceKm = null
                lastPoints = 0
            }
            timerRunning = false
        }
    }

    Column(Modifier.fillMaxSize()) {

        // Tabs: Streetview, (Tipps), Karte  — Tabs liegen bewusst ÜBER der Map
        val tabTitles = if (isHintMode)
            listOf("Streetview", "Tipps", "Karte")
        else
            listOf("Streetview", "Karte")

        TabRow(
            selectedTabIndex = tab,
            modifier = Modifier.zIndex(2f) // 🔼 Tabs immer über Map/AndroidView
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
            }
        }

        // Kopfzeile: weißer, leicht transparenter Streifen über Map/Streetview
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .zIndex(1.5f), // 🔼 über der Map
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Zeit: ${timeLeft}s", style = MaterialTheme.typography.titleMedium)
                when {
                    isHintMode -> {
                        val multStr = String.format("%.2f", GeoUtils.hintPenalty(hintsUsed))
                        Text("Punkte ×$multStr", style = MaterialTheme.typography.titleMedium)
                    }
                    lastPoints != null -> Text("Punkte: $lastPoints", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        // Inhalt — Map/Viewer liegen UNTEN
        Box(
            Modifier
                .weight(1f)
                .zIndex(0f) // 🔽 unter Tabs/Head
        ) {
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
                            modifier = Modifier
                                .fillMaxSize()
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
                    // Nur werten, wenn ein Tipp existiert
                    if (guess == null) return@Button

                    val center = bbox?.let { GeoUtils.bboxCenter(it) }
                    truth = center
                    val target = trueLocation ?: center

                    val basePoints = if (target != null) {
                        val dKm = GeoUtils.haversineKm(
                            guess!!.first, guess!!.second,
                            target.first, target.second
                        )
                        lastDistanceKm = dKm
                        GeoUtils.distanceScore(dKm, maxScore = 5000)
                    } else {
                        lastDistanceKm = null
                        0
                    }
                    val penalty = if (isHintMode) GeoUtils.hintPenalty(hintsUsed) else 1.0
                    lastPoints = (basePoints * penalty).toInt()
                    timerRunning = false
                },
                // ✅ Bestätigen nur möglich, wenn ein Dot gesetzt wurde
                enabled = timeLeft > 0 && guess != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .zIndex(1f) // 🔼 sicher über der Map
            ) { Text("Bestätigen") }
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .zIndex(1f) // 🔼 sicher über der Map
            ) {
                lastDistanceKm?.let { Text("Distanz: ${"%.1f".format(it)} km") }
                if (isHintMode) {
                    val multStr = String.format("%.2f", GeoUtils.hintPenalty(hintsUsed))
                    Text("Multiplikator: ×$multStr")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onConfirmGuess(lastPoints!!)
                        // Reset für die nächste Runde
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
