package com.nbcamp.tripgo.view.tour.detail

import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity

interface TourDetailRepository {
    suspend fun getDetailInformation(contentId: String?): DetailCommonEntity?
    fun getCurrentUser(): Any?
    suspend fun getMySchedules(email: String): List<CalendarEntity>

}
