// com/example/geoguessr/data/MapillaryApi.kt
package com.example.geoguessr.data

import retrofit2.http.GET
import retrofit2.http.Query
//Final
// Wie reden wir mit Mapillary API. --> Serververtrag.
// Gibt an, was für Endpunkte es gibt und welche Parameter.
interface MapillaryApi {

    // Beispiel-Call: /images?bbox=...&fields=id,computed_geometry,computed_compass_angle,thumb_1024_url,is_pano&limit=200
    //Jedes Query taucht in der URL auf
    // Wenn getImages() aufgerufen wird, dann soll Retrofit einen HTTP-GET-Request auf /images machen
    @GET("images")
    suspend fun getImages(
        @Query("bbox") bbox: String,
        @Query("fields") fields: String =
            "id,computed_geometry,computed_compass_angle,thumb_1024_url,is_pano",
        @Query("limit") limit: Int = 200,
        // optionaler Filter – darf nur mit bbox/creator_username kombiniert werden
        @Query("is_pano") isPano: Boolean? = null
    ): MapillaryResponse
}

/** Top-Level Antwort der Graph API: {"data":[ ... ]} */
//Die Anwort der API wird in diese Datenklassen gemappt
data class MapillaryResponse(
    val data: List<MapillaryImage>?
)

/** Entspricht den angefragten Feldern in `fields=` */
//Hier die Datenklasse im Detail
data class MapillaryImage(
    val id: String?,
    val computed_geometry: GeoJSONPoint?,     // {"type":"Point","coordinates":[lon,lat]}
    val computed_compass_angle: Double?,      // Blickrichtung in Grad
    val thumb_1024_url: String?,              // Bild-URL (zeitlich befristet)
    val is_pano: Boolean?                     // true = 360°
)

/** GeoJSON Point, wie von Mapillary geliefert */
//Die Mapillary-API (genauer: die Graph API) verwendet den Standard GeoJSON für Geokoordinaten.
//Das heißt: egal ob ein Punkt, eine Linie oder ein Polygon – alles kommt in diesem einheitlichen JSON-Schema.
data class GeoJSONPoint(
    val type: String?,
    val coordinates: List<Double>?            // [lon, lat]
)
