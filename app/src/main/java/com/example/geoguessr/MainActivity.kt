// MainActivity.kt
package com.example.geoguessr

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import com.example.geoguessr.ui.theme.GeoGuessrTheme
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getImageURL()


        setContent {
            GeoGuessrTheme {
                val url by viewModel.imageUrl.observeAsState()
                if (url != null) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        onError = { error -> android.util.Log.e("GeoGuessr", "Coil Error: $error") },
                        modifier = Modifier.size(300.dp),
                    )
                } else {
                    androidx.compose.material3.Text("Kein Bild gefunden")
                }
            }
        }
    }
}
