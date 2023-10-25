package com.nbcamp.tripgo.data.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.util.URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


// Retrofit을 통해 인터넷 연결하게 해주는 모듈 (구현체)
object RetrofitModule {
    private fun buildOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                // 로깅 인터셉터
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .readTimeout(5, TimeUnit.MINUTES)
            .connectTimeout(5, TimeUnit.MINUTES)
            .build()


    private var gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    fun create(): ExamService {
        return Retrofit.Builder()
            .baseUrl(URL.TOUR_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildOkHttpClient())
            .build()
            .create(ExamService::class.java)
    }

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
}
