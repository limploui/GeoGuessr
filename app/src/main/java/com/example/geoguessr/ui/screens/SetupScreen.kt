// ui/screens/SetupScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SetupScreen(
    modeTitle: String,
    initialRounds: Int = 1,
    initialSeconds: Int = 60,           // ⏱️ neu
    onStart: (rounds: Int, seconds: Int) -> Unit  // ⏱️ neu
) {
    var rounds by remember { mutableStateOf(initialRounds) }
    var seconds by remember { mutableStateOf(initialSeconds) } // 10..600

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(modeTitle, style = MaterialTheme.typography.headlineMedium)

        // Runden
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { rounds = (rounds - 1).coerceAtLeast(1) }) { Text("-") }
            Text("  Runden $rounds  ", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { rounds = (rounds + 1).coerceAtMost(10) }) { Text("+") }
        }

        // ⏱️ Timer (sek)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { seconds = (seconds - 10).coerceAtLeast(10) }) { Text("-") }
            Text("  Zeit: ${seconds}s  ", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { seconds = (seconds + 10).coerceAtMost(600) }) { Text("+") }
        }

        Button(onClick = { onStart(rounds, seconds) }, modifier = Modifier.fillMaxWidth()) {
            Text("Los geht’s")
        }
    }
}
