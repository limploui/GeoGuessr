// ui/screens/EndScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geoguessr.game.RoundResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.geoguessr.R


@Composable
fun EndScreen(
    totalPoints: Int,
    totalDistanceKm: Double,
    results: List<RoundResult>,
    onNewGame: () -> Unit
) {
    // gleiche Bottom-Bild-Logik wie im SetupScreen
    val IMAGE_WIDTH_FRACTION = 0.7f
    val ASPECT_RATIO = 1.6f
    val MIN_IMAGE_HEIGHT = 240.dp
    val MAX_IMAGE_HEIGHT = 360.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val desiredWidth = maxWidth * IMAGE_WIDTH_FRACTION
        val desiredHeight = desiredWidth / ASPECT_RATIO
        val bottomHeight = desiredHeight.coerceIn(MIN_IMAGE_HEIGHT, MAX_IMAGE_HEIGHT)

        // OBERER BEREICH: Inhalt + scrollbare Ergebnisliste
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomHeight) // Platz für die fixierte Bildbox unten
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ende", style = MaterialTheme.typography.headlineMedium)
            Text("Gesamtpunkte: $totalPoints", style = MaterialTheme.typography.titleLarge)

            // Optional: kurze Zusammenfassung oben
            Text(
                "Gesamtentfernung: ${"%.1f".format(totalDistanceKm)} km",
                style = MaterialTheme.typography.titleMedium
            )

            // Scrollbare Ergebnisliste in Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 320.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Kopfzeile
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Runde")
                        Text("Distanz")
                        Text("Ergebnis")
                    }
                    Divider()

                    // Einzelne Runden
                    results.forEachIndexed { idx, r ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${idx + 1}")
                            Text("${"%.1f".format(r.distanceKm)} km")
                            Text("${r.points}")
                        }
                    }

                    Divider(Modifier.padding(top = 8.dp))
                    // Summen
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gesamt")
                        Text("${"%.1f".format(totalDistanceKm)} km")
                        Spacer(Modifier.width(4.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$totalPoints")
                    }
                }
            }
            // hier KEIN regulärer Button – der sitzt unten auf der Weltkarte
            Spacer(Modifier.height(8.dp))
        }

        // UNTERER BEREICH: Weltkarte fix, Button zentriert darauf
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomHeight),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(IMAGE_WIDTH_FRACTION)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.welt),
                    contentDescription = "Weltkarte",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                ElevatedButton(
                    onClick = onNewGame,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text("Neues Spiel", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

