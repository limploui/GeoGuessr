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

        // 1) Eigene Regionen setzen (kannst du frei erweitern/ändern)
        viewModelTwo.setRegions(
            bboxes = listOf(
                doubleArrayOf(13.4030, 52.5190, 13.4068, 52.5210), // Berlin Mitte
                doubleArrayOf(11.5675, 48.1355, 11.5795, 48.1430), // München Altstadt
                doubleArrayOf(9.9860, 53.5435, 10.0050, 53.5538),  // Hamburg City
                doubleArrayOf(6.9400, 50.9325, 6.9650, 50.9465)    // Köln Innenstadt
            ),
            names = listOf("Berlin Mitte", "München Altstadt", "Hamburg City", "Köln Innenstadt")
        )

        // 2) Gleich beim Start ein Bild laden (aus zufälliger Region)
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
                            val item by viewModelTwo.image.observeAsState()
                            val bbox by viewModelTwo.currentBbox.observeAsState()

                            if (item?.id != null) {
                                val trueLoc: Pair<Double, Double>? =
                                    if (item!!.lat != 0.0 || item!!.lon != 0.0) Pair(item!!.lat, item!!.lon) else null

                                GameScreen(
                                    accessToken = accessToken,
                                    imageId = item!!.id,
                                    bbox = bbox,
                                    trueLocation = trueLoc,
                                    onConfirmGuess = { points ->
                                        vm.finishRound(points)
                                        if (vm.isFinished()) {
                                            nav.navigate(Route.End.path) {
                                                popUpTo(Route.Start.path) { inclusive = false }
                                            }
                                        } else {
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
