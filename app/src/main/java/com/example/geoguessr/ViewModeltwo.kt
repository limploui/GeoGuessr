package com.example.geoguessr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.geoguessr.data.MapillaryClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

class ViewModeltwo(application: Application) : AndroidViewModel(application) {

    // --- Hints ---
    private var regionHints: Map<String, List<String>> = emptyMap()
    private val _currentHints = MutableLiveData<List<String>>(emptyList())
    val currentHints: LiveData<List<String>> = _currentHints

    // --- Region/Last selection ---
    private var lastRegionIdx: Int = -1
    private var lastImageId: String? = null

    private val mapillaryClient = MapillaryClient(application)

    // --- Lade-Schutz (Schritt 2) ---
    private var currentLoadJob: Job? = null
    private var requestSeq: Long = 0L

    // --- Regionen ---
    private var regions: List<DoubleArray> = emptyList() // [minLon, minLat, maxLon, maxLat]
    private var regionNames: List<String> = emptyList()

    // Defaults
    private val defaultRegions = listOf(
        doubleArrayOf(13.4030, 52.5190, 13.4068, 52.5210), // Berlin Mitte
        doubleArrayOf(11.5675, 48.1355, 11.5795, 48.1430), // München Altstadt
        doubleArrayOf(9.9860, 53.5435, 10.0050, 53.5538),  // Hamburg City
        doubleArrayOf(6.9400, 50.9325, 6.9650, 50.9465),   // Köln Innenstadt
        doubleArrayOf(21.17869, 38.70138, 21.22435, 38.74557), // Amvrakia Griechenland
        doubleArrayOf(-8.02156, 53.39113, -7.81797, 53.45741), // Athlone Irland
        doubleArrayOf(-0.45374, 46.76485, -0.40285, 46.78390), // Boisme Frankreich
        doubleArrayOf(-3.08321, 48.20202, -2.98141, 48.23908), // Caurel Frankreich
        doubleArrayOf(-2.62550, 51.31660, -2.60005, 51.32529), // Chew Valley Lake England
        doubleArrayOf(-8.29614, 52.12840, -8.24524, 52.14547), // Fermoy Irland
        doubleArrayOf(-9.54618, 52.04225, -9.44438, 52.07645), // Killarney Irland
        doubleArrayOf(-2.46180, 47.84755, -2.36000, 47.88487), // La Chapelle Caro Frankreich
        doubleArrayOf(13.16445, 41.31105, 13.21535, 41.33194), // La Fiora Italien
        doubleArrayOf(4.09636, 43.62215, 4.29995, 43.70263),   // Marsillargues Frankreich
        doubleArrayOf(13.27096, 38.05762, 13.31662, 38.10221), // Monreale Sizilien Italien
        doubleArrayOf(15.63595, 38.22321, 15.64737, 38.23434), // Pezzo Superiore Italien
        doubleArrayOf(13.553996, 41.448589, 13.566720, 41.453799), // Pico Italien
        doubleArrayOf(16.60781, 40.64813, 16.61923, 40.65888), // Pini Italien
        doubleArrayOf(5.19979, 43.39183, 5.30159, 43.43223),   // Saint Victoret Frankreich
        doubleArrayOf(14.73936, 40.66814, 14.79026, 40.68923), // Salerno Italien
        doubleArrayOf(14.28909, 40.84016, 14.31454, 40.85068), // San Giovanni A Teduccio Italien
        doubleArrayOf(16.27187, 38.25200, 16.31753, 38.29647), // Siderno Italien
        doubleArrayOf(14.4220, 35.8064, 14.6046, 35.9900),     // Valetta Malta
        doubleArrayOf(-2.64105, 50.93334, -2.61560, 50.94210)  // Yeovil England
    )
    private val defaultNames = listOf(
        "Berlin Mitte", "München Altstadt", "Hamburg City", "Köln Innenstadt",
        "Amvrakia GR", "Athlone IR", "Boisme FR", "Caurel FR", "Chew Valley Lake UK",
        "Fermoy IR", "Killarney IR", "La Chapelle Caro FR", "La Fiora IT", "Marsillargues FR",
        "Monreale Sizilien IT", "Pezzo Superiore IT", "Pico IT", "Pini IT",
        "Saint Victoret FR", "Salerno IT", "San Giovanni A Teduccio IT", "Siderno IT",
        "Valetta Malta", "Yeovil UK"
    )

    private val _image = MutableLiveData<MapillaryClient.ImageData?>()
    val image: LiveData<MapillaryClient.ImageData?> = _image

    private val _currentBbox = MutableLiveData<DoubleArray>() // [minLon, minLat, maxLon, maxLat]
    val currentBbox: LiveData<DoubleArray> = _currentBbox

    private val _currentRegionName = MutableLiveData<String?>()
    val currentRegionName: LiveData<String?> = _currentRegionName

