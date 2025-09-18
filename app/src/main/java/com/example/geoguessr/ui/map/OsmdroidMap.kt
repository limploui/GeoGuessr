// ui/map/OsmdroidMap.kt
package com.example.geoguessr.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
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

//Final
// Das ist die Karte, die in der Spiel-UI angezeigt wird.
// Hier kann der User seinen Tipp setzen.
@Composable
fun OsmdroidMap(
    modifier: Modifier = Modifier,
    bbox: DoubleArray?,                    // [minLon, minLat, maxLon, maxLat]
    guessPoint: Pair<Double, Double>?,     // (lat, lon) --> der Punkt, den der User getippt hat
    truthPoint: Pair<Double, Double>?,     // (lat, lon) --> der wahre Punkt (wird erst am Ende der Runde angezeigt)
    onTap: (lat: Double, lon: Double) -> Unit
) {
    // MapView & Marker Referenzen, also die "Zeiger" auf die nativen Objekte
    var mapView by remember { mutableStateOf<MapView?>(null) }
    // Marker für Guess & Truth
    var guessMarker by remember { mutableStateOf<Marker?>(null) }
    // (kann null sein, wenn noch kein Tipp gesetzt wurde)
    var truthMarker by remember { mutableStateOf<Marker?>(null) }
    // Letzte BBox merken, um Änderungen zu erkennen
    var lastBbox by remember { mutableStateOf<DoubleArray?>(null) }

    // Auto-Fit bis erste User-Geste / einmal pro BBox
    var autoFitEnabled by remember { mutableStateOf(true) }
    var fitDoneForThisBbox by remember { mutableStateOf(false) }

    // Die Karte selbst
    AndroidView(
        modifier = modifier, // z.B. Modifier.fillMaxSize()
        // Hier wird die MapView gebaut und eingerichtet
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isTilesScaledToDpi = true

                // Eltern nicht intercepten während Touch-Gesten
                // wichtig, wenn die MapView in einem ScrollContainer ist, also z.B. in einer Column,
                // in der der User scrollen kann
                setOnTouchListener { v, ev ->
                    val parent = v.parent
                    when (ev.actionMasked) {
                        android.view.MotionEvent.ACTION_DOWN -> parent?.requestDisallowInterceptTouchEvent(true)
                        android.view.MotionEvent.ACTION_UP,
                        android.view.MotionEvent.ACTION_CANCEL -> parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }

                // Taps -> Guess setzen & Auto-Fit aus
                // tapReceiver ist ein Objekt, das auf Tap-Events hört
                val tapReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        autoFitEnabled = false
                        p?.let { onTap(it.latitude, it.longitude) }
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }
                overlays.add(MapEventsOverlay(tapReceiver))

                // Scroll/Zoom -> Auto-Fit aus
                // Listener für Zoom- und Scroll-Ereignisse
                addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean { autoFitEnabled = false; return false }
                    override fun onZoom(event: ZoomEvent?): Boolean { autoFitEnabled = false; return false }
                })

                onResume()
                mapView = this
            }
        },
        update = { map ->
            // 1) BBox-Wechsel erkennen
            // Also wenn eine neue BBox reinkommt, die sich von der letzten unterscheidet
            val bboxChanged = if (bbox == null || lastBbox == null) {
                bbox != null && lastBbox == null
            } else !lastBbox.contentEquals(bbox)

            if (bboxChanged && bbox != null) {
                lastBbox = bbox.copyOf()
                fitDoneForThisBbox = false
                autoFitEnabled = true
            }

            // 2) Auto-Fit einmalig pro BBox
            //Autofit bedeutet, dass die Karte automatisch auf die BBox zoomt und zentriert.
            // Das ist hier aber nicht immer gewünscht, z.B. wenn der User schon reingezoomt hat.
            if (bbox != null && autoFitEnabled && !fitDoneForThisBbox) {
                val bb = BoundingBox(bbox[3], bbox[2], bbox[1], bbox[0]) // N,E,S,W
                map.zoomToBoundingBox(bb, false)
                fitDoneForThisBbox = true
            }

            // 3) Guess-Marker (blauer Dot)
            // Der Tipp des Users wird als blauer Punkt angezeigt.
            if (guessPoint != null) {
                val (lat, lon) = guessPoint
                val p = GeoPoint(lat, lon)
                if (guessMarker == null) {
                    guessMarker = Marker(map).apply {
                        position = p
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = coloredDot(map.context, Color.rgb(33, 150, 243), 14f) // #2196F3
                        title = "Dein Tipp"
                    }
                    map.overlays.add(guessMarker)
                } else if (guessMarker?.position != p) {
                    guessMarker?.position = p
                }
            } else {
                guessMarker?.let { m -> map.overlays.remove(m); guessMarker = null }
            }

            // 4) Truth-Marker (roter Dot)
            // Der wahre Ort wird als roter Punkt angezeigt (erst am Ende der Runde)
            if (truthPoint != null) {
                val (lat, lon) = truthPoint
                val p = GeoPoint(lat, lon)
                if (truthMarker == null) {
                    truthMarker = Marker(map).apply {
                        position = p
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = coloredDot(map.context, Color.RED, 14f)
                        title = "Wahrer Ort"
                    }
                    map.overlays.add(truthMarker)
                } else if (truthMarker?.position != p) {
                    truthMarker?.position = p
                }
            } else {
                truthMarker?.let { m -> map.overlays.remove(m); truthMarker = null }
            }
            // 5) MapView updaten, wenn sich was geändert hat oder Marker bewegt wurden
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

// Erzeugt einen runden Dot als Drawable (Durchmesser in dp).
// Wird für die Marker-Icons verwendet.
private fun coloredDot(ctx: Context, color: Int, diameterDp: Float): Drawable {
    val px = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        diameterDp,
        ctx.resources.displayMetrics
    ).toInt()

    val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)

    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    val r = px / 2f
    canvas.drawCircle(r, r, r, fill)

    // optionaler weißer Rand für Kontrast
    // Ist aber nicht so schön, wenn der Dot klein ist
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = px * 0.08f
    }
    canvas.drawCircle(r, r, r - stroke.strokeWidth / 2f, stroke)

    return BitmapDrawable(ctx.resources, bmp)
}
