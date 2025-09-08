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
     * Punkte von Distanz (0–10.000). Quadratischer Falloff:
     * 0 km ≈ 10.000 | 50 km ≈ 9.875 | 200 km ≈ 9.000 | 500 km ≈ 6.250 | 1.000 km ≈ 2.500 | >= 2.000 km → 0.
     */
    fun scoreFromDistanceKm(dKm: Double): Int {
        val max = 10000.0
        val cutoff = 2000.0
        if (dKm >= cutoff) return 0
        val x = 1.0 - (dKm / cutoff)         // 1..0
        val s = max * x * x                   // quadratisch
        return s.roundToInt()
    }
}
