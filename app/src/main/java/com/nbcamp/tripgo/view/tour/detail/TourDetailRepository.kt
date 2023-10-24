package com.nbcamp.tripgo.view.tour.detail

import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity

interface TourDetailRepository {

    suspend fun getDetailInformation(contentId: String?): DetailCommonEntity?

}
