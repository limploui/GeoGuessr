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

//ViewModeltwo kümmert sich darum, aus welchen Regionen Bilder kommen, welche Hinweise
// es dazu gibt und welches konkrete Bild als Nächstes geladen wird.
//Es achtet darauf, nicht zweimal hintereinander dieselbe Region oder
// dasselbe Bild zu nehmen und vergrößert die BBox, wenn in einer Gegend keine Bilder gefunden werden.
// Außerdem schützt es vor Race Conditions (späte Netzantworten).
class ViewModeltwo(application: Application) : AndroidViewModel(application) {

    // --- Hinweise ---
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

    // Defaults, also wenn setRegions() nicht aufgerufen wird
    // (Koordinaten: minLon, minLat, maxLon, maxLat)
    private val defaultRegions = listOf(
        doubleArrayOf(13.4030, 52.5190, 13.4068, 52.5210), // Berlin Mitte
        doubleArrayOf(11.5675, 48.1355, 11.5795, 48.1430), // München Altstadt
        doubleArrayOf(9.9860, 53.5435, 10.0050, 53.5538),  // Hamburg City
        doubleArrayOf(6.9400, 50.9325, 6.9650, 50.9465),   // Köln Innenstadt
        doubleArrayOf(21.17869, 38.70138, 21.22435, 38.74557), // Amvrakia GR
        doubleArrayOf(-8.02156, 53.39113, -7.81797, 53.45741), // Athlone IR
        doubleArrayOf(-0.45374, 46.76485, -0.40285, 46.78390), // Boisme FR
        doubleArrayOf(-3.08321, 48.20202, -2.98141, 48.23908), // Caurel FR
        doubleArrayOf(-2.62550, 51.31660, -2.60005, 51.32529), // Chew Valley Lake UK
        doubleArrayOf(-8.29614, 52.12840, -8.24524, 52.14547), // Fermoy IR
        doubleArrayOf(-9.54618, 52.04225, -9.44438, 52.07645), // Killarney IR
        doubleArrayOf(-2.46180, 47.84755, -2.36000, 47.88487), // La Chapelle Caro FR
        doubleArrayOf(13.16445, 41.31105, 13.21535, 41.33194), // La Fiora IT
        doubleArrayOf(4.09636, 43.62215, 4.29995, 43.70263),   // Marsillargues FR
        doubleArrayOf(13.27096, 38.05762, 13.31662, 38.10221), // Monreale Sizilien IT
        doubleArrayOf(15.63595, 38.22321, 15.64737, 38.23434), // Pezzo Superiore IT
        doubleArrayOf(13.553996, 41.448589, 13.566720, 41.453799), // Pico IT
        doubleArrayOf(16.60781, 40.64813, 16.61923, 40.65888), // Pini IT
        doubleArrayOf(5.19979, 43.39183, 5.30159, 43.43223),   // Saint Victoret FR
        doubleArrayOf(14.73936, 40.66814, 14.79026, 40.68923), // Salerno IT
        doubleArrayOf(14.28909, 40.84016, 14.31454, 40.85068), // San Giovanni A Teduccio IT
        doubleArrayOf(16.27187, 38.25200, 16.31753, 38.29647), // Siderno IT
        doubleArrayOf(14.4220, 35.8064, 14.6046, 35.9900),     // Valletta Malta
        doubleArrayOf(-2.64105, 50.93334, -2.61560, 50.94210),  // Yeovil UK
        doubleArrayOf(2.57561, 39.51979, 2.77920, 39.60554),   // Palma Malle
        doubleArrayOf(-1.16077, 37.96863, -1.05898, 38.01246), // Murcia Spanien
        doubleArrayOf(-4.45968, 36.69808, -4.35788, 36.74267), // Malaga Spanien
        doubleArrayOf(-8.71705, 41.16664, -8.61526, 41.20849), // Porto Portugal
        doubleArrayOf(5.0393, 60.2786, 5.8537, 60.4985),       // Bergen Norwegen
        doubleArrayOf(23.6213, 61.4332, 24.0285, 61.5394),     // Tampere Finnland
        doubleArrayOf(82.7713, 54.9522, 83.1785, 55.0797),     // Nowosibirsk Russland
        doubleArrayOf(132.2627, 34.3049, 132.6699, 34.4884),   // Hiroshima Japan
        doubleArrayOf(136.2675, 34.7754, 137.8963, 35.5031),   // Nagoya Japan
        doubleArrayOf(140.9244, 42.7279, 142.5531, 43.3781),    // Sapporo Japan
        doubleArrayOf(128.54982, 35.84481, 128.75341, 35.93492), // Daegu Südkorea
        doubleArrayOf(116.30522, 39.84440, 116.50881, 39.92975), // Peking China
        doubleArrayOf(172.58981, -43.55315, 172.69160, -43.51283), // Christchurch NZ
        doubleArrayOf(168.1715, -46.4875, 168.5787, -46.3341),    // Invercargill NZ
        doubleArrayOf(147.29207, -42.90369, 147.39387, -42.86293), // Hobart Tasmania
        doubleArrayOf(144.27383, -38.19624, 144.47742, -38.10876), // Geelong Australien
        doubleArrayOf(106.6601, -6.2832, 107.0673, -6.0620),       // Jakarta Indonesien
        doubleArrayOf(103.98731, 1.11246, 104.08911, 1.16807),     // Baloi
        doubleArrayOf(103.77754, 1.23982, 103.98113, 1.35102),      // Singapur
        doubleArrayOf(79.6739, 6.8150, 80.0811, 7.0358),           // Colombo Sri Lanka
        doubleArrayOf(76.26005, 9.61886, 76.46364, 9.72852),       // Cherthala Indien
        doubleArrayOf(72.79404, 19.00094, 72.99763, 19.10608),     // Mumbai Indien
        doubleArrayOf(18.37412, -33.94920, 18.47591, -33.90305),   // Kapstadt
        doubleArrayOf(17.00632, -22.63367, 17.20991, -22.53096),   // Windhuk Namibia
        doubleArrayOf(32.62487, 0.25009, 32.65032, 0.26399),       // Ggaba Uganda
        doubleArrayOf(8.95343, 38.67119, 9.06330, 38.87478),       // Addis Abeba Äthiopien
        doubleArrayOf(-17.6052, 14.6128, -17.1981, 14.8280),       // Dakar Senegal
        doubleArrayOf(-67.24594, -54.52855, -67.14415, -54.49627), // Tolhuin Argentinien
        doubleArrayOf(-73.11504, -36.85106, -72.91145, -36.76200),  // Concepcion Chile
        doubleArrayOf(-55.99594, -27.43258, -55.79235, -27.33381), // Posadas Argentinien
        doubleArrayOf(-46.58624, -23.73837, -46.48444, -23.68744), // Sao Bernado do Campo Brazil
        doubleArrayOf(-74.07104, 4.603931, -74.05832, 4.610861),   // Bogota Co
        doubleArrayOf(-99.90632, 16.84995, -99.88087, 16.86326),   // Acaoulco MX
        doubleArrayOf(-103.4078, 20.49039, -103.3060, 20.54248),   // Hacienda Satana Fe MX
        doubleArrayOf(-80.04274, 40.43614, -79.99184, 40.45730),   // Pittsburgh US
        doubleArrayOf(-71.54932, 41.64894, -71.34573, 41.73201),   // Warwick US
        doubleArrayOf(-97.05412, 49.76336, -97.00323, 49.78132),   // Grande Pointe Kanada
        doubleArrayOf(-120.5889, 46.97604, -120.4871, 47.01397),   // Ellensburg US
        doubleArrayOf(-116.5625, 43.4143, -116.1554, 43.5757)      // Kuna US"
    )
    // Namen der Regionen (für Hinweise)
    private val defaultNames = listOf(
        "Berlin Mitte", "München Altstadt", "Hamburg City", "Köln Innenstadt",
        "Amvrakia GR", "Athlone IR", "Boisme FR", "Caurel FR", "Chew Valley Lake UK",
        "Fermoy IR", "Killarney IR", "La Chapelle Caro FR", "La Fiora IT", "Marsillargues FR",
        "Monreale Sizilien IT", "Pezzo Superiore IT", "Pico IT", "Pini IT",
        "Saint Victoret FR", "Salerno IT", "San Giovanni A Teduccio IT", "Siderno IT",
        "Valletta Malta", "Yeovil UK",   "Palma Malle", "Murcia Spanien", "Malaga Spanien",
        "Porto Portugal", "Bergen Norwegen", "Tampere Finnland", "Nowosibirsk Russland",
        "Hiroshima Japan", "Nagoya Japan", "Sapporo Japan", "Daegu Südkorea",
        "Peking China", "Christchurch NZ", "Invercargill NZ", "Hobart Tasmania",
        "Geelong Australien", "Jakarta Indonesien", "Baloi", "Singapur",  "Colombo Sri Lanka",
        "Cherthala Indien", "Mumbai Indien", "Kapstadt", "Windhuk Namibia", "Ggaba Uganda",
        "Addis Abeba Äthiopien", "Dakar Senegal", "Tolhuin Argentinien", "Concepcion Chile",
        "Posadas Argentinien", "Sao Bernado do Campo Brazil", "Bogota Co", "Acaoulco MX",
        "Hacienda Satana Fe MX", "Pittsburgh US", "Warwick US", "Grande Pointe Kanada",
        "Ellensburg US", "Kuna US"
    )

    //_image/image, _currentBbox/currentBbox, _currentRegionName/currentRegionName:
    // LiveData, die der UI sagen, was angezeigt werden soll.
    private val _image = MutableLiveData<MapillaryClient.ImageData?>()
    val image: LiveData<MapillaryClient.ImageData?> = _image

    private val _currentBbox = MutableLiveData<DoubleArray>() // [minLon, minLat, maxLon, maxLat]
    val currentBbox: LiveData<DoubleArray> = _currentBbox

    private val _currentRegionName = MutableLiveData<String?>()
    val currentRegionName: LiveData<String?> = _currentRegionName

    /** Regionen + optionale Hinweise setzen
     * Setzt, welche Regionen/Namen/Hints verwendet werden.*/
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
