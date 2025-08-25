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

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(message: android.webkit.ConsoleMessage): Boolean {
                        Log.i("MJS", "[console] ${message.message()} @${message.sourceId()}:${message.lineNumber()}")
                        return super.onConsoleMessage(message)
                    }
                }

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
        update = { webView ->
            // Wenn Seite geladen & init bereits passiert ist, aber imageId sich geändert hat → setImage
            if (pageLoaded && inited && lastImageId != imageId) {
                val js = "window.setImage(${json(imageId)});"
                Log.d("MJS", "➡️ setImage($imageId)")
                webView.evaluateJavascript(js, null)
                lastImageId = imageId
            }
        },
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
