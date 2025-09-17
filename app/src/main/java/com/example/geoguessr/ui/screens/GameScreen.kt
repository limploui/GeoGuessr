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
import kotlin.math.roundToInt

/**
 * Final
 *
 * GameScreen
 *
 * Verantwortlich für:
 * - Streetview-/Mapillary-Ansicht (WebView)
 * - Tipps (Hint-Tab) anzeigen und zählen
 * - Karte zur Eingabe des Tipps (Tap setzt Marker)
 * - Countdown / Zeitverwaltung
 * - Punkteberechnung & Distanzberechnung
 * - Tab-Wechsel (Streetview, Tipps, Karte)
 * - Bestätigen des Tipps und Weiter zur nächsten Runde
 */
@Composable
fun GameScreen(
    accessToken: String,                        // Mapillary Access Token (nur Token, kein "OAuth ")
    imageId: String,                            // aktuell zu zeigendes Mapillary Bild/Panorama
    bbox: DoubleArray?,                         // Bounding Box der aktiven Region [minLon, minLat, maxLon, maxLat]
    trueLocation: Pair<Double, Double>?,        // wahre Position (lat, lon); optional (sonst BBox-Mittelpunkt)
    roundSeconds: Int = 60,                     // Zeitlimit in Sekunden (oder Int.MAX_VALUE für ∞)
    isHintMode: Boolean = false,                // Schaltet den Tipps-Tab ein
    hints: List<String> = emptyList(),          // Liste der Hinweise für die aktuelle Region
    onConfirmGuess: (RoundResult) -> Unit       // Callback am Ende der Runde (Punkte + Distanz)
) {
    // ---- UI-Tab-Logik -------------------------------------------------------
    // 0 = Streetview, 1 = Tipps (nur im Hint-Mode sichtbar), 2 = Karte
    var tab by remember { mutableStateOf(0) }

    // ---- In-Runden-Status ---------------------------------------------------
    // Spieler-Tipp (lat, lon); "saveable", damit z. B. bei Recomposition/Rotation erhalten bleibt
    var guess by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }

    // Wahrheitspunkt, den wir nach dem Bestätigen einblenden (z. B. BBox-Mittelpunkt)
    var truth by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }

    // Punkte der Runde, null solange noch nicht bestätigt
    var lastPoints by rememberSaveable { mutableStateOf<Int?>(null) }

    // Distanz der Runde in km, null solange noch nicht bestätigt
    var lastDistanceKm by rememberSaveable { mutableStateOf<Double?>(null) }

    // ---- Zeitmodus (Countdown vs. ∞) ----------------------------------------
    val unlimitedTime = roundSeconds == Int.MAX_VALUE

    // Restzeit in Sekunden (oder Int.MAX_VALUE für ∞). Reset bei neuem imageId.
    var timeLeft by remember(imageId, unlimitedTime) {
        mutableStateOf(if (unlimitedTime) Int.MAX_VALUE else roundSeconds)
    }

    // Steuert, ob der Timer tickt (bei ∞ von Anfang an aus)
    var timerRunning by remember(imageId, unlimitedTime) { mutableStateOf(!unlimitedTime) }

    // ---- Hints / Tipps ------------------------------------------------------
    // Zählt, wie viele Tipps der Spieler bereits aufgedeckt hat (0..5)
    var hintsUsed by rememberSaveable(imageId) { mutableStateOf(0) }

    // Wenn der Hint-Tab nicht verfügbar ist (Normalmodus), und wir gerade darauf sind,
    // springe sicherheitshalber zurück auf Streetview
    LaunchedEffect(isHintMode) {
        if (!isHintMode && tab == 1) tab = 0
        // Wenn der 2. Tab im Normalmodus die Karte ist, und man von Hint-Mode kommt:
        if (!isHintMode && tab == 2) tab = 1
    }

    // ---- Countdown-Logik ----------------------------------------------------
    // Tick jede Sekunde, solange:
    // - ein Zeitlimit existiert
    // - Timer läuft
    // - noch kein Ergebnis feststeht
    LaunchedEffect(imageId, timerRunning, lastPoints, unlimitedTime) {
        if (unlimitedTime || !timerRunning || lastPoints != null) return@LaunchedEffect
        while (timeLeft > 0 && lastPoints == null && timerRunning) {
            delay(1000)
            timeLeft -= 1
        }
        // Zeit ist abgelaufen -> falls schon geraten wurde, Ergebnis automatisch berechnen
        if (timeLeft <= 0 && lastPoints == null) {
            val center = bbox?.let { GeoUtils.bboxCenter(it) } // Fallback-Ziel: Mittelpunkt der BBox
            val target = trueLocation ?: center
            if (guess != null && target != null) {
                truth = center
                val dKm = GeoUtils.haversineKm(guess!!.first, guess!!.second, target.first, target.second)
                lastDistanceKm = dKm

                // Basispunkte 0..5000 invers zur Distanz
                val basePoints = GeoUtils.scoreFromDistanceKm(dKm)

                // Multiplikator im Hinweis-Modus (6..1) je nach genutzten Tipps,
                // im Normalmodus 1.0 (kein Boost)
                val mult = if (isHintMode) GeoUtils.hintMultiplier(hintsUsed) else 1.0

                // Endpunkte runden und auf 5000 deckeln (Variante B)
                lastPoints = (basePoints * mult).roundToInt().coerceAtMost(5000)
            } else {
                // Kein Tipp gesetzt -> 0 Punkte, keine Distanz
                truth = null
                lastDistanceKm = null
                lastPoints = 0
            }
            timerRunning = false
        }
    }

    // ---- UI: Grundlayout ----------------------------------------------------
    Column(Modifier.fillMaxSize()) {

        // Tabs (Straße/Tipps/Karte) – im Normalmodus nur 2 Tabs
        val tabTitles = if (isHintMode)
            listOf("Streetview", "Tipps", "Karte")
        else
            listOf("Streetview", "Karte")

        TabRow(
            selectedTabIndex = tab,
            modifier = Modifier.zIndex(2f) // überlagert Inhalte leicht (Schatten/Trennung)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
            }
        }

        // Kopfzeile mit Zeit – zeigt ∞ bei ausgeschaltetem Zeitlimit
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

        // Hauptbereich: Streetview / Tipps / Karte
        Box(
            Modifier
                .weight(1f)   // nimmt den restlichen verfügbaren Platz ein
                .zIndex(0f)
        ) {
            when (tab) {
                // 0) Streetview/Mapillary-Viewer
                0 -> MapillaryViewer(
                    accessToken = accessToken,
                    imageId = imageId,
                    modifier = Modifier.fillMaxSize()
                )

                // 1) Tipps (nur im Hint-Mode sichtbar)
                1 -> {
                    if (isHintMode) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            // so viele Tipps anzeigen, wie bereits angefordert wurden
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
                                    // max. 5 Tipps nutzbar und nicht mehr als vorhanden
                                    if (hintsUsed < 5 && hintsUsed < hints.size) hintsUsed++
                                },
                                enabled = lastPoints == null && hintsUsed < min(5, hints.size)
                            ) { Text("Tipp anzeigen") }
                        }
                    } else {
                        // Falls Tab 1 im Normalmodus: Karte statt Tipps
                        OsmdroidMap(
                            modifier = Modifier.fillMaxSize(),
                            bbox = bbox,
                            guessPoint = guess,
                            truthPoint = truth,
                            onTap = { lat, lon -> guess = Pair(lat, lon) } // Tipp setzen
                        )
                    }
                }

                // 2) Karte (nur im Hint-Mode; im Normalmodus ist sie Tab 1)
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

        // ---- Aktionen: Bestätigen / Weiter ----------------------------------
        if (lastPoints == null) {
            // Noch kein Ergebnis -> Button „Bestätigen“
            Button(
                onClick = {
                    if (guess == null) return@Button

                    val center = bbox?.let { GeoUtils.bboxCenter(it) }
                    truth = center
                    val target = trueLocation ?: center

                    // Punkte + Distanz berechnen (nur wenn Ziel bekannt)
                    val (points, distKm) = if (target != null) {
                        val dKm = GeoUtils.haversineKm(
                            guess!!.first, guess!!.second,
                            target.first, target.second
                        )
                        val base = GeoUtils.scoreFromDistanceKm(dKm) // 0..5000
                        val mult = if (isHintMode) GeoUtils.hintMultiplier(hintsUsed) else 1.0
                        ((base * mult).roundToInt().coerceAtMost(5000)) to dKm
                    } else 0 to null

                    lastDistanceKm = distKm
                    lastPoints = points
                    timerRunning = false
                },
                enabled = (unlimitedTime || timeLeft > 0) && guess != null, // nur mit Tipp & ggf. Restzeit
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .zIndex(1f)
            ) { Text("Bestätigen") }
        } else {
            // Ergebnis liegt vor -> Button „Weiter“ (nächste Runde / Endscreen)
            Button(
                onClick = {
                    // Ergebnis nach „oben“ melden
                    onConfirmGuess(
                        RoundResult(
                            points = lastPoints ?: 0,
                            distanceKm = lastDistanceKm ?: 0.0
                        )
                    )
                    // Zustand für nächste Runde zurücksetzen
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
