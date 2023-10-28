package com.nbcamp.tripgo.util.extension

import com.prolificinteractive.materialcalendarview.CalendarDay

object StringExtension {
    fun String.toCalendarDay(): CalendarDay {
        val (year, date) = this.chunked(4).map { it }
        val (month, day) = date.chunked(2).map { it.toInt() }

        return CalendarDay.from(year.toInt(), month, day)
    }
}
