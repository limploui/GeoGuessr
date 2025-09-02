// ui/screens/StartScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StartScreen(
    onChooseNormal: () -> Unit,
    onChooseHint: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("GeoGuezzr", style = MaterialTheme.typography.headlineLarge)

        ModeCard("Normales Spiel", onChooseNormal)
        ModeCard("Hinweis Spiel", onChooseHint)
    }
}

@Composable private fun ModeCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation()
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
