// ui/screens/EndScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EndScreen(
    totalPoints: Int,
    onNewGame: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ende", style = MaterialTheme.typography.headlineMedium)
        Text("Punkte: $totalPoints", style = MaterialTheme.typography.titleLarge)
        Button(onClick = onNewGame, modifier = Modifier.fillMaxWidth()) {
            Text("Neues Spiel")
        }
    }
}
