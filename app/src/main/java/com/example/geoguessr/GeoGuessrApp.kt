// GeoGuessrApp.kt
package com.example.geoguessr

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import org.osmdroid.config.Configuration
import java.io.File

//Final
// Application-Klasse, wird beim Start der App initialisiert.
// GeoGuessrApp ist für die Initialisierung von osmdroid und anderen globalen Einstellungen zuständig.
// Hauptfunktion hier: initOsmdroid(), die osmdroid konfiguriert.
class GeoGuessrApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initOsmdroid(applicationContext)
    }

    private fun initOsmdroid(ctx: Context) {
        // 1) User-Agent setzen (wichtig, sonst blocken manche Tile-Server)
        Configuration.getInstance().userAgentValue = "com.example.geoguessr"

        // 2) Cache-Orte explizit auf App-internen Cache legen (API 26 sicher)
        val basePath = File(ctx.cacheDir, "osmdroid")
        val tileCache = File(basePath, "tiles")
        if (!tileCache.exists()) tileCache.mkdirs()

        Configuration.getInstance().osmdroidBasePath = basePath
        Configuration.getInstance().osmdroidTileCache = tileCache

        // 3) Einstellungen laden/speichern
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        Configuration.getInstance().load(ctx, prefs)
    }
}
