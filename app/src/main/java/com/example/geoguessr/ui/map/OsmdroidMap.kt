// ui/map/OsmdroidMap.kt
package com.example.geoguessr.ui.map

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmdroidMap(
    modifier: Modifier = Modifier,
    bbox: DoubleArray?,                      // [minLon, minLat, maxLon, maxLat]
    guessPoint: Pair<Double, Double>?,       // (lat, lon)
    truthPoint: Pair<Double, Double>?,       // (lat, lon)
    onTap: (lat: Double, lon: Double) -> Unit
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var guessMarker by remember { mutableStateOf<Marker?>(null) }
    var truthMarker by remember { mutableStateOf<Marker?>(null) }

    var lastBbox by remember { mutableStateOf<DoubleArray?>(null) }

    // Auto-Fit nur bis zur ersten User-Geste
    var autoFitEnabled by remember { mutableStateOf(true) }
    // Ein Fit pro BBox-Wechsel
    var fitDoneForThisBbox by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                // Grund-Setup
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isTilesScaledToDpi = true
                // (Optional) Zoom-Grenzen, falls gewünscht:
                // setMinZoomLevel(2.0)
                // setMaxZoomLevel(19.0)

                // Touch-Weitergabe: während der Geste Eltern nicht intercepten, danach wieder erlauben
                setOnTouchListener { v, ev ->
                    val parent = v.parent
                    when (ev.actionMasked) {
                        android.view.MotionEvent.ACTION_DOWN -> parent?.requestDisallowInterceptTouchEvent(true)
                        android.view.MotionEvent.ACTION_UP,
                        android.view.MotionEvent.ACTION_CANCEL -> parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false // Map verarbeitet die Geste normal
                }

                // Taps → Guess setzen UND Auto-Fit deaktivieren
                val tapReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        autoFitEnabled = false
                        p?.let { onTap(it.latitude, it.longitude) }
                        return true // Event verbraucht
                    }
                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }
                overlays.add(MapEventsOverlay(tapReceiver))

                // Scroll/Zoom → Auto-Fit aus
                addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        autoFitEnabled = false
                        return false
                    }
                    override fun onZoom(event: ZoomEvent?): Boolean {
                        autoFitEnabled = false
                        return false
                    }
                })

                // Lifecycle-Helfer
                onResume()
                mapView = this
            }
        },
        update = { map ->
            // 1) BBox-Wechsel erkennen
            val bboxChanged = if (bbox == null || lastBbox == null) {
                bbox != null && lastBbox == null
            } else {
                !lastBbox.contentEquals(bbox)
            }
            if (bboxChanged && bbox != null) {
                lastBbox = bbox.copyOf()
                fitDoneForThisBbox = false    // neuer Bereich → einmalig fitten
                autoFitEnabled = true         // Fit darf laufen, bis User interagiert
            }

            // 2) Auto-Fit: genau einmal pro BBox und nur solange keine User-Geste
            if (bbox != null && autoFitEnabled && !fitDoneForThisBbox) {
                val bb = BoundingBox(
                    /*north*/ bbox[3], /*east*/ bbox[2],
                    /*south*/ bbox[1], /*west*/ bbox[0]
                )
                // animate=false: keine „Zuckler“, zuverlässiger in Compose
                map.zoomToBoundingBox(bb, false)
                fitDoneForThisBbox = true
            }

            // 3) Guess-Marker pflegen/entfernen
            if (guessPoint != null) {
                val (lat, lon) = guessPoint
                val p = GeoPoint(lat, lon)
                if (guessMarker == null) {
                    guessMarker = Marker(map).apply {
                        position = p
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Dein Tipp"
                    }
                    map.overlays.add(guessMarker)
                } else if (guessMarker?.position != p) {
                    guessMarker?.position = p
                }
            } else {
                // guess ist null -> Marker entfernen, wenn vorhanden
                guessMarker?.let { m ->
                    map.overlays.remove(m)
                    guessMarker = null
                }
            }

            // 4) Truth-Marker pflegen/entfernen
            if (truthPoint != null) {
                val (lat, lon) = truthPoint
                val p = GeoPoint(lat, lon)
                if (truthMarker == null) {
                    truthMarker = Marker(map).apply {
                        position = p
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Mittelpunkt der BBox"
                    }
                    map.overlays.add(truthMarker)
                } else if (truthMarker?.position != p) {
                    truthMarker?.position = p
                }
            } else {
                truthMarker?.let { m ->
                    map.overlays.remove(m)
                    truthMarker = null
                }
            }

            map.invalidate()
        },
        onRelease = {
            mapView?.onPause()
            mapView?.onDetach()
            mapView = null
            guessMarker = null
            truthMarker = null
            lastBbox = null
            autoFitEnabled = true
            fitDoneForThisBbox = false
        }
    )
}
