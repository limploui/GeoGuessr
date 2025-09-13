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
                doubleArrayOf(6.9400, 50.9325, 6.9650, 50.9465),   // Köln Innenstadt

                doubleArrayOf(21.17869, 38.70138, 21.22435, 38.74557), // Amvrakia Griechenland
                doubleArrayOf(-8.02156, 53.39113, -7.81797, 53.45741), // Athlone Irland
                doubleArrayOf(-0.45374, 46.76485, -0.40285, 46.78390), // Boisme Frankreich
                doubleArrayOf(-3.08321, 48.20202, -2.98141, 48.23908), // Caurel Frankreich
                doubleArrayOf(-2.62550, 51.31660, -2.60005, 51.32529), // Chew Valley Lake England
                doubleArrayOf(-8.29614, 52.12840, -8.24524, 52.14547), // Fermoy Irland
                doubleArrayOf(-9.54618, 52.04225, -9.44438, 52.07645), // Killarney Irland
                doubleArrayOf(-2.46180, 47.84755, -2.36000, 47.88487), // La Chapelle Caro Frankreich
                doubleArrayOf(13.16445, 41.31105, 13.21535, 41.33194), // La Fiora Italien
                doubleArrayOf(4.09636, 43.62215, 4.29995, 43.70263),    // Marsillargues Frankreich

                doubleArrayOf(13.27096, 38.05762, 13.31662, 38.10221), // Monreale Sizilien Italien
                doubleArrayOf(15.63595, 38.22321, 15.64737, 38.23434), // Pezzo Superiore Italien
                doubleArrayOf(13.553996, 41.448589, 13.566720, 41.453799), // Pico Italien
                doubleArrayOf(16.60781, 40.64813, 16.61923, 40.65888), // Pini Italien
                doubleArrayOf(5.19979, 43.39183, 5.30159, 43.43223),   // Saint Victoret Frankreich
                doubleArrayOf(14.73936, 40.66814, 14.79026, 40.68923), // Salerno Italien
                doubleArrayOf(14.28909, 40.84016, 14.31454, 40.85068), // San Giovanni A Teduccio Italien
                doubleArrayOf(16.27187, 38.25200, 16.31753, 38.29647), // Siderno Italien
                doubleArrayOf(14.4220, 35.8064, 14.6046, 35.9900),     // Valetta Malta
                doubleArrayOf(-2.64105, 50.93334, -2.61560, 50.94210)  // Yeovil England
            ),
            names = listOf(
                "Berlin Mitte",
                "München Altstadt",
                "Hamburg City",
                "Köln Innenstadt",
                "Amvrakia GR",
                "Athlone IR",
                "Boisme FR",
                "Caurel FR",
                "Chew Valley Lake UK",
                "Fermoy IR",
                "Killarney IR",
                "La Chapelle Caro FR",
                "La Fiora IT",
                "Marsillargues FR",

                "Monreale Sizilien IT",
                "Pezzo Superiore IT",
                "Pico IT",
                "Pini IT",
                "Saint Victoret FR",
                "Salerno IT",
                "San Giovanni A Teduccio IT",
                "Siderno IT",
                "Valetta Malta",
                "Yeovil UK")
        )

        // 2) Gleich beim Start ein Bild laden (aus zufälliger Region)
        //viewModelTwo.loadRandomImage()

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
                                initialSeconds = vm.roundSeconds, // ⏱️ neu
                                onStart = { rounds, seconds ->
                                    vm.setTotalRounds(rounds)
                                    vm.setRoundSeconds(seconds)   // ⏱️ neu
                                    vm.startGame()
                                    viewModelTwo.loadRandomImageClearing()
                                    nav.navigate(Route.Game.path)
                                }
                            )
                        }

                        composable(Route.Game.path) {
                            val item by viewModelTwo.image.observeAsState()
                            val bbox by viewModelTwo.currentBbox.observeAsState()
                            val secs = vm.roundSeconds  // ⏱️

                            if (item?.id != null) {
                                val trueLoc: Pair<Double, Double>? =
                                    if (item!!.lat != 0.0 || item!!.lon != 0.0) Pair(item!!.lat, item!!.lon) else null

                                GameScreen(
                                    accessToken = accessToken,
                                    imageId = item!!.id,
                                    bbox = bbox,
                                    trueLocation = trueLoc,
                                    roundSeconds = secs,        // ⏱️ neu
                                    onConfirmGuess = { points ->
                                        vm.finishRound(points)
                                        if (vm.isFinished()) {
                                            nav.navigate(Route.End.path) {
                                                popUpTo(Route.Start.path) { inclusive = false }
                                            }
                                        } else {
                                            viewModelTwo.loadRandomImageClearing()
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
