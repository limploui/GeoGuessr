// com/example/geoguessr/data/MapillaryApi.kt
package com.example.geoguessr.data

import retrofit2.http.GET
import retrofit2.http.Query

interface MapillaryApi {
    // Nur Header-Auth. Keine access_token-Query mehr!
    @GET("images")
    suspend fun getImages(
        @Query("bbox") bbox: String,
        @Query("fields") fields: String = "id,thumb_1024_url",
        @Query("limit") limit: Int = 200
    ): MapillaryResponse
}

data class MapillaryResponse(
    val data: List<MapillaryImage>?
)

data class MapillaryImage(
    val id: String?,
    val thumb_1024_url: String?
)
