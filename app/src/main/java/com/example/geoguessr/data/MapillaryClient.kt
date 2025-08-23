// MapillaryClient.kt
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

class MapillaryClient(context: Context) {

    // Dein Client-Token (kein OAuth nötig)
    private val clientToken: String = "MLY|25128393533414969|53cc9f3a61d67b7e6648f080f4cdff1d"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Auth-Header nur noch im Header, NICHT mehr als Query-Param
    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "OAuth $clientToken")
            .addHeader("Accept", "application/json")
            .build()
        chain.proceed(req)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .retryOnConnectionFailure(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: MapillaryApi = Retrofit.Builder()
        .baseUrl("https://graph.mapillary.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MapillaryApi::class.java)

    /**
     * Holt eine zufällige thumb_1024_url in einer BBox.
     * BBox-Format: left,bottom,right,top (minLon,minLat,maxLon,maxLat)
     * Tipp: BBox klein halten (z.B. ~200–500 m Kantenlänge).
     */
    suspend fun getRandomImageUrl(bbox: String): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val cleanBbox = bbox.replace(" ", "")
            val res = api.getImages(
                bbox = cleanBbox,
                fields = "id,thumb_1024_url",
                limit = 200
            )
            android.util.Log.d("GeoGuessr", "API Response: $res")
            android.util.Log.d("GeoGuessr", "Image count: ${res.data?.size}")
            android.util.Log.d("GeoGuessr", "Image URLs: ${res.data?.map { it.thumb_1024_url }}")
            val randomUrl = res.data?.mapNotNull { it.thumb_1024_url }?.randomOrNull()
            android.util.Log.d("GeoGuessr", "Gewählte zufällige URL: $randomUrl")
            android.util.Log.d("GeoGuessr", "Image URL: $randomUrl")
            randomUrl
        } catch (_: Exception) {
            null
        }
    }
}
