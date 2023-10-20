package com.nbcamp.tripgo.view.home

import com.nbcamp.tripgo.data.repository.model.FestivalEntity
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity
import com.nbcamp.tripgo.data.repository.model.NearbyPlaceEntity
import com.nbcamp.tripgo.data.repository.model.TravelerEntity
import com.nbcamp.tripgo.data.repository.model.WeatherEntity

// 뷰모델과 데이터 영역(retrofit 호출)을 잇는 repository 추상화
interface HomeRepository {
    suspend fun getCalculationTravelers(
        startDate: String,
        endDate: String,
        responseCount: Int
    ): List<TravelerEntity>?

    suspend fun getFestivalsInThisMonth(
        startDate: String,
        responseCount: Int
    ): List<FestivalEntity>?

    suspend fun getTodayWeather(
        date: String,
        time: String
    ): WeatherEntity?

    suspend fun getInformationByKeyword(
        keyword: String,
        contentTypeId: String,
        responseCount: Int
    ): List<KeywordSearchEntity>?

    suspend fun getNearbyPlaces(
        latitude: String,
        longitude: String,
        radius: String,
        pageNumber: String
    ): List<NearbyPlaceEntity>?
}
