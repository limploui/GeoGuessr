// ui/screens/SetupScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.geoguessr.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale



@Composable
fun SetupScreen(
    modeTitle: String,
    initialRounds: Int = 1,
    initialSeconds: Int = 60,
    onStart: (rounds: Int, seconds: Int) -> Unit
) {
    var rounds by remember { mutableStateOf(initialRounds) }
    var seconds by remember { mutableStateOf(initialSeconds) }
    var noTimeLimit by remember { mutableStateOf(false) }   // Toggle für Zeitlimit

    // Tuning-Parameter für die Bildbox unten
    val IMAGE_WIDTH_FRACTION = 0.7f   // wie breit das Bild unten relativ zur Screenbreite sein soll
    val ASPECT_RATIO = 1.6f           // Breite / Höhe des Bildes (z.B. 16:10 ≈ 1.6; 16:9 wäre ≈ 1.78)
    val MIN_IMAGE_HEIGHT = 240.dp     // untere Klammer
    val MAX_IMAGE_HEIGHT = 360.dp     // obere Klammer

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Höhe der Bildbox so bestimmen, dass sie zur gewünschten Bildbreite passt:
        // height = (width * fraction) / aspect
        val desiredWidth = maxWidth * IMAGE_WIDTH_FRACTION
        val desiredHeight = desiredWidth / ASPECT_RATIO
        val bottomHeight = desiredHeight.coerceIn(MIN_IMAGE_HEIGHT, MAX_IMAGE_HEIGHT)

        // OBERER BEREICH: scrollt; bekommt unten Padding, damit er nicht in die Bildbox ragt
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomHeight)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modeTitle,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 32.dp, bottom = 48.dp),
                textAlign = TextAlign.Center
            )

            // Runden
            SettingCard(
                title = "Runden",
                valueText = rounds.toString(),
                onMinus = { rounds = (rounds - 1).coerceAtLeast(1) },
                onPlus  = { rounds = (rounds + 1).coerceAtMost(10) }
            )

            Spacer(Modifier.height(16.dp))

            // Zeit (mit rotem X-Button)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                elevation = CardDefaults.cardElevation(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFCC80),
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Zeit", style = MaterialTheme.typography.titleLarge)
                        FilledTonalButton(
                            onClick = { noTimeLimit = !noTimeLimit },
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (noTimeLimit) Color.Gray else Color.Red,
                                contentColor = Color.White
                            )
                        ) { Text("x", style = MaterialTheme.typography.titleMedium) }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SquareActionButton(
                            text = "−",
                            onClick = { seconds = (seconds - 10).coerceAtLeast(10) },
                            enabled = !noTimeLimit
                        )
                        ValueBox(valueText = if (noTimeLimit) "∞" else "${seconds}s")
                        SquareActionButton(
                            text = "+",
                            onClick = { seconds = (seconds + 10).coerceAtMost(600) },
                            enabled = !noTimeLimit
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            // (Bild ist unten fixiert – hier kein Bild mehr)
        }

        // UNTERER BEREICH: Bild fix am unteren Rand, Höhe wie berechnet
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomHeight),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(IMAGE_WIDTH_FRACTION)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.welt),
                    contentDescription = "Weltkarte",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                ElevatedButton(
                    onClick = { onStart(rounds, if (noTimeLimit) Int.MAX_VALUE else seconds) },
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
            containerColor = Color(0xFFFFCC80),
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
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SquareActionButton("−", onMinus)
                ValueBox(valueText)
                SquareActionButton("+", onPlus)
            }
        }
    }
}

@Composable
private fun SquareActionButton(
    text: String,
    onClick: () -> Unit,
    size: Dp = 40.dp,
    enabled: Boolean = true
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(size),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color(0xFFFAFAFA),
            disabledContentColor = Color(0xFF9E9E9E)
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
