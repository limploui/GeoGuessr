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
     * Distanz -> Punkte mit quadratischem Abfall bis cutoff km.
     * maxScore bleibt das absolute Maximum (z.B. 5000 wie im Normalmodus).
     */
    fun distanceScore(dKm: Double, maxScore: Int = 5000, cutoffKm: Double = 2000.0): Int {
        if (dKm >= cutoffKm) return 0
        val x = 1.0 - (dKm / cutoffKm)        // 1..0
        return (maxScore * x * x).roundToInt().coerceIn(0, maxScore)
    }

    /**
     * Hinweis-Abschlag (0..1]: 0 Tipps=1.0, 1=5/6≈0.833, 2=4/6≈0.667, 3=0.5, 4≈0.333, 5≈0.167.
     * So bleibt das theoretische Maximum = maxScore (wie Normalmodus), wird aber mit Tipps reduziert.
     */
    fun hintPenalty(hintsUsed: Int): Double {
        val h = hintsUsed.coerceIn(0, 5)
        return (6 - h) / 6.0
    }
}
