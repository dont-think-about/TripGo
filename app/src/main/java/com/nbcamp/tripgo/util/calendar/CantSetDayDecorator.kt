package com.nbcamp.tripgo.util.calendar

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import com.nbcamp.tripgo.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class CantSetDayDecorator(
    context: Context,
    private val dates: Collection<CalendarDay>
) : DayViewDecorator {
    private val drawable = AppCompatResources.getDrawable(context, R.drawable.icon_close)
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(drawable!!)
        view.setDaysDisabled(true)
    }
}
