// MainActivity.kt
package com.example.geoguessr

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.example.geoguessr.data.MapillaryViewer
import com.example.geoguessr.ui.theme.GeoGuessrTheme

class MainActivity : AppCompatActivity() {

    private val viewModelTwo: ViewModeltwo by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "🚀 onCreate gestartet – rufe loadRandomImage() auf")

        // gleich beim Start ein Panorama laden
        try {
            viewModelTwo.loadRandomImage()
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Fehler beim Starten von loadRandomImage", e)
        }

        setContent {
            GeoGuessrTheme {
                val item by viewModelTwo.image.observeAsState()

                if (item != null) {
                    Log.d("MainActivity", "✅ Image im UI angekommen: ${item!!.id}")
                    MapillaryViewer(
                        accessToken = "MLY|25128393533414969|53cc9f3a61d67b7e6648f080f4cdff1d",
                        imageId = item!!.id,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Log.d("MainActivity", "⏳ Noch kein Image da – zeige Placeholder")
                    Text("🔄 Suche Panorama...")
                }
            }
        }
    }
}
