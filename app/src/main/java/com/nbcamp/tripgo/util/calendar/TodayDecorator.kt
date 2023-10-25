package com.nbcamp.tripgo.util.calendar

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.nbcamp.tripgo.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class TodayDecorator(context: Context) : DayViewDecorator {
    private val drawable = ContextCompat.getDrawable(context, R.drawable.calendar_today_circle)
    private var date = CalendarDay.today()
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day?.equals(date)!!
    }

    override fun decorate(view: DayViewFacade?) {
        view?.setBackgroundDrawable(drawable!!)
        view?.addSpan(DotSpan(7f, Color.parseColor("#FF008000")))
    }
}
