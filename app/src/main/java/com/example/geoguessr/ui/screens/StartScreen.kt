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

@Composable
fun StartScreen(
    onChooseNormal: () -> Unit,
    onChooseHint: () -> Unit,
) {
    // Wie viel höher das Bild sitzen soll (anpassbar)
    val bottomOffset = 8.dp   // z.B. 24dp; bei Bedarf 16–32dp testen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "GeoGuezzr",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 32.dp, bottom = 48.dp)
        )

        ModeCard("Normales Spiel", onChooseNormal)
        Spacer(Modifier.height(16.dp))
        ModeCard("Hinweis Spiel", onChooseHint)

        // Schiebt das Bild nach unten
        Spacer(Modifier.weight(1f))

        Image(
            painter = painterResource(R.drawable.welt),
            contentDescription = "welt.png",
            modifier = Modifier.fillMaxWidth(0.7f)
        )

        // NEU: kleiner Spacer unter dem Bild → Bild sitzt etwas höher
        Spacer(Modifier.height(bottomOffset))
    }
}


@Composable
private fun ModeCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFCC80), // grüner Button
            contentColor = Color.Black        // weiße Schrift
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


