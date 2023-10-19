package com.nbcamp.tripgo.view.calendar

import com.google.firebase.auth.FirebaseUser
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.util.APIResponse

interface CalendarRepository {
    fun getCurrentUser(): FirebaseUser?
    suspend fun getMySchedules(email: String?): APIResponse<List<CalendarEntity>>
}
