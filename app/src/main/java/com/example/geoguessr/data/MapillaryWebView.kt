// MapillaryViewer.kt (Composable)
package com.example.geoguessr.data

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
// Zeigt ein Mapillary Bild oder Pano in einem WebView an
// Das ist das Fenster für Streetview-ähnliche Bilder
fun MapillaryViewer(
    accessToken: String,
    imageId: String,
    modifier: Modifier = Modifier
) {
    // Merker, ob HTML geladen ist und ob JS init schon passiert ist
    var pageLoaded by remember { mutableStateOf(false) }
    var inited by remember { mutableStateOf(false) }
    var lastImageId by remember { mutableStateOf<String?>(null) }

    AndroidView(
        modifier = modifier,
        //Hier wird der WebView gebaut, eingerichtet und es wird eine HTML-Datei geladen.
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // wichtig

                // WebChromeClient: hier wird onConsoleMessage überschrieben,
                // damit man Logs aus dem Browser-Console im Android-Logcat sieht (Log.i("MJS", ...)).

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(message: android.webkit.ConsoleMessage): Boolean {
                        Log.i("MJS", "[console] ${message.message()} @${message.sourceId()}:${message.lineNumber()}")
                        return super.onConsoleMessage(message)
                    }
                }

                //WebViewClient: onPageFinished feuert, wenn die HTML fertig geladen ist.
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        Log.d("MJS", "✅ HTML geladen: $url")
                        pageLoaded = true
                        // Gleich initialisieren (mit aktuellem imageId)
                        evaluateJavascript(
                            "window.init(${json(accessToken)}, ${json(imageId)});"
                        ) { _ ->
                            Log.d("MJS", "➡️ init(accessToken, imageId) gerufen")
                            inited = true
                            lastImageId = imageId
                        }
                    }
                }

                // HTML aus assets laden
                loadUrl("file:///android_asset/mly_v2.html")
            }
        },
        //Dieser Block läuft jedes Mal, wenn Compose neu rendert.
        //Hier wird geprüft:
        //„Seite ist geladen und init schon gemacht und die imageId ist NEU?“
        //Wenn ja → window.setImage(imageId) per JS.
        //Danach wird lastImageId = imageId gespeichert, damit du es nicht nochmal machst.
        update = { webView ->
            // Wenn Seite geladen & init bereits passiert ist, aber imageId sich geändert hat → setImage
            if (pageLoaded && inited && lastImageId != imageId) {
                val js = "window.setImage(${json(imageId)});"
                Log.d("MJS", "➡️ setImage($imageId)")
                webView.evaluateJavascript(js, null)
                lastImageId = imageId
            }
        },
        //Wird aufgerufen, wenn Compose diesen Composable „wegwirft“
        onRelease = {
            // Cleanup
            inited = false
            pageLoaded = false
            lastImageId = null
        }
    )
}

// Kleiner Helfer: String sicher für JS quoten
private fun json(s: String?): String = if (s == null) "null" else "\"${s.replace("\"", "\\\"")}\""
