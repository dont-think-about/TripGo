package com.nbcamp.tripgo.view.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.databinding.FragmentCalendarBinding
import com.nbcamp.tripgo.util.calendar.SaturdayDecorator
import com.nbcamp.tripgo.util.calendar.SelectedDayDecorator
import com.nbcamp.tripgo.util.calendar.SundayDecorator
import com.nbcamp.tripgo.util.calendar.TodayDecorator
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.calendar.uistate.CalendarScheduleUiState
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.Calendar

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding: FragmentCalendarBinding
        get() = _binding!!
    private val calendarViewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory(
            requireActivity()
        )
    }
    private val scheduleListAdapter by lazy {
        ScheduleListAdapter {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
    }

    private fun initViewModel() = with(calendarViewModel) {
        getLoginStatus()
        with(binding) {
            loginStatus.observe(viewLifecycleOwner) { state ->
                when (state) {
                    true -> {
                        getSchedulesFromFireStore()
                    }

                    false -> {
                        calendarNoticeTextView.text = getString(R.string.log_in_and_submit_review)
                    }
                }
            }
            myScheduleState.observe(viewLifecycleOwner) { state ->
                if (state == CalendarScheduleUiState.error(state.message)) {
                    println(state.data)
                    state.message?.let {
                        requireActivity().toast(it)
                    }
                    return@observe
                }
                if (state.data?.isEmpty() == true) {
                    calendarNoticeTextView.isVisible = true
                    calendarNoticeTextView.text = getString(R.string.add_schedule_and_submit_review)
                }
                calendarNoticeTextView.isVisible = false
                calendarProgressBar.isVisible = state.isLoading
                showScheduleInCalendarView(state.data)
                scheduleListAdapter.submitList(state.data)
            }

            schedulesDateState.observe(viewLifecycleOwner) { dateList ->
                val mcv = calendarMainView.state().edit()
                dateList.forEach { date ->
                    calendarMainView.setDateSelected(
                        CalendarDay.from(
                            date.first,
                            date.second,
                            date.third
                        ),
                        true
                    )
                }
                mcv.commit()
            }
            changedMonthState.observe(viewLifecycleOwner) { changedList ->
                scheduleListAdapter.submitList(changedList)
            }
        }
    }

    private fun showScheduleInCalendarView(data: List<CalendarEntity>?) = with(binding) {
        calendarViewModel.setSelectedDate(data)
    }

    private fun getSchedulesFromFireStore() {
        calendarViewModel.getSchedulesFromFireStoreDatabase()
    }

    private fun initViews() = with(binding) {
        calendarMainView.run {
            val month = Calendar.getInstance().get(Calendar.MONTH)
            addDecorators(SaturdayDecorator(month, 1), SundayDecorator(month, 1))
            setOnMonthChangedListener { _, date ->
                removeDecorators()
                invalidateDecorators()
                addDecorators(
                    SaturdayDecorator(date.month, 0),
                    SundayDecorator(date.month, 0),
                    TodayDecorator(requireActivity()),
                    SelectedDayDecorator(requireActivity())
                )
                calendarViewModel.changeScheduleListForThisMonth(date)
            }
        }
        calendarScheduleRecyclerView.run {
            adapter = scheduleListAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        calendarViewModel.getLoginStatus()
    }

    companion object {
        fun newInstance() = CalendarFragment()

        const val TAG = "WEATHER_FRAGMENT"
    }
}
