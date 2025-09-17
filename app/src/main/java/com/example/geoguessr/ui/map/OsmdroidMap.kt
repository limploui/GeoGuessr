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

@Composable
fun OsmdroidMap(
    modifier: Modifier = Modifier,
    bbox: DoubleArray?,                    // [minLon, minLat, maxLon, maxLat]
    guessPoint: Pair<Double, Double>?,     // (lat, lon)
    truthPoint: Pair<Double, Double>?,     // (lat, lon)
    onTap: (lat: Double, lon: Double) -> Unit
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var guessMarker by remember { mutableStateOf<Marker?>(null) }
    var truthMarker by remember { mutableStateOf<Marker?>(null) }

    var lastBbox by remember { mutableStateOf<DoubleArray?>(null) }

    // Auto-Fit bis erste User-Geste / einmal pro BBox
    var autoFitEnabled by remember { mutableStateOf(true) }
    var fitDoneForThisBbox by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isTilesScaledToDpi = true

                // Eltern nicht intercepten während Touch-Gesten
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
            val bboxChanged = if (bbox == null || lastBbox == null) {
                bbox != null && lastBbox == null
            } else !lastBbox.contentEquals(bbox)

            if (bboxChanged && bbox != null) {
                lastBbox = bbox.copyOf()
                fitDoneForThisBbox = false
                autoFitEnabled = true
            }

            // 2) Auto-Fit einmalig pro BBox
            if (bbox != null && autoFitEnabled && !fitDoneForThisBbox) {
                val bb = BoundingBox(bbox[3], bbox[2], bbox[1], bbox[0]) // N,E,S,W
                map.zoomToBoundingBox(bb, false)
                fitDoneForThisBbox = true
            }

            // 3) Guess-Marker (blauer Dot)
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

/** Erzeugt einen runden Dot als Drawable (Durchmesser in dp). */
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
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = px * 0.08f
    }
    canvas.drawCircle(r, r, r - stroke.strokeWidth / 2f, stroke)

    return BitmapDrawable(ctx.resources, bmp)
}
