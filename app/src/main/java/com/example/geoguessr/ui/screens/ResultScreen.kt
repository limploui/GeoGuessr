// ui/screens/ResultScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ResultScreen:
 * Zeigt das Ergebnis einer Runde:
 * - Punkte dieser Runde
 * - Distanz des Tipps zur Lösung
 * - Button zur nächsten Runde
 */
@Composable
fun ResultScreen(
    roundPoints: Int,            // Punkte der letzten Runde
    roundDistanceKm: Double,     // Distanz der letzten Runde in km
    onNextRound: () -> Unit      // Callback: weiter zur nächsten Runde
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Überschrift
        Text("Karte – Ergebnis", style = MaterialTheme.typography.headlineMedium)

        // Punkte
        Text(
            "Deine Punktzahl: $roundPoints",
            style = MaterialTheme.typography.titleLarge
        )

        // Distanz formatiert mit 1 Nachkommastelle
        Text(
            "Entfernung: ${"%.1f".format(roundDistanceKm)} km",
            style = MaterialTheme.typography.titleMedium
        )

        // Weiter-Button
        Button(
            onClick = onNextRound,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nächste Runde")
        }
    }
}
