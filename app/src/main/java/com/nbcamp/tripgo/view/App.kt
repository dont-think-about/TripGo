package com.nbcamp.tripgo.view

import android.app.Application
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.auth.FirebaseUser
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.model.Account
import com.nbcamp.tripgo.BuildConfig
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
        KakaoSdk.init(this, BuildConfig.KAKAO_API_KEY)
        super.onCreate()
    }

    companion object {
        lateinit var prefs: PreferenceUtils
        lateinit var imageLoader: ImageLoader
        var kaKaoUser: Account? = null
        var firebaseUser: FirebaseUser? = null
    }
}
