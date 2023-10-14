package com.nbcamp.tripgo.data.service

import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.util.URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Retrofit을 통해 인터넷 연결하게 해주는 모듈 (구현체)
object RetrofitModule {
    private fun buildOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                //로깅 인터셉터
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()

    fun create(): ExamService {
        return Retrofit.Builder()
            .baseUrl(URL.TOUR_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildOkHttpClient())
            .build()
            .create(ExamService::class.java)
    }
}
