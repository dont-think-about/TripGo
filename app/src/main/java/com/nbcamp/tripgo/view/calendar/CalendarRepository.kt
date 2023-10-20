package com.nbcamp.tripgo.view.calendar

import com.nbcamp.tripgo.data.repository.model.CalendarEntity

interface CalendarRepository {
    fun getCurrentUser(): Any?
    suspend fun getMySchedules(email: String): List<CalendarEntity>
}
