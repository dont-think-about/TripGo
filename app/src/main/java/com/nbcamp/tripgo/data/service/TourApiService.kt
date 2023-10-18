package com.nbcamp.tripgo.data.service

import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.data.model.festivals.FestivalResponseModel
import com.nbcamp.tripgo.data.model.keywords.KeywordSearchResponseModel
import com.nbcamp.tripgo.data.model.nearby.NearbyPlaceResponseModel
import com.nbcamp.tripgo.data.model.travelers.TravelersCountResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface TourApiService {

    // 월별 방문자 수 조회 (시군구 단위)
    @GET("DataLabService/metcoRegnVisitrDDList")
    suspend fun getCalculationTravelers(
        @QueryMap defaultQuerySet: HashMap<String, String> = DEFAULT_QUERY_SET,
        @Query("numOfRows") responseCount: Int,
        @Query("startYmd") startDate: String,
        @Query("endYmd") endDate: String,
    ): Response<TravelersCountResponseModel>

    // 행사 정보 조회
    @GET("KorService1/searchFestival1")
    suspend fun getFestivalInThisMonth(
        @QueryMap defaultQuerySet: HashMap<String, String> = DEFAULT_QUERY_SET,
        @Query("eventStartDate") startDate: String,
        @Query("numOfRows") responseCount: Int
    ): Response<FestivalResponseModel>

    // 키워드 기반 관광 정보 조회
    @GET("KorService1/searchKeyword1")
    suspend fun getPlaceBySearch(
        @QueryMap defaultQuerySet: HashMap<String, String> = DEFAULT_QUERY_SET,
        @Query("keyword") keyword: String,
        @Query("contentTypeId") contentTypeId: String,
        @Query("numOfRows") responseCount: Int
    ): Response<KeywordSearchResponseModel>

    // 위치 기반 관광 정보 조회
    @GET("KorService1/locationBasedList1")
    suspend fun getNearbyPlace(
        @QueryMap defaultQuerySet: HashMap<String, String> = DEFAULT_QUERY_SET,
        @Query("mapY") latitude: String,
        @Query("mapX") longitude: String,
        @Query("radius") radius: String,
        @Query("contentTypeId") typeId: String = "12"
    ): Response<NearbyPlaceResponseModel>

    companion object {
        val DEFAULT_QUERY_SET = hashMapOf(
            "serviceKey" to BuildConfig.TOUR_API_KEY,
            "MobileOS" to "AND",
            "MobileApp" to "TripGo",
            "_type" to "json"
        )
    }
}
