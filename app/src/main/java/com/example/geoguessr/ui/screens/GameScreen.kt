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

@Composable
fun GameScreen(
    accessToken: String,
    imageId: String,
    bbox: DoubleArray?,
    trueLocation: Pair<Double, Double>?,   // (lat, lon)
    roundSeconds: Int = 60,                // ⏱️ neu: aus VM durchreichen
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

    // ⏱️ Countdown-Effect – läuft, bis 0 oder bestätigt
    LaunchedEffect(imageId, timerRunning, lastPoints) {
        if (!timerRunning || lastPoints != null) return@LaunchedEffect
        while (timeLeft > 0 && lastPoints == null && timerRunning) {
            delay(1000)
            timeLeft -= 1
        }
        if (timeLeft <= 0 && lastPoints == null) {
            // Auto-Bestätigen: Truth setzen (BBox-Mittelpunkt), Punkte berechnen
            val center = bbox?.let { GeoUtils.bboxCenter(it) }
            truth = center
            val target = trueLocation ?: center
            val pts = if (guess != null && target != null) {
                val dKm = GeoUtils.haversineKm(guess!!.first, guess!!.second, target.first, target.second)
                lastDistanceKm = dKm
                GeoUtils.scoreFromDistanceKm(dKm)
            } else {
                lastDistanceKm = null
                0
            }
            lastPoints = pts
            timerRunning = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Tabs
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Streetview") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Karte") })
        }

        // ⏱️ Timeranzeige
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Zeit: ${timeLeft}s")
            if (lastPoints != null) Text("Punkte: ${lastPoints}")
        }

        // Inhalt
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> MapillaryViewer(accessToken = accessToken, imageId = imageId, modifier = Modifier.fillMaxSize())
                1 -> OsmdroidMap(
                    modifier = Modifier.fillMaxSize(),
                    bbox = bbox,
                    guessPoint = guess,
                    truthPoint = truth,
                    onTap = { lat, lon -> guess = Pair(lat, lon) }
                )
            }
        }

        // Buttons
        if (lastPoints == null) {
            Button(
                onClick = {
                    val center = bbox?.let { GeoUtils.bboxCenter(it) }
                    truth = center
                    val target = trueLocation ?: center
                    if (guess != null && target != null) {
                        val dKm = GeoUtils.haversineKm(guess!!.first, guess!!.second, target.first, target.second)
                        lastDistanceKm = dKm
                        lastPoints = GeoUtils.scoreFromDistanceKm(dKm)
                    } else {
                        lastDistanceKm = null
                        lastPoints = 0
                    }
                    timerRunning = false
                },
                enabled = timeLeft > 0, // nach Ablauf nicht mehr klickbar
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("Bestätigen") }
        } else {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                lastDistanceKm?.let { Text("Distanz: ${"%.1f".format(it)} km") }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onConfirmGuess(lastPoints!!)
                        // lokalen State für nächste Runde zurücksetzen
                        guess = null
                        truth = null
                        lastPoints = null
                        lastDistanceKm = null
                        timeLeft = roundSeconds
                        timerRunning = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Weiter") }
            }
        }
    }
}
