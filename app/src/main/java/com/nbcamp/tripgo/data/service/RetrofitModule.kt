package com.nbcamp.tripgo.data.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nbcamp.tripgo.util.URL
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

// Retrofit을 통해 인터넷 연결하게 해주는 모듈 (구현체)
object RetrofitModule {
    private fun buildOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.MINUTES)
            .connectTimeout(5, TimeUnit.MINUTES)
            .build()

    private var gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    fun createTourApiService(): TourApiService {
        return Retrofit.Builder()
            .baseUrl(URL.DEFAULT_TOUR_API_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(buildOkHttpClient())
            .build()
            .create(TourApiService::class.java)
    }

    fun createWeatherApiService(): WeatherService {
        return Retrofit.Builder()
            .baseUrl(URL.WEATHER_API_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(buildOkHttpClient())
            .build()
            .create(WeatherService::class.java)
    }

    fun createTMapApiService(): TMapApiService {
        return Retrofit.Builder()
            .baseUrl(URL.SK_OPEN_API_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(buildOkHttpClient())
            .build()
            .create(TMapApiService::class.java)
    }

    fun createAreaApiService(): TourApiService {
        return Retrofit.Builder()
            .baseUrl(URL.DEFAULT_TOUR_API_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(buildOkHttpClient())
            .build()
            .create(TourApiService::class.java)
    } // 지역 기반
}
