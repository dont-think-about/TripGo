package com.nbcamp.tripgo.util.calendar

import android.content.Context
import android.text.style.ForegroundColorSpan
import com.nbcamp.tripgo.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class OutDateMonthDecorator(
    private val context: Context,
    private val month: Int,
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.month != month
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(ForegroundColorSpan(context.getColor(R.color.deactivated)))
    }
}
