package com.example.geoguessr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.geoguessr.data.MapillaryClient
import kotlinx.coroutines.launch

class ViewModeltwo(application: Application) : AndroidViewModel(application) {

    private val mapillaryClient = MapillaryClient(application)

    // Start-BBox (Berlin Mitte)
    private val baseBbox = doubleArrayOf(13.4030, 52.5190, 13.4068, 52.5210)

    private val _image = MutableLiveData<MapillaryClient.ImageData?>()
    val image: MutableLiveData<MapillaryClient.ImageData?> = _image

    /**
     * Lädt zuerst ein zufälliges Panorama-Bild.
     * Falls kein Panorama gefunden wird, wird auf normales Bild zurückgefallen.
     */
    fun loadRandomImage() {
        viewModelScope.launch {
            var bboxArray = baseBbox
            var attempt = 0
            var result: MapillaryClient.ImageData? = null

            while (result == null && attempt < 5) {
                val bboxString = "${bboxArray[0]},${bboxArray[1]},${bboxArray[2]},${bboxArray[3]}"

                // 1️⃣ Versuche Panoramen
                result = mapillaryClient.getRandomPano(bboxString)

                // 2️⃣ Falls keine Panoramen → normales Bild nehmen
                if (result == null) {
                    android.util.Log.d("GeoGuessr", "❌ Kein Pano gefunden – fallback zu normalem Bild")
                    result = mapillaryClient.getRandomImage(bboxString, onlyPanorama = null)
                }

                if (result == null) {
                    attempt++
                    bboxArray = expandBbox(bboxArray, 0.002 * attempt)
                    android.util.Log.d("GeoGuessr", "Nichts gefunden, vergrößere BBox: $bboxArray")
                }
            }

            _image.value = result
            if (result != null) {
                android.util.Log.d("GeoGuessr", "✅ Bild gefunden: ${result.id} (Pano/Normal)")
            } else {
                android.util.Log.e("GeoGuessr", "❌ Auch nach $attempt Versuchen kein Bild gefunden.")
            }
        }
    }

    /**
     * Hilfsfunktion: BBox vergrößern
     */
    private fun expandBbox(bbox: DoubleArray, delta: Double): DoubleArray {
        val (minLon, minLat, maxLon, maxLat) = bbox
        return doubleArrayOf(
            minLon - delta,
            minLat - delta,
            maxLon + delta,
            maxLat + delta
        )
    }
}
