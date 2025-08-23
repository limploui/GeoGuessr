// MainViewModel.kt
package com.example.geoguessr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.geoguessr.data.MapillaryClient
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val mapillaryClient = MapillaryClient(application)

    // Beispiel: kleine BBox um (lon=13.4049, lat=52.5200), ~300–400 m
    // left,bottom,right,top
    private val bbox = "13.4030,52.5190,13.4068,52.5210"

    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: MutableLiveData<String?> = _imageUrl

    fun getImageURL() {
        viewModelScope.launch {
            val url = mapillaryClient.getRandomImageUrl(bbox)
            android.util.Log.d("GeoGuessr", "Image URL: $url")
            _imageUrl.value = url
        }
    }
}
