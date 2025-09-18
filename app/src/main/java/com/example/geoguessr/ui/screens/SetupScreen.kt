// ui/screens/SetupScreen.kt
package com.example.geoguessr.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

/**
 *
 * Final
 * SetupScreen
 * - Oben: Titel, Runden- und Zeit-Einstellungen (scrollbar)
 * - Unten: Weltbild fixiert mit „Los geht’s“-Button
 */
@Composable
fun SetupScreen(
    modeTitle: String,
    initialRounds: Int = 1,
    initialSeconds: Int = 60,
    onStart: (rounds: Int, seconds: Int) -> Unit
) {
    // Letzte *endliche* Zeit merken (für den Fall, dass ∞ wieder ausgeschaltet wird)
    var lastTimedSeconds by rememberSaveable { mutableStateOf(60) }

    // Aus initialSeconds ableiten, ob „kein Zeitlimit“ aktiv ist
    var noTimeLimit by rememberSaveable { mutableStateOf(initialSeconds == Int.MAX_VALUE) }

    // Gezeigte Sekunden sind NIE MAX_VALUE – bei ∞ zeigen wir das Zeichen an
    var seconds by rememberSaveable {
        mutableStateOf(
            if (initialSeconds == Int.MAX_VALUE) lastTimedSeconds else initialSeconds.coerceIn(10, 600)
        )
    }

    var rounds by rememberSaveable { mutableStateOf(initialRounds.coerceIn(1, 10)) }

    // Falls du per Navigation zurückkommst und andere Startwerte mitbringst → synchronisieren
    LaunchedEffect(initialSeconds, initialRounds) {
        rounds = initialRounds.coerceIn(1, 10)
        if (initialSeconds == Int.MAX_VALUE) {
            noTimeLimit = true
            // seconds bleibt ein normaler Wert; Anzeige zeigt "∞"
        } else {
            noTimeLimit = false
            seconds = initialSeconds.coerceIn(10, 600)
            lastTimedSeconds = seconds
        }
    }

    // Layout-Tuning für die Bildfläche unten (Breite/Höhe anpassbar)
    val imageWidthFraction = 0.7f
    val aspectRatio = 1.6f
    val minImageHeight = 240.dp
    val maxImageHeight = 360.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val desiredWidth = maxWidth * imageWidthFraction
        val desiredHeight = desiredWidth / aspectRatio
        val bottomHeight = desiredHeight.coerceIn(minImageHeight, maxImageHeight)

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

            // Runden,die man nach Bedarf anpassen kann
            // (min 1, max 10)
            SettingCard(
                title = "Runden",
                valueText = rounds.toString(),
                onMinus = { rounds = (rounds - 1).coerceAtLeast(1) },
                onPlus  = { rounds = (rounds + 1).coerceAtMost(10) }
            )

            Spacer(Modifier.height(16.dp))

            // Zeit (mit rotem X-Button)
            // (min 10s, max 600s, oder ∞)
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
                            onClick = {
                                if (!noTimeLimit) {
                                    // ∞ einschalten → aktuelle finite Zeit merken
                                    lastTimedSeconds = seconds.coerceIn(10, 600)
                                    noTimeLimit = true
                                } else {
                                    // ∞ ausschalten → letzte finite Zeit wiederherstellen
                                    noTimeLimit = false
                                    seconds = lastTimedSeconds.coerceIn(10, 600)
                                }
                            },
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
                            onClick = {
                                seconds = (seconds - 10).coerceAtLeast(10)
                                lastTimedSeconds = seconds // damit beim nächsten Re-Enable der gleiche Wert wiederkommt
                            },
                            enabled = !noTimeLimit
                        )
                        ValueBox(valueText = if (noTimeLimit) "∞" else "${seconds}s")
                        SquareActionButton(
                            text = "+",
                            onClick = {
                                seconds = (seconds + 10).coerceAtMost(600)
                                lastTimedSeconds = seconds
                            },
                            enabled = !noTimeLimit
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Unteres Weltbild + Start
        // Button auf der Weltkarte zentriert
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomHeight),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(imageWidthFraction)
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
                    onClick = { onStart(rounds, if (noTimeLimit) Int.MAX_VALUE else seconds.coerceIn(10, 600)) },
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

// Hilfskomponenten unverändert, nur in diesen File verschoben
// Darauf basieren die SettingCards oben
// (Runden, Zeit)
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

// Quadratischer Button mit Text in der Mitte
// (für + und −)
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

// Box mit Mindestbreite, die den Wert (Runden, Zeit) anzeigt
// (damit die Buttons nicht wandern, wenn die Zahl sich ändert)
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
