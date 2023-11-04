package com.nbcamp.tripgo.data.service

import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.data.model.weathers.WeatherResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface WeatherService {

    @GET("VilageFcstInfoService_2.0/getUltraSrtNcst")
    suspend fun getTodayWeather(
        @QueryMap querySet: HashMap<String, String> = DEFAULT_QUERY_SET,
        @Query("base_date") date: String,
        @Query("base_time") time: String,
        @Query("nx") x: String,
        @Query("ny") y: String
    ): Response<WeatherResponseModel>
    companion object {
        val DEFAULT_QUERY_SET = hashMapOf(
            "ServiceKey" to BuildConfig.TOUR_API_KEY,
            "dataType" to "JSON",
        )
    }
}