    /** Regionen + optionale Hinweise setzen */
    fun setRegions(
        bboxes: List<DoubleArray>,
        names: List<String>? = null,
        hintsByName: Map<String, List<String>>? = null
    ) {
        regions = if (bboxes.isNotEmpty()) bboxes else defaultRegions
        regionNames = when {
            names != null && names.size == regions.size -> names
            else -> List(regions.size) { "Region ${it + 1}" }
        }
        regionHints = hintsByName ?: emptyMap()
        lastRegionIdx = -1
    }

    /** Bild aus zufälliger (nicht zuletzt genutzter) Region laden */
    fun loadRandomImage() {
        currentLoadJob?.cancel()
        val myReq = ++requestSeq

        currentLoadJob = viewModelScope.launch {
            if (regions.isEmpty()) setRegions(defaultRegions, defaultNames)

            // Wir versuchen bis zu 5 verschiedene Regionen (ohne die letzte)
            val maxRegionTries = minOf(5, regions.size.coerceAtLeast(1))
            var regionTries = 0
            var pickedIdx = -1
            var bboxArray: DoubleArray
            var result: MapillaryClient.ImageData? = null

            while (regionTries < maxRegionTries && result == null) {
                // Region wählen, die NICHT der letzten entspricht
                pickedIdx = pickNextRegionIndex()
                bboxArray = regions[pickedIdx].copyOf()
                _currentRegionName.value = regionNames.getOrNull(pickedIdx)

                // Hints für diese Runde setzen (Custom oder Fallback)
                _currentHints.value = regionHints[_currentRegionName.value]
                    ?: generateFallbackHints(_currentRegionName.value, bboxArray)

                // In dieser Region bis zu 5 Expand-Versuche
                var attempt = 0
                while (result == null && attempt < 5) {
                    val bboxString = "${bboxArray[0]},${bboxArray[1]},${bboxArray[2]},${bboxArray[3]}"
                    result = mapillaryClient.getRandomPano(bboxString)
                        ?: mapillaryClient.getRandomImage(bboxString, onlyPanorama = null)

                    // gleiches Bild wie letzte Runde vermeiden (bis zu 2 Extra-Versuche)
                    var dodgeTries = 0
                    while (result != null && result.id == lastImageId && dodgeTries < 2) {
                        result = mapillaryClient.getRandomImage(bboxString, onlyPanorama = null)
                        dodgeTries++
                    }

                    if (result == null) {
                        attempt++
                        bboxArray = expandBbox(bboxArray, 0.002 * attempt)
                    }
                }

                // wenn nach 5 Expands immer noch nichts → nächste Region probieren
                if (result == null) {
                    regionTries++
                    // damit die nächste pickNextRegionIndex()-Wahl eine andere nimmt:
                    lastRegionIdx = pickedIdx
                } else {
                    // Erfolg: späte Ergebnisse verwerfen?
                    if (myReq != requestSeq) return@launch
                    lastRegionIdx = pickedIdx
                    lastImageId = result?.id
                    _image.value = result
                    _currentBbox.value = bboxArray
                    return@launch
                }
            }

            // Falls wir hier landen: auch nach mehreren Regionen nichts gefunden → leer lassen
            if (myReq != requestSeq) return@launch
            lastRegionIdx = pickedIdx
            _image.value = null
            // Optional: hier könntest du _currentHints leeren oder eine Info setzen
        }
    }





    /** Fallback-Hints aus BBox/Name */
    private fun generateFallbackHints(name: String?, bbox: DoubleArray): List<String> {
        val centerLat = (bbox[1] + bbox[3]) / 2.0
        val centerLon = (bbox[0] + bbox[2]) / 2.0
        fun hemiNS(lat: Double) = if (lat >= 0) "nördliche Halbkugel" else "südliche Halbkugel"
        fun hemiEW(lon: Double) = if (lon >= 0) "östliche Hemisphäre" else "westliche Hemisphäre"
        fun degAbs(v: Double) = String.format("%.1f°", kotlin.math.abs(v))
        return listOf(
            "Liegt auf der ${hemiNS(centerLat)}.",
            "Liegt in der ${hemiEW(centerLon)}.",
            "Breitengrad ca. ${degAbs(centerLat)} ${if (centerLat >= 0) "N" else "S"}.",
            "Längengrad ca. ${degAbs(centerLon)} ${if (centerLon >= 0) "E" else "W"}.",
            name?.let { "Ortsname beginnt mit '${it.split(' ').first()}'." } ?: "Ortsname hat mehrere Wörter."
        )
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

    /** Nächsten Regionsindex wählen (≠ letzter) */
    private fun pickNextRegionIndex(): Int {
        if (regions.size <= 1) return 0
        var idx: Int
        do { idx = Random.nextInt(regions.size) } while (idx == lastRegionIdx)
        return idx
    }

    /** Erst Bild leeren, optional Hints leeren, dann laden */
    fun loadRandomImageClearing() {
        _image.value = null
        // _currentHints.value = emptyList() // optional, wird ohnehin gleich wieder gesetzt
        loadRandomImage()
    }
}
