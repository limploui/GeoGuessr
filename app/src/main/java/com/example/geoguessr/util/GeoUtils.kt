// util/GeoUtils.kt
package com.example.geoguessr.util

import kotlin.math.*

object GeoUtils {
    /** bbox = [minLon, minLat, maxLon, maxLat] -> Mittelpunkt (lat, lon) */
    fun bboxCenter(bbox: DoubleArray): Pair<Double, Double> {
        val lon = (bbox[0] + bbox[2]) / 2.0
        val lat = (bbox[1] + bbox[3]) / 2.0
        return Pair(lat, lon)
    }

    /** Haversine-Distanz in km */
    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        return 2 * R * asin(min(1.0, sqrt(a)))
    }

    /**
     * Punkteberechnung abhängig von Distanz.
     * - Standard: maxScore = 5000 (Normalmodus)
     * - Hinweis-Modus: maxScore = 333 (wird mit Multiplikator hochskaliert)
     */
    fun scoreFromDistanceKm(dKm: Double, maxScore: Int = 5000): Int {
        val cutoff = 2000.0
        if (dKm >= cutoff) return 0
        val x = 1.0 - (dKm / cutoff) // 1..0
        val s = maxScore * x * x
        return s.roundToInt().coerceIn(0, maxScore)
    }

    /** Hinweis-Multiplikator */
    fun hintMultiplier(hintsUsed: Int): Int {
        return when (hintsUsed) {
            0 -> 6
            1 -> 5
            2 -> 4
            3 -> 3
            4 -> 2
            else -> 1
        }
    }
}
