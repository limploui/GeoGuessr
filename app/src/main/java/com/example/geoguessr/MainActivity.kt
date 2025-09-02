// MainActivity.kt
package com.example.geoguessr

import com.example.geoguessr.ui.navigation.Route
import com.example.geoguessr.ui.screens.*
import com.example.geoguessr.game.GameViewModel
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geoguessr.game.GameMode
import com.example.geoguessr.ui.screens.*
import com.example.geoguessr.ui.theme.GeoGuessrTheme

class MainActivity : AppCompatActivity() {

    private val vm: GameViewModel by viewModels()
    private val viewModelTwo: ViewModeltwo by viewModels()

    // Setze hier dein echtes Mapillary-Token ein
    private val accessToken = "MLY|25128393533414969|53cc9f3a61d67b7e6648f080f4cdff1d"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // gleich beim Start ein Panorama laden
        viewModelTwo.loadRandomImage()

        setContent {
            GeoGuessrTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = Route.Start.path) {

                        composable(Route.Start.path) {
                            StartScreen(
                                onChooseNormal = {
                                    vm.setMode(GameMode.NORMAL)
                                    nav.navigate(Route.Setup.create("Normales Spiel"))
                                },
                                onChooseHint = {
                                    vm.setMode(GameMode.HINT)
                                    nav.navigate(Route.Setup.create("Hinweis Spiel"))
                                }
                            )
                        }

                        composable(Route.Setup.path) { backStack ->
                            val modeTitle = backStack.arguments?.getString("mode") ?: "Spiel"
                            SetupScreen(
                                modeTitle = modeTitle,
                                initialRounds = vm.totalRounds,
                                onStart = { rounds ->
                                    vm.setTotalRounds(rounds)
                                    vm.startGame()
                                    // Für die erste Runde neues Bild laden
                                    viewModelTwo.loadRandomImage()
                                    nav.navigate(Route.Game.path)
                                }
                            )
                        }

                        composable(Route.Game.path) {
                            // Dynamische Image-Daten aus ViewModeltwo
                            val item by viewModelTwo.image.observeAsState()

                            if (item?.id != null) {
                                GameScreen(
                                    accessToken = accessToken,
                                    imageId = item!!.id,  // <-- dynamische ID
                                    onConfirmGuess = { points ->
                                        vm.finishRound(points)
                                        if (vm.isFinished()) {
                                            nav.navigate(Route.End.path) {
                                                popUpTo(Route.Start.path) { inclusive = false }
                                            }
                                        } else {
                                            // Für nächste Runde neues Bild laden
                                            viewModelTwo.loadRandomImage()
                                            nav.navigate(Route.Result.path)
                                        }
                                    }
                                )
                            } else {
                                Text("🔄 Lade Panorama...")
                            }
                        }

                        composable(Route.Result.path) {
                            ResultScreen(
                                roundPoints = vm.lastRoundPoints,
                                onNextRound = {
                                    nav.navigate(Route.Game.path) {
                                        popUpTo(Route.Game.path) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Route.End.path) {
                            EndScreen(
                                totalPoints = vm.totalPoints,
                                onNewGame = {
                                    // Neustart: Punkte zurücksetzen und neues Bild laden
                                    vm.startGame()
                                    viewModelTwo.loadRandomImage()
                                    nav.navigate(Route.Start.path) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
