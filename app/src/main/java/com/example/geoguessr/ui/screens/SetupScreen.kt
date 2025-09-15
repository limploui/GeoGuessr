// ui/screens/SetupScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.geoguessr.R

@Composable
fun SetupScreen(
    modeTitle: String,
    initialRounds: Int = 1,
    initialSeconds: Int = 60,
    onStart: (rounds: Int, seconds: Int) -> Unit
) {
    var rounds by remember { mutableStateOf(initialRounds) }
    var seconds by remember { mutableStateOf(initialSeconds) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Überschrift wie im Startscreen
        Text(
            modeTitle,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 32.dp, bottom = 48.dp),
            textAlign = TextAlign.Center
        )

        // Cards im gleichen Look wie Startscreen-Buttons
        SettingCard(
            title = "Runden",
            valueText = rounds.toString(),
            onMinus = { rounds = (rounds - 1).coerceAtLeast(1) },
            onPlus = { rounds = (rounds + 1).coerceAtMost(10) }
        )

        Spacer(Modifier.height(16.dp))

        SettingCard(
            title = "Zeit",
            valueText = "${seconds}s",
            onMinus = { seconds = (seconds - 10).coerceAtLeast(10) },
            onPlus = { seconds = (seconds + 10).coerceAtMost(600) }
        )

        Spacer(Modifier.height(16.dp))

        // Bild wie im Startscreen (70% Breite) + Button direkt darauf (zentriert)
        Box(
            modifier = Modifier.fillMaxWidth(0.7f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.welt),
                contentDescription = "Weltkarte",
                modifier = Modifier.fillMaxWidth()
            )

            // Weißer Button auf der Karte
            ElevatedButton(
                onClick = { onStart(rounds, seconds) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text("Los geht’s", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    valueText: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFCC80), // helles Orange
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Titel links nimmt flexiblen Platz
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            // Rechte Steuerleiste mit fixer Gesamtbreite (überall gleich)
            Row(
                modifier = Modifier.width(200.dp), // bei Bedarf anpassen
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SquareActionButton("−", onMinus)          // fester Button
                ValueBox(valueText)                        // Wert mit Mindestbreite
                SquareActionButton("+", onPlus)            // fester Button
            }
        }
    }
}

@Composable
private fun SquareActionButton(
    text: String,
    onClick: () -> Unit,
    size: Dp = 40.dp
) {
    // Quadratischer Button → immer gleiche Breite/Höhe
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(size),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ValueBox(
    valueText: String,
    minWidth: Dp = 64.dp
) {
    Box(
        modifier = Modifier.widthIn(min = minWidth),
        contentAlignment = Alignment.Center
    ) {
        Text(
            valueText,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            maxLines = 1
        )
    }
}
