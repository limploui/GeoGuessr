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

        Spacer(Modifier.height(32.dp)) // Abstand nach unten
        Image(
            painter = painterResource(R.drawable.welt),
            contentDescription = "welt.png",
            modifier = Modifier.fillMaxWidth(0.7f)
        )
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


