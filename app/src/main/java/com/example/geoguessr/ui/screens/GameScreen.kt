// ui/screens/GameScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geoguessr.data.MapillaryViewer
import com.example.geoguessr.ui.map.OsmdroidMap
import com.example.geoguessr.util.GeoUtils

@Composable
fun GameScreen(
    accessToken: String,
    imageId: String,
    bbox: DoubleArray?,                         // [minLon, minLat, maxLon, maxLat]
    trueLocation: Pair<Double, Double>?,        // (lat, lon) – echte Bild-Position
    onConfirmGuess: (points: Int) -> Unit
) {
    var tab by remember { mutableStateOf(0) } // 0 Streetview, 1 Karte

    var guess by remember { mutableStateOf<Pair<Double, Double>?>(null) }  // (lat, lon)
    var truth by remember { mutableStateOf<Pair<Double, Double>?>(null) }  // (lat, lon) – Anzeige (BBox-Center)
    var lastPoints by remember { mutableStateOf<Int?>(null) }
    var lastDistanceKm by remember { mutableStateOf<Double?>(null) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Streetview") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Karte") })
        }

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
                    truthPoint = truth, // wir setzen das erst beim Bestätigen
                    onTap = { lat, lon -> guess = Pair(lat, lon) }
                )
            }
        }

        if (lastPoints == null) {
            Button(
                onClick = {
                    val b = bbox
                    val g = guess
                    if (g != null) {
                        // 1) Für die Karte: Mittelpunkt der BBox als "roter" Marker (Vergleich)
                        val center = b?.let { GeoUtils.bboxCenter(it) }
                        truth = center

                        // 2) Für die Wertung: echte Bild-Position bevorzugen, sonst BBox-Center
                        val target = trueLocation ?: center
                        if (target != null) {
                            val dKm = GeoUtils.haversineKm(
                                g.first, g.second,
                                target.first, target.second
                            )
                            lastDistanceKm = dKm
                            lastPoints = GeoUtils.scoreFromDistanceKm(dKm)
                        }
                    }
                },
                enabled = guess != null,  // Tipp muss gesetzt sein
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Bestätigen")
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Distanz: ${"%.1f".format(lastDistanceKm ?: 0.0)} km")
                Spacer(Modifier.height(4.dp))
                Text("Punkte: ${lastPoints!!}")
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onConfirmGuess(lastPoints!!)
                        // lokalen State resetten (nächste Runde)
                        guess = null
                        truth = null
                        lastPoints = null
                        lastDistanceKm = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Weiter")
                }
            }
        }
    }
}
