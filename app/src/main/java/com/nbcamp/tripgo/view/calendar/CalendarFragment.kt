package com.nbcamp.tripgo.view.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.databinding.FragmentCalendarBinding
import com.nbcamp.tripgo.util.calendar.OutDateMonthDecorator
import com.nbcamp.tripgo.util.calendar.SaturdayDecorator
import com.nbcamp.tripgo.util.calendar.SelectedDayDecorator
import com.nbcamp.tripgo.util.calendar.SundayDecorator
import com.nbcamp.tripgo.util.calendar.TodayDecorator
import com.nbcamp.tripgo.view.calendar.uistate.CalendarScheduleUiState
import com.nbcamp.tripgo.view.main.MainViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
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
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val scheduleListAdapter by lazy {
        ScheduleListAdapter {
            runDialogForReviewWriting()
        }
    }

    private val thisMonthScheduleList = arrayListOf<CalendarEntity>()
    private var thisMonth: Int = 0
    private val selectedDayList = arrayListOf<CalendarDay>()

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
        // 먼저 로그인 상태를 가져옴
        getLoginStatus()
        with(binding) {
            loginStatus.observe(viewLifecycleOwner) { state ->
                when (state) {
                    // 로그인이 되어있으면 파이어스토어로 부터 데이터를 가져옴
                    true -> {
                        calendarMainView.selectionMode = MaterialCalendarView.SELECTION_MODE_NONE
                        getSchedulesFromFireStore()
                    }

                    // 돠어 있지 않으면 하단에 안내 메세지 띄움 + 캘린더 선택하면 스낵바 띄울 수 있도록 모드 변경
                    false -> {
                        calendarMainView.selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE
                        calendarNoticeTextView.text = getString(R.string.log_in_and_submit_review)
                    }
                }
            }

            // 파이어 스토어로부터 데이터가 넘어 왔을 때, 관찰 되는 livedata
            myScheduleState.observe(viewLifecycleOwner) { state ->
                if (state == CalendarScheduleUiState.error(state.message) ||
                    state.data?.isEmpty() == true
                ) {
                    calendarNoticeTextView.isVisible = true
                    calendarProgressBar.isVisible = state.isLoading
                    calendarNoticeTextView.text = state.message
                    return@observe
                }
                calendarNoticeTextView.isVisible = false
                calendarProgressBar.isVisible = state.isLoading
                thisMonthScheduleList.run {
                    clear()
                    state.data?.let { addAll(it) }
                }
                // 뷰모델로 부터 관찰한 내 일정을 캘린더에 표시
                showScheduleInCalendarView(state.data)

                // 뷰모델로 부터 관찰한 내 일정을 리사이클러뷰에 표시 (단, 현재 달만)
                scheduleListAdapter.submitList(state.data?.filter { it.startDate?.slice(4..5) == thisMonth.toString() })
            }

            schedulesDateState.observe(viewLifecycleOwner) { dateList ->
                val mcv = calendarMainView.state().edit()
                dateList.forEach { date ->
                    val selectedDay = CalendarDay.from(
                        date.first,
                        date.second,
                        date.third
                    )
                    selectedDayList.add(selectedDay)
                    calendarMainView.setDateSelected(selectedDay, true)
                }
                mcv.commit()
                calendarMainView.addDecorator(
                    SelectedDayDecorator(selectedDayList)
                )
            }

            // 달력을 넘겼을 때 관찰되는 livedata
            changedMonthState.observe(viewLifecycleOwner) { changedList ->
                // 현재 달만 보여주기 위해 기존에 들어 있던 일정 정보를 지우고, 리스트에 추가
                thisMonthScheduleList.run {
                    clear()
                    changedList?.let { addAll(it) }
                }
                // 리사이클러 뷰 어댑터에 보내기
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
            // 상단 바 월 이동 버튼 클릭 리스너
            setOnMonthChangedListener { _, date ->
                removeDecorators()
                invalidateDecorators()
                addDecorators(
                    SaturdayDecorator(date.month, 0),
                    SundayDecorator(date.month, 0),
                    TodayDecorator(requireActivity()),
                    SelectedDayDecorator(selectedDayList),
                    OutDateMonthDecorator(requireActivity(), date.month)
                )
                thisMonth = date.month
                // 하단 리사이클러뷰의 리스트를 현재 달에 바꾸어줌
                calendarViewModel.changeScheduleListForThisMonth(date)
            }
            setOnDateLongClickListener { widget, date ->
                runDialogForReviewWriting()
            }

            // 로그인 안 되었을 떄, 스낵바 띄우는 리스너
            setOnDateChangedListener { _, _, _ ->
                Snackbar.make(binding.root, "로그인 페이지로 이동", 5000)
                    .setAction("LOGIN") {
                        sharedViewModel.runLoginActivity()
                    }.show()
            }
        }
        calendarScheduleRecyclerView.run {
            adapter = scheduleListAdapter
        }
    }

    private fun runDialogForReviewWriting() {
        TODO("Not yet implemented")
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
