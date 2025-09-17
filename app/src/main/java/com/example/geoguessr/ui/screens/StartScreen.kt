// ui/screens/StartScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.geoguessr.R

/**
 * Final
 * StartScreen
 * - Titel + zwei Karten zur Auswahl des Modus
 * - Weltbild am unteren Rand mit kleinem Abstand nach unten
 */
@Composable
fun StartScreen(
    onChooseNormal: () -> Unit,   // Callback „Normales Spiel“
    onChooseHint: () -> Unit,     // Callback „Hinweis Spiel“
) {
    val bottomOffset = 8.dp       // Feintuning: Abstand unter dem Bild

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App-Titel
        Text(
            "GeoGuezzr",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 32.dp, bottom = 48.dp)
        )

        // Modus-Auswahl
        ModeCard("Normales Spiel", onChooseNormal)
        Spacer(Modifier.height(16.dp))
        ModeCard("Hinweis Spiel", onChooseHint)

        // Restlichen Platz füllen → Bild rutscht nach unten
        Spacer(Modifier.weight(1f))

        // Weltbild (unten)
        Image(
            painter = painterResource(R.drawable.welt),
            contentDescription = "welt.png",
            modifier = Modifier.fillMaxWidth(0.7f)
        )

        // Kleiner Abstand zum unteren Rand
        Spacer(Modifier.height(bottomOffset))
    }
}

/** Kleine Karte als klickbarer Modus-Button. */
@Composable
private fun ModeCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFCC80),
            contentColor = Color.Black
        )
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
