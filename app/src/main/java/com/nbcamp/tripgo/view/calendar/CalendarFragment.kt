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
import com.nbcamp.tripgo.util.calendar.OutDateMonthDecorator
import com.nbcamp.tripgo.util.calendar.SaturdayDecorator
import com.nbcamp.tripgo.util.calendar.SelectedDayDecorator
import com.nbcamp.tripgo.util.calendar.SundayDecorator
import com.nbcamp.tripgo.util.calendar.TodayDecorator
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
    private val thisMonthScheduleList = arrayListOf<CalendarEntity>()
    private var thisMonth: Int = 0

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
                if (state == CalendarScheduleUiState.error(state.message) ||
                    state.data?.isEmpty() == true
                ) {
                    calendarNoticeTextView.isVisible = true
                    calendarNoticeTextView.text = state.message
                    return@observe
                }
                calendarNoticeTextView.isVisible = false
                calendarProgressBar.isVisible = state.isLoading
                thisMonthScheduleList.run {
                    clear()
                    state.data?.let { addAll(it) }
                }
                showScheduleInCalendarView(state.data)
                scheduleListAdapter.submitList(state.data?.filter { it.startDate?.slice(4..5) == thisMonth.toString() })
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
                thisMonthScheduleList.run {
                    clear()
                    changedList?.let { addAll(it) }
                }
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
            thisMonth = month + 1
            addDecorators(SaturdayDecorator(month, 1), SundayDecorator(month, 1))
            setOnMonthChangedListener { _, date ->
                removeDecorators()
                invalidateDecorators()
                addDecorators(
                    SaturdayDecorator(date.month, 0),
                    SundayDecorator(date.month, 0),
                    TodayDecorator(requireActivity()),
                    SelectedDayDecorator(requireActivity()),
                    OutDateMonthDecorator(requireActivity(), date.month)
                )
                thisMonth = date.month
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
