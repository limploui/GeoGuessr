// ui/screens/GameScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geoguessr.data.MapillaryViewer

@Composable
fun GameScreen(
    accessToken: String,
    imageId: String,
    onConfirmGuess: (points: Int) -> Unit
) {
    var tab by remember { mutableStateOf(0) } // 0 Streetview, 1 Karte

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
                1 -> Box(Modifier.fillMaxSize().padding(24.dp)) {
                    Text("Karte (Placeholder) – hier später Map einbinden")
                }
            }
        }

        Button(
            onClick = { onConfirmGuess((5000..10000).random()) }, // Dummy-Scoring
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Bestätigen")
        }
    }
}
