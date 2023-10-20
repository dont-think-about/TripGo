package com.nbcamp.tripgo.view.calendar

import com.google.firebase.auth.FirebaseUser
import com.nbcamp.tripgo.data.repository.model.CalendarEntity

interface CalendarRepository {
    fun getCurrentUser(): FirebaseUser?
    suspend fun getMySchedules(email: String): List<CalendarEntity>
}
