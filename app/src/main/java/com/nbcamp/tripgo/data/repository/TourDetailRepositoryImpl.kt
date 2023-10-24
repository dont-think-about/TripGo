package com.nbcamp.tripgo.data.repository

import com.nbcamp.tripgo.data.repository.mapper.DetailMapper.toDetailCommonEntity
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity
import com.nbcamp.tripgo.data.service.TourApiService
import com.nbcamp.tripgo.view.tour.detail.TourDetailRepository

class TourDetailRepositoryImpl(
    private val tourApiService: TourApiService
) : TourDetailRepository {

    override suspend fun getDetailInformation(contentId: String?): DetailCommonEntity? {
        if (contentId == null)
            return null
        val response = tourApiService.getDetailInformation(
            contentId = contentId
        )

        if (response.isSuccessful) {
            response.body()?.let { detailModel ->
                val items = detailModel.response.body.items.item.first()
                return items.toDetailCommonEntity()
            }
        }
        return null
    }
}
