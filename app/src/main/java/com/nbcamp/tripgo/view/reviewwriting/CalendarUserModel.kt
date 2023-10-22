package com.nbcamp.tripgo.view.reviewwriting

import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.view.calendar.WritingType

data class CalendarUserModel(
    val model: CalendarEntity?,
    val currentUser: Any?,
    val writingType: WritingType
)
