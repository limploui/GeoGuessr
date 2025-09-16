// ui/screens/ResultScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResultScreen(
    roundPoints: Int,
    roundDistanceKm: Double,   // ⬅️ neu
    onNextRound: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Karte – Ergebnis", style = MaterialTheme.typography.headlineMedium)

        Text(
            "Deine Punktzahl: $roundPoints",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            "Entfernung: ${"%.1f".format(roundDistanceKm)} km",
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            onClick = onNextRound,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nächste Runde")
        }
    }
}
