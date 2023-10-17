package com.nbcamp.tripgo.data.service

import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.data.model.festival.FestivalResponseModel
import com.nbcamp.tripgo.data.model.travelers.TravelersCountResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface TourApiService {

    @GET("DataLabService/metcoRegnVisitrDDList")
    suspend fun getCalculationTravelers(
        @QueryMap defaultQuerySet: HashMap<String, String> = DEFAULT_QUERY_SET,
        @Query("numOfRows") responseCount: Int,
        @Query("startYmd") startDate: String,
        @Query("endYmd") endDate: String,
    ): Response<TravelersCountResponseModel>

    @GET("KorService1/searchFestival1")
    suspend fun getFestivalInThisMonth(
        @QueryMap defaultQuerySet: HashMap<String, String> = DEFAULT_QUERY_SET,
        @Query("eventStartDate") startDate: String,
        @Query("numOfRows") responseCount: Int
    ): Response<FestivalResponseModel>


    companion object {
        val DEFAULT_QUERY_SET = hashMapOf(
            "serviceKey" to BuildConfig.TOUR_API_KEY,
            "MobileOS" to "AND",
            "MobileApp" to "TripGo",
            "_type" to "json"
        )
    }
}
