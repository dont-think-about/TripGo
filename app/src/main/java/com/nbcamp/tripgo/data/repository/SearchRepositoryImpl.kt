package com.nbcamp.tripgo.data.repository

import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.travelerssegmentation.SegmentationItem
import com.nbcamp.tripgo.data.repository.mapper.HomeMapper.toKeywordSearchEntity
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity
import com.nbcamp.tripgo.data.service.TourApiService
import com.nbcamp.tripgo.view.search.SearchRepository

class SearchRepositoryImpl(
    private val tourApiService: TourApiService
) : SearchRepository {

    override suspend fun getPlaceBySearch(
        keyword: String,
        contentTypeId: String,
        responseCount: Int
    ): List<KeywordSearchEntity>? {
        val response = tourApiService.getPlaceBySearch(
            keyword = keyword,
            contentTypeId = contentTypeId,
            responseCount = responseCount
        )
        if (response.isSuccessful) {
            val list = arrayListOf<KeywordSearchEntity>()
            response.body()?.let { keywordSearchResponseModel ->
                val resultCode = keywordSearchResponseModel.response.header.resultCode
                val items = keywordSearchResponseModel.response.body.items.item
                if (resultCode != "0000") {
                    return null
                }
                if (items.isEmpty()) {
                    return emptyList()
                }
                items.forEach { item ->
                    list.add(item.toKeywordSearchEntity())
                }
                return list
            }
        }
        return emptyList()
    }

    override suspend fun getCalculationTravelers(
        startDate: String,
        endDate: String,
        responseCount: Int
    ): List<SegmentationItem>? {
        val response = tourApiService.getCalculationTravelerSegmentation(
            startDate = startDate,
            endDate = endDate,
            responseCount = responseCount
        )

        if (response.isSuccessful) {
            response.body()?.let { travelerCountModel ->
                val resultCode = travelerCountModel.response.header.resultCode
                val items = travelerCountModel.response.body.items.segmentationItem
                if (resultCode != "0000") {
                    return null
                }
                if (items.isEmpty()) {
                    return emptyList()
                }
                return items
            }
        }
        return emptyList()
    }

    override suspend fun getFestivalBySearch(
        startDate: String,
        responseCount: Int
    ): List<FestivalItem>? {
        val response = tourApiService.getFestivalInThisMonth(
            startDate = startDate,
            responseCount = responseCount
        )
        if (response.isSuccessful) {
            response.body()?.let { travelerCountModel ->
                val resultCode = travelerCountModel.response.header.resultCode
                val items = travelerCountModel.response.body.items.item
                if (resultCode != "0000") {
                    return null
                }
                if (items.isEmpty()) {
                    return emptyList()
                }
                return items
            }
        }
        return emptyList()
    }
}
