package com.nbcamp.tripgo.data.repository

import com.nbcamp.tripgo.data.repository.mapper.HomeMapper.toFestivalEntity
import com.nbcamp.tripgo.data.repository.mapper.HomeMapper.toKeywordSearchEntity
import com.nbcamp.tripgo.data.repository.mapper.HomeMapper.toNearbyPlaceEntity
import com.nbcamp.tripgo.data.repository.mapper.HomeMapper.toTravelerEntity
import com.nbcamp.tripgo.data.repository.mapper.HomeMapper.toWeatherEntity
import com.nbcamp.tripgo.data.repository.model.FestivalEntity
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity
import com.nbcamp.tripgo.data.repository.model.NearbyPlaceEntity
import com.nbcamp.tripgo.data.repository.model.TravelerEntity
import com.nbcamp.tripgo.data.repository.model.WeatherEntity
import com.nbcamp.tripgo.data.service.TourApiService
import com.nbcamp.tripgo.data.service.WeatherService
import com.nbcamp.tripgo.util.APIResponse
import com.nbcamp.tripgo.view.home.HomeRepository

class HomeRepositoryImpl(
    private val tourApiService: TourApiService,
    private val weatherApiService: WeatherService
) : HomeRepository {
    override suspend fun getCalculationTravelers(
        startDate: String,
        endDate: String,
        responseCount: Int
    ): APIResponse<List<TravelerEntity>> {
        val response = tourApiService.getCalculationTravelers(
            startDate = startDate,
            endDate = endDate,
            responseCount = responseCount
        )

        if (response.isSuccessful) {
            val list = arrayListOf<TravelerEntity>()
            response.body()?.let { travelerCountModel ->
                val resultCode = travelerCountModel.response.header.resultCode
                if (resultCode != "0000") {
                    return APIResponse.Error(response.message())
                }
                travelerCountModel.response.body.items.item.forEach { item ->
                    list.add(item.toTravelerEntity())
                }
                return APIResponse.Success(list)
            }
        }
        return APIResponse.Error(response.message())
    }

    override suspend fun getFestivalsInThisMonth(
        startDate: String,
        responseCount: Int
    ): APIResponse<List<FestivalEntity>> {
        val response = tourApiService.getFestivalInThisMonth(
            startDate = startDate,
            responseCount = responseCount
        )

        if (response.isSuccessful) {
            val list = arrayListOf<FestivalEntity>()
            response.body()?.let { festivalModel ->
                val resultCode = festivalModel.response.header.resultCode
                if (resultCode != "0000") {
                    return APIResponse.Error(response.message())
                }
                festivalModel.response.body.items.item.forEach { item ->
                    list.add(item.toFestivalEntity())
                }
                return APIResponse.Success(list)
            }
        }
        return APIResponse.Error(response.message())
    }

    override suspend fun getTodayWeather(
        date: String,
        time: String
    ): APIResponse<WeatherEntity> {
        val response = weatherApiService.getTodayWeather(
            date = date,
            time = time
        )

        if (response.isSuccessful) {
            response.body()?.let { weatherModel ->
                val resultCode = weatherModel.response.header.resultCode
                if (resultCode != "00") {
                    return APIResponse.Error(response.message())
                }
                val weatherInfo =
                    weatherModel.response.body.items.weatherItem
                return APIResponse.Success(weatherInfo.toWeatherEntity())
            }
        }
        return APIResponse.Error(response.message())
    }

    override suspend fun getInformationByKeyword(
        keyword: String,
        contentTypeId: String,
        responseCount: Int
    ): APIResponse<List<KeywordSearchEntity>> {
        val response = tourApiService.getPlaceBySearch(
            keyword = keyword,
            contentTypeId = contentTypeId,
            responseCount = responseCount
        )
        if (response.isSuccessful) {
            val list = arrayListOf<KeywordSearchEntity>()
            response.body()?.let { keywordModel ->
                val resultCode = keywordModel.response.header.resultCode
                if (resultCode != "0000") {
                    return APIResponse.Error(response.message())
                }
                keywordModel.response.body.items.item.forEach { item ->
                    list.add(item.toKeywordSearchEntity())
                }
                return APIResponse.Success(list)
            }
        }
        return APIResponse.Error(response.message())
    }

    override suspend fun getNearbyPlaces(
        latitude: String,
        longitude: String,
        radius: String,
        pageNumber: String
    ): APIResponse<List<NearbyPlaceEntity>> {
        val response = tourApiService.getNearbyPlace(
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            pageNumber = pageNumber
        )
        if (response.isSuccessful) {
            val list = arrayListOf<NearbyPlaceEntity>()
            response.body()?.let { nearbyModel ->
                val resultCode = nearbyModel.response.header.resultCode
                if (resultCode != "0000") {
                    return APIResponse.Error(response.message())
                }
                nearbyModel.response.body.items.item.forEach { item ->
                    list.add(item.toNearbyPlaceEntity())
                }
                return APIResponse.Success(list)
            }
        }
        return APIResponse.Error(response.message())
    }
}
