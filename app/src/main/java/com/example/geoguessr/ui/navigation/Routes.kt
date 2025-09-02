// ui/navigation/Routes.kt
package com.example.geoguessr.ui.navigation

sealed class Route(val path: String) {
    object Start : Route("start")
    object Setup : Route("setup/{mode}") {
        fun create(mode: String) = "setup/$mode"
    }
    object Game : Route("game")
    object Result : Route("result") // Runden-Ergebnis
    object End : Route("end")       // Spielende
}
