package com.nbcamp.tripgo.data.service

import com.nbcamp.tripgo.BuildConfig
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TMapApiService {

    @GET("routeStaticMap")
    suspend fun getRouteImage(
        @Query("appKey") key: String = BuildConfig.SK_OPEN_API_KEY,
        @Query("startX") startLongitude: Double,
        @Query("startY") startLatitude: Double,
        @Query("endX") endLongitude: Double,
        @Query("endY") endLatitude: Double,
        @Query("lineColor") lineColor: String = "red"
    ): Response<ResponseBody>

}
