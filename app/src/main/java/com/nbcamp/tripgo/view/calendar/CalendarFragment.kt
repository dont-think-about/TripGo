package com.nbcamp.tripgo.view.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nbcamp.tripgo.databinding.FragmentCalendarBinding
import com.nbcamp.tripgo.util.calendar.SaturdayDecorator
import com.nbcamp.tripgo.util.calendar.SundayDecorator
import java.util.Calendar

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding: FragmentCalendarBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() = with(binding) {
        calendarMainView.run {
            val month = Calendar.getInstance().get(Calendar.MONTH)
            addDecorators(SaturdayDecorator(month, 1), SundayDecorator(month, 1))
            setOnMonthChangedListener { _, date ->
                removeDecorators()
                invalidateDecorators()
                addDecorators(SaturdayDecorator(date.month, 0), SundayDecorator(date.month, 0))
            }
        }

    }


    companion object {
        fun newInstance() = CalendarFragment()

        const val TAG = "WEATHER_FRAGMENT"
    }
}
