package com.nbcamp.tripgo.data.repository

import com.nbcamp.tripgo.data.repository.mapper.HomeMapper.toFestivalEntity
import com.nbcamp.tripgo.data.repository.mapper.HomeMapper.toTravelerEntity
import com.nbcamp.tripgo.data.repository.model.FestivalEntity
import com.nbcamp.tripgo.data.repository.model.TravelerEntity
import com.nbcamp.tripgo.data.service.TourApiService
import com.nbcamp.tripgo.util.APIResponse
import com.nbcamp.tripgo.view.home.HomeRepository

class HomeRepositoryImpl(
    private val tourApiService: TourApiService
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
                festivalModel.response.body.items.item.forEach { item ->
                    list.add(item.toFestivalEntity())
                }
                return APIResponse.Success(list)
            }
        }
        return APIResponse.Error(response.message())
    }
}
