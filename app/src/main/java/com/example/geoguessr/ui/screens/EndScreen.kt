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

@Composable
fun EndScreen(
    totalPoints: Int,
    totalDistanceKm: Double,
    results: List<RoundResult>,
    onNewGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ende", style = MaterialTheme.typography.headlineMedium)
        Text("Gesamtpunkte: $totalPoints", style = MaterialTheme.typography.titleLarge)

        // ⇩ Scrollbares Fenster
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp, max = 320.dp) // begrenzte Höhe, scrollbar
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Kopfzeile
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Runde")
                    Text("Distanz")
                    Text("Ergebnis")
                }
                Divider()

                // Einzelne Runden
                results.forEachIndexed { idx, r ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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

        Button(onClick = onNewGame, modifier = Modifier.fillMaxWidth()) {
            Text("Neues Spiel")
        }
    }
}
