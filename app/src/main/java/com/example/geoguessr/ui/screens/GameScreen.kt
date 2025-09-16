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
import com.example.geoguessr.game.RoundResult
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
    onConfirmGuess: (RoundResult) -> Unit
) {
    // Tabs: 0 Streetview, 1 Tipps (nur im Hint-Mode), 2 Karte
    var tab by remember { mutableStateOf(0) }

    var guess by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var truth by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var lastPoints by rememberSaveable { mutableStateOf<Int?>(null) }
    var lastDistanceKm by rememberSaveable { mutableStateOf<Double?>(null) }

    // ∞-Modus?
    val unlimitedTime = roundSeconds == Int.MAX_VALUE

    // ⏱️ Timer-State (bei ∞ kein Countdown)
    var timeLeft by remember(imageId, unlimitedTime) {
        mutableStateOf(if (unlimitedTime) Int.MAX_VALUE else roundSeconds)
    }
    var timerRunning by remember(imageId, unlimitedTime) { mutableStateOf(!unlimitedTime) }

    // 💡 Hints (0..5)
    var hintsUsed by rememberSaveable(imageId) { mutableStateOf(0) }

    // Falls Hinweis-Tab verschwindet, zurück zu Streetview
    LaunchedEffect(isHintMode) {
        if (!isHintMode && tab == 1) tab = 0
        if (!isHintMode && tab == 2) tab = 1
    }

    // ⏱️ Countdown → nur wenn Zeitlimit aktiv ist
    LaunchedEffect(imageId, timerRunning, lastPoints, unlimitedTime) {
        if (unlimitedTime || !timerRunning || lastPoints != null) return@LaunchedEffect
        while (timeLeft > 0 && lastPoints == null && timerRunning) {
            delay(1000)
            timeLeft -= 1
        }
        if (timeLeft <= 0 && lastPoints == null) {
            val center = bbox?.let { GeoUtils.bboxCenter(it) }
            val target = trueLocation ?: center
            if (guess != null && target != null) {
                truth = center
                val dKm = GeoUtils.haversineKm(guess!!.first, guess!!.second, target.first, target.second)
                lastDistanceKm = dKm
                val basePoints = GeoUtils.scoreFromDistanceKm(dKm)
                val mult = if (isHintMode) GeoUtils.hintMultiplier(hintsUsed) else 1
                lastPoints = (basePoints * mult).coerceAtMost(50000)
            } else {
                truth = null
                lastDistanceKm = null
                lastPoints = 0
            }
            timerRunning = false
        }
    }

    Column(Modifier.fillMaxSize()) {

        // Tabs
        val tabTitles = if (isHintMode)
            listOf("Streetview", "Tipps", "Karte")
        else
            listOf("Streetview", "Karte")

        TabRow(
            selectedTabIndex = tab,
            modifier = Modifier.zIndex(2f)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
            }
        }

        // Kopfzeile (zeigt ∞ bei ausgeschalteter Zeit)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .zIndex(1.5f),
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
                Text(
                    text = if (unlimitedTime) "Zeit: ∞" else "Zeit: ${timeLeft}s",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Inhalt
        Box(
            Modifier
                .weight(1f)
                .zIndex(0f)
        ) {
            when (tab) {
                0 -> MapillaryViewer(
                    accessToken = accessToken,
                    imageId = imageId,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> {
                    if (isHintMode) {
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
                    if (guess == null) return@Button

                    val center = bbox?.let { GeoUtils.bboxCenter(it) }
                    truth = center
                    val target = trueLocation ?: center

                    val (points, distKm) = if (target != null) {
                        val dKm = GeoUtils.haversineKm(
                            guess!!.first, guess!!.second,
                            target.first, target.second
                        )
                        val base = GeoUtils.scoreFromDistanceKm(dKm)
                        val mult = if (isHintMode) GeoUtils.hintMultiplier(hintsUsed) else 1
                        ((base * mult).coerceAtMost(50000)) to dKm
                    } else 0 to null

                    lastDistanceKm = distKm
                    lastPoints = points
                    timerRunning = false
                },
                enabled = (unlimitedTime || timeLeft > 0) && guess != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .zIndex(1f)
            ) { Text("Bestätigen") }
        } else {
            Button(
                onClick = {
                    onConfirmGuess(
                        RoundResult(
                            points = lastPoints ?: 0,
                            distanceKm = lastDistanceKm ?: 0.0
                        )
                    )
                    // Reset für nächste Runde
                    guess = null
                    truth = null
                    lastPoints = null
                    lastDistanceKm = null
                    timeLeft = if (unlimitedTime) Int.MAX_VALUE else roundSeconds
                    timerRunning = !unlimitedTime
                    hintsUsed = 0
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .zIndex(1f)
            ) { Text("Weiter") }
        }
    }
}
