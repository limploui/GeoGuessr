// ui/map/OsmdroidMap.kt
package com.example.geoguessr.ui.map

import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay

@Composable
fun OsmdroidMap(
    modifier: Modifier = Modifier,
    bbox: DoubleArray?, // [minLon, minLat, maxLon, maxLat]
    guessPoint: Pair<Double, Double>?, // (lat, lon)
    truthPoint: Pair<Double, Double>?, // (lat, lon) – erst nach Bestätigen sichtbar
    onTap: (lat: Double, lon: Double) -> Unit
) {
    // Marker-Referenzen über remember halten
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var guessMarker by remember { mutableStateOf<Marker?>(null) }
    var truthMarker by remember { mutableStateOf<Marker?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setMultiTouchControls(true)
                isTilesScaledToDpi = true

                // Bounds/Zoom initial setzen, falls vorhanden
                bbox?.let {
                    val bb = BoundingBox(
                        /* north */ it[3], /* east */ it[2],
                        /* south */ it[1], /* west */ it[0]
                    )
                    zoomToBoundingBox(bb, true)
                }

                // Taps abfangen
                val tapReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let { onTap(it.latitude, it.longitude) }
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }
                overlays.add(MapEventsOverlay(tapReceiver))

                mapViewRef = this
            }
        },
        update = { map ->
            // BBox-Update (z.B. erste Anzeige)
            bbox?.let {
                val bb = BoundingBox(it[3], it[2], it[1], it[0])
                // nur beim ersten Mal oder wenn noch nicht fokussiert
                if (map.zoomLevelDouble < 3.0) map.zoomToBoundingBox(bb, true)
            }

            // Guess-Marker zeichnen/aktualisieren (blaue Farbe)
            if (guessPoint != null) {
                val (lat, lon) = guessPoint
                val p = GeoPoint(lat, lon)
                if (guessMarker == null) {
                    guessMarker = Marker(map).apply {
                        position = p
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Dein Tipp"
                        // Blau: setIcon via eingebautem Marker-Style (einfach: default lassen),
                        // alternativ mit eigenem Drawable arbeiten.
                    }
                    map.overlays.add(guessMarker)
                } else {
                    guessMarker?.position = p
                }
            }

            // Truth-Marker zeichnen/aktualisieren (rot)
            if (truthPoint != null) {
                val (lat, lon) = truthPoint
                val p = GeoPoint(lat, lon)
                if (truthMarker == null) {
                    truthMarker = Marker(map).apply {
                        position = p
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Mittelpunkt der BBox"
                        // andere Farbe: simplest ist ein anderes Drawable. Optional:
                        // setIcon(ContextCompat.getDrawable(context, R.drawable.ic_marker_red))
                    }
                    map.overlays.add(truthMarker)
                } else {
                    truthMarker?.position = p
                }
            }

            map.invalidate()
        },
        onRelease = {
            mapViewRef?.onDetach()
            mapViewRef = null
            guessMarker = null
            truthMarker = null
        }
    )
}
