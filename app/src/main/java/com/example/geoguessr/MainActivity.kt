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

    private val accessToken = "MLY|25128393533414969|53cc9f3a61d67b7e6648f080f4cdff1d"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 0) Hints definieren (max. 5 pro Region werden genutzt)
        val HINTS: Map<String, List<String>> = mapOf(
            // Deutschland
            "Berlin Mitte" to listOf(
                "Liegt in Ostdeutschland.",
                "Eine große Spree fließt durch die Stadt.",
                "Sitz von Regierung und Parlament.",
                "Berühmtes Tor ist das Wahrzeichen.",
                "Fernsehturm prägt die Skyline."
            ),
            "München Altstadt" to listOf(
                "Liegt in Süddeutschland.",
                "Großes Volksfest im Herbst.",
                "Dialekt: Bairisch.",
                "Berühmtes Glockenspiel am zentralen Platz.",
                "Fußballklub in Rot ist weltbekannt."
            ),
            "Hamburg City" to listOf(
                "Großstadt im Norden.",
                "Großer Hafen an der Elbe.",
                "Backstein-Speicherstadt.",
                "Berühmte Vergnügungsmeile.",
                "Konzerthaus mit gläserner Krone."
            ),
            "Köln Innenstadt" to listOf( // deine Wünsche
                "Liegt in Westdeutschland.",
                "Da fließt der Rhein durch.",
                "Karneval ist voll wichtig da.",
                "Auf Latein heißt es 'Colonia'.",
                "Da steht der Dom."
            ),

            // Frankreich
            "Boisme FR" to listOf(
                "Westfrankreich, ländlich.",
                "Bocage-Landschaft.",
                "Zwischen Nantes und Poitiers.",
                "Viehzucht verbreitet.",
                "Kleine Pfarrkirche im Ort."
            ),
            "Caurel FR" to listOf(
                "Bretagne.",
                "Nahe dem Lac de Guerlédan.",
                "Granit und Schiefer.",
                "Keltischer Einfluss.",
                "Beliebt zum Wandern."
            ),
            "La Chapelle Caro FR" to listOf(
                "Ebenfalls Bretagne.",
                "Landwirtschaftlich geprägt.",
                "Kleine Kapellen.",
                "Bretonische Traditionen.",
                "Zwischen Ploërmel und Malestroit."
            ),
            "Saint Victoret FR" to listOf(
                "Provence, Südfrankreich.",
                "Neben großer Mittelmeerstadt.",
                "Nahe am Flughafen.",
                "Zypressen und Pinien.",
                "Viel Sonne, mildes Klima."
            ),
            "Marsillargues FR" to listOf(
                "Okzitanien, nahe Mittelmeer.",
                "Zwischen Montpellier und Nîmes.",
                "Camargue-Ebene mit Wein.",
                "Römische Spuren in der Region.",
                "Mistral weht mitunter."
            ),

            // Irland
            "Athlone IR" to listOf(
                "Ziemlich in der Landesmitte.",
                "Fluss Shannon teilt die Stadt.",
                "Historische Burg am Ufer.",
                "Brücken verbinden die Ufer.",
                "Zwischen Galway und Dublin."
            ),
            "Fermoy IR" to listOf(
                "Süden, County Cork.",
                "Fluss Blackwater.",
                "Brücke und alte Mühlen.",
                "Grüne Weiden.",
                "Nähe zu Cork City."
            ),
            "Killarney IR" to listOf(
                "Südwesten, County Kerry.",
                "Nationalpark mit Seen.",
                "Berge ringsum.",
                "Touristischer Hotspot.",
                "Ringstraße in der Nähe."
            ),

            // Italien
            "La Fiora IT" to listOf(
                "Mittelitalien (Latium).",
                "Hügelige Landschaft.",
                "Oliven & Wein.",
                "Tuffsteindörfer.",
                "Nähe zum Tyrrhenischen Meer."
            ),
            "Monreale Sizilien IT" to listOf(
                "Große Mittelmeerinsel.",
                "Blick auf eine Hafenmetropole.",
                "Berühmte Kathedrale mit Goldmosaiken.",
                "Arabisch-normannische Architektur.",
                "Zitronen- und Orangenhaine."
            ),
            "Pezzo Superiore IT" to listOf(
                "Südlich vom Festlandende.",
                "Über der Straße von Messina.",
                "Hügelige, dichte Bebauung.",
                "Palmen & Mittelmeerflora.",
                "Nähe Reggio Calabria."
            ),
            "Pico IT" to listOf(
                "Latium, Zentralitalien.",
                "Burgdorf auf Hügel.",
                "Natursteinmauern.",
                "Olivenhaine ringsum.",
                "Ruhige Kleinstadt."
            ),
            "Pini IT" to listOf(
                "Süditalien (Apulien/Basilikata).",
                "Sanfte Hügel.",
                "Kiefern/Pinien namensgebend.",
                "Kalkstein & Trockenmauern.",
                "Ionisches Meer nicht weit."
            ),
            "Salerno IT" to listOf(
                "Tyrrhenische Küste.",
                "Tor zur Amalfiküste.",
                "Lange Promenade.",
                "Zitronen berühmt.",
                "Fährhafen."
            ),
            "San Giovanni A Teduccio IT" to listOf(
                "Teil einer großen Süditalien-Metropole.",
                "Bucht mit Vulkan in Sicht.",
                "Dichte Bebauung.",
                "Industriell geprägt.",
                "Kaffee- & Pizza-Kultur."
            ),
            "Siderno IT" to listOf(
                "Kalabrien, Ionische Küste.",
                "Langer Strand.",
                "Promenade mit Palmen.",
                "Altstadt im Hinterland.",
                "Wein & Zitrusfrüchte."
            ),

            // Griechenland & Malta
            "Amvrakia GR" to listOf(
                "Westgriechenland, Binnenland.",
                "Seen & Lagunen.",
                "Viele Olivenbäume.",
                "Mediterranes Klima.",
                "Antike Spuren."
            ),
            "Valletta Malta" to listOf( // Schreibweise korrigiert
                "Mittelmeer-Inselstaat.",
                "Historische Hauptstadt auf Halbinsel.",
                "Sandfarbener Kalkstein prägt.",
                "Mächtige Festungsanlagen.",
                "Zentraler Busknotenpunkt."
            ),

            // UK
            "Chew Valley Lake UK" to listOf(
                "Südwesten Englands.",
                "Großer Stausee.",
                "Vogelbeobachtung beliebt.",
                "Nähe Bristol/Bath.",
                "Grüne Hügel & Hecken."
            ),
            "Yeovil UK" to listOf(
                "Südwesten Englands (Somerset).",
                "Luftfahrt-Tradition.",
                "Kleinstadt mit Einkaufsstraßen.",
                "Backstein/Sandstein-Bauten.",
                "Eigener Bahnhof."
            )
        )

        // 1) Regionen + Hints setzen
        viewModelTwo.setRegions(
            bboxes = listOf(
                doubleArrayOf(13.4030, 52.5190, 13.4068, 52.5210), // Berlin Mitte
                doubleArrayOf(11.5675, 48.1355, 11.5795, 48.1430), // München Altstadt
                doubleArrayOf(9.9860, 53.5435, 10.0050, 53.5538),  // Hamburg City
                doubleArrayOf(6.9400, 50.9325, 6.9650, 50.9465),   // Köln Innenstadt
                doubleArrayOf(21.17869, 38.70138, 21.22435, 38.74557), // Amvrakia GR
                doubleArrayOf(-8.02156, 53.39113, -7.81797, 53.45741), // Athlone IR
                doubleArrayOf(-0.45374, 46.76485, -0.40285, 46.78390), // Boisme FR
                doubleArrayOf(-3.08321, 48.20202, -2.98141, 48.23908), // Caurel FR
                doubleArrayOf(-2.62550, 51.31660, -2.60005, 51.32529), // Chew Valley Lake UK
                doubleArrayOf(-8.29614, 52.12840, -8.24524, 52.14547), // Fermoy IR
                doubleArrayOf(-9.54618, 52.04225, -9.44438, 52.07645), // Killarney IR
                doubleArrayOf(-2.46180, 47.84755, -2.36000, 47.88487), // La Chapelle Caro FR
                doubleArrayOf(13.16445, 41.31105, 13.21535, 41.33194), // La Fiora IT
                doubleArrayOf(4.09636, 43.62215, 4.29995, 43.70263),   // Marsillargues FR
                doubleArrayOf(13.27096, 38.05762, 13.31662, 38.10221), // Monreale Sizilien IT
                doubleArrayOf(15.63595, 38.22321, 15.64737, 38.23434), // Pezzo Superiore IT
                doubleArrayOf(13.553996, 41.448589, 13.566720, 41.453799), // Pico IT
                doubleArrayOf(16.60781, 40.64813, 16.61923, 40.65888), // Pini IT
                doubleArrayOf(5.19979, 43.39183, 5.30159, 43.43223),   // Saint Victoret FR
                doubleArrayOf(14.73936, 40.66814, 14.79026, 40.68923), // Salerno IT
                doubleArrayOf(14.28909, 40.84016, 14.31454, 40.85068), // San Giovanni A Teduccio IT
                doubleArrayOf(16.27187, 38.25200, 16.31753, 38.29647), // Siderno IT
                doubleArrayOf(14.4220, 35.8064, 14.6046, 35.9900),     // Valletta Malta
                doubleArrayOf(-2.64105, 50.93334, -2.61560, 50.94210)  // Yeovil UK
            ),
            names = listOf(
                "Berlin Mitte", "München Altstadt", "Hamburg City", "Köln Innenstadt",
                "Amvrakia GR", "Athlone IR", "Boisme FR", "Caurel FR", "Chew Valley Lake UK",
                "Fermoy IR", "Killarney IR", "La Chapelle Caro FR", "La Fiora IT", "Marsillargues FR",
                "Monreale Sizilien IT", "Pezzo Superiore IT", "Pico IT", "Pini IT",
                "Saint Victoret FR", "Salerno IT", "San Giovanni A Teduccio IT", "Siderno IT",
                "Valletta Malta", "Yeovil UK"
            ),
            hintsByName = HINTS // ✅ neu
        )

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
                                initialSeconds = vm.roundSeconds,
                                onStart = { rounds, seconds ->
                                    vm.setTotalRounds(rounds)
                                    vm.setRoundSeconds(seconds)
                                    vm.startGame()
                                    viewModelTwo.loadRandomImageClearing()
                                    nav.navigate(Route.Game.path)
                                }
                            )
                        }

                        composable(Route.Game.path) {
                            val item by viewModelTwo.image.observeAsState()
                            val bbox by viewModelTwo.currentBbox.observeAsState()
                            val regionHints by viewModelTwo.currentHints.observeAsState(emptyList())
                            val secs = vm.roundSeconds
                            val isHint = (vm.mode == GameMode.HINT)

                            if (item?.id != null) {
                                val trueLoc: Pair<Double, Double>? =
                                    if (item!!.lat != 0.0 || item!!.lon != 0.0) Pair(item!!.lat, item!!.lon) else null

                                GameScreen(
                                    accessToken = accessToken,
                                    imageId = item!!.id,
                                    bbox = bbox,
                                    trueLocation = trueLoc,
                                    roundSeconds = secs,
                                    isHintMode = isHint,
                                    hints = regionHints,
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
                                    vm.startGame()
                                    // kein direktes Laden hier – erst im Setup
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