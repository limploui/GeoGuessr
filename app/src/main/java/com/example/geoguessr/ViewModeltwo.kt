package com.example.geoguessr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.geoguessr.data.MapillaryClient
import kotlinx.coroutines.launch
import kotlin.random.Random

class ViewModeltwo(application: Application) : AndroidViewModel(application) {

    private val mapillaryClient = MapillaryClient(application)

    // === NEU: Liste von Regionen (jede: [minLon, minLat, maxLon, maxLat]) ===
    private var regions: List<DoubleArray> = emptyList()
    private var regionNames: List<String> = emptyList() // optional, gleich lang wie regions

    // Falls nichts gesetzt wurde, nutze diese Defaults:
    private val defaultRegions = listOf(
        doubleArrayOf(13.4030, 52.5190, 13.4068, 52.5210), // Berlin Mitte
        doubleArrayOf(11.5675, 48.1355, 11.5795, 48.1430), // München Altstadt
        doubleArrayOf(9.9860, 53.5435, 10.0050, 53.5538),  // Hamburg City
        doubleArrayOf(6.9400, 50.9325, 6.9650, 50.9465)    // Köln Innenstadt
    )
    private val defaultNames = listOf("Berlin Mitte", "München Altstadt", "Hamburg City", "Köln Innenstadt")

    private val _image = MutableLiveData<MapillaryClient.ImageData?>()
    val image: LiveData<MapillaryClient.ImageData?> = _image

    private val _currentBbox = MutableLiveData<DoubleArray>() // [minLon, minLat, maxLon, maxLat]
    val currentBbox: LiveData<DoubleArray> = _currentBbox

    // optional: aktuellen Regionsnamen exponieren
    private val _currentRegionName = MutableLiveData<String?>()
    val currentRegionName: LiveData<String?> = _currentRegionName

    /** Regionen setzen (optional Namen gleich lang wie bboxes). */
    fun setRegions(bboxes: List<DoubleArray>, names: List<String>? = null) {
        regions = if (bboxes.isNotEmpty()) bboxes else defaultRegions
        regionNames = when {
            names != null && names.size == regions.size -> names
            else -> List(regions.size) { "Region ${it + 1}" }
        }
    }

    /**
     * Lädt ein zufälliges Bild (Pano bevorzugt) aus einer zufällig gewählten Region.
     * Falls in der Region nichts gefunden wird, wird die BBox schrittweise vergrößert (wie bisher).
     * Merkt sich die tatsächlich verwendete BBox der Runde.
     */
    fun loadRandomImage() {
        viewModelScope.launch {
            // Fallback auf Defaults, falls der Aufrufer nichts gesetzt hat
            if (regions.isEmpty()) {
                setRegions(defaultRegions, defaultNames)
            }

            // Zufällige Region wählen
            val idx = Random.nextInt(regions.size)
            var bboxArray = regions[idx].copyOf()
            _currentRegionName.value = regionNames.getOrNull(idx)

            var attempt = 0
            var result: MapillaryClient.ImageData? = null

            while (result == null && attempt < 5) {
                val bboxString = "${bboxArray[0]},${bboxArray[1]},${bboxArray[2]},${bboxArray[3]}"

                // 1) Panoramen bevorzugen
                result = mapillaryClient.getRandomPano(bboxString)

                // 2) Fallback auf beliebiges Bild
                if (result == null) {
                    result = mapillaryClient.getRandomImage(bboxString, onlyPanorama = null)
                }

                // 3) Wenn nichts: BBox leicht vergrößern
                if (result == null) {
                    attempt++
                    bboxArray = expandBbox(bboxArray, 0.002 * attempt)
                }
            }

            _image.value = result
            _currentBbox.value = bboxArray // tatsächlich genutzte BBox dieser Runde
        }
    }

    /** BBox vergrößern */
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
