package com.example.geoguessr.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MapillaryClient(context: Context) {
    // Final
    // Hier werden Auth, Logging, Fehlerbehandlung und Feld-Auswahl gekapselt, ähnlich wie Backend.
    // Dein Client-Token (kein OAuth nötig)
    private val clientToken: String = "MLY|25128393533414969|53cc9f3a61d67b7e6648f080f4cdff1d"

    // Logging (nur für Debug, nicht in Produktion)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Fügt jedem Request den Authorization-Header hinzu
    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "OAuth $clientToken")
            .addHeader("Accept", "application/json")
            .build()
        chain.proceed(req)
    }

    // HTTP-Client mit Interceptors
    // Timeouts & Retry: stabileres Netzverhalten.
    // Der HTTP-Client macht die eigentliche Arbeit der Netzwerkkommunikation.
    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .retryOnConnectionFailure(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Retrofit-API-Client
    // Basis-URL, JSON-Konverter, API-Interface
    // Diese Instanz wird für alle API-Aufrufe verwendet.
    private val api: MapillaryApi = Retrofit.Builder()
        .baseUrl("https://graph.mapillary.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MapillaryApi::class.java)


    // Vereinfachte Bilddaten fürs UI
    // Imagedata ist die vereinfachte Datenklasse, die nur die für die App relevanten Felder enthält.
    data class ImageData(
        val id: String,
        val lon: Double,
        val lat: Double,
        val headingDeg: Double?,
        val thumbUrl: String,
        val isPano: Boolean
    )


     // Zufälliges Panorama (strict).
     //Gibt null zurück, wenn kein Panorama gefunden wurde.
     //Bbox-Format: "minLon,minLat,maxLon,maxLat" (ohne Leerzeichen)
     //Viel log für das Debugging.Haben wir Panos, wenn neinn, warum nicht?

    suspend fun getRandomPano(bbox: String): ImageData? =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getImages(
                    bbox = bbox.replace(" ", ""),
                    fields = "id,computed_geometry,computed_compass_angle,thumb_1024_url,is_pano",
                    limit = 200
                )

                val panoItems = res.data.orEmpty()
                    .filter { it.id != null && it.thumb_1024_url != null && it.computed_geometry?.coordinates?.size == 2 }
                    .filter { it.is_pano == true }

                android.util.Log.d("GeoGuessr", "Gefundene Panoramen: ${panoItems.size}")

                val pick = panoItems.randomOrNull() ?: return@withContext null
                val coords = pick.computed_geometry!!.coordinates!!

                ImageData(
                    id = pick.id!!,
                    lon = coords[0],
                    lat = coords[1],
                    headingDeg = pick.computed_compass_angle,
                    thumbUrl = pick.thumb_1024_url!!,
                    isPano = true
                )
            } catch (e: Exception) {
                android.util.Log.e("GeoGuessr", "Fehler bei getRandomPano", e)
                null
            }
        }

    // Allgemeines Bild – kann Panoramen oder Non-Panos zurückgeben.
    suspend fun getRandomImage(bbox: String, onlyPanorama: Boolean? = null): ImageData? =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getImages(
                    bbox = bbox.replace(" ", ""),
                    fields = "id,computed_geometry,computed_compass_angle,thumb_1024_url,is_pano",
                    limit = 200
                )

                val items = res.data.orEmpty()
                    .filter { it.id != null && it.thumb_1024_url != null && it.computed_geometry?.coordinates?.size == 2 }
                    .filter { img ->
                        when (onlyPanorama) {
                            true -> img.is_pano == true
                            false -> img.is_pano == false
                            null -> true
                        }
                    }

                android.util.Log.d("GeoGuessr", "Gefundene Bilder: ${items.size}")

                val pick = items.randomOrNull() ?: return@withContext null
                val coords = pick.computed_geometry!!.coordinates!!

                ImageData(
                    id = pick.id!!,
                    lon = coords[0],
                    lat = coords[1],
                    headingDeg = pick.computed_compass_angle,
                    thumbUrl = pick.thumb_1024_url!!,
                    isPano = pick.is_pano == true
                )
            } catch (e: Exception) {
                android.util.Log.e("GeoGuessr", "Fehler bei getRandomImage", e)
                null
            }
        }

    // Haversine-Distanz (km)
    //Für das Spiel brauchen wir die Entfernung zwischen zwei Punkten auf der Erde.
    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        return 2 * R * asin(min(1.0, sqrt(a)))
    }


    // Nur die URL eines zufälligen Bildes in der Bbox (für Widget)
    // Gibt null zurück, wenn kein Bild gefunden wurde oder ein Fehler auftritt
    suspend fun getRandomImageUrl(bbox: String): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val cleanBbox = bbox.replace(" ", "")
            val res = api.getImages(
                bbox = cleanBbox,
                fields = "id,thumb_1024_url",
                limit = 200
            )
            android.util.Log.d("GeoGuessr", "Image count: ${res.data?.size}")
            val randomUrl = res.data?.mapNotNull { it.thumb_1024_url }?.randomOrNull()
            randomUrl
        } catch (_: Exception) {
            null
        }
    }



}
