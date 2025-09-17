// ui/navigation/Routes.kt
package com.example.geoguessr.ui.navigation


// Definiert die verschiedenen Routen/Pfade für die Navigation in der App.
// Jede Route hat einen eindeutigen Pfad (path), der in der Navigation verwendet wird.
sealed class Route(val path: String) {
    object Start : Route("start")
    object Setup : Route("setup/{mode}") {
        fun create(mode: String) = "setup/$mode"
    }
    object Game : Route("game")
    object Result : Route("result") // Runden-Ergebnis
    object End : Route("end")       // Spielende
}
