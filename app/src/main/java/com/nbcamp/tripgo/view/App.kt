package com.nbcamp.tripgo.view

import android.app.Application
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.nbcamp.tripgo.util.PreferenceUtils

class App : Application() {
    override fun onCreate() {
        prefs = PreferenceUtils(applicationContext)
        imageLoader = ImageLoader.Builder(baseContext)
            .memoryCache {
                MemoryCache.Builder(baseContext)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(baseContext.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
        super.onCreate()
    }

    companion object {
        lateinit var prefs: PreferenceUtils
        lateinit var imageLoader: ImageLoader
    }
}
