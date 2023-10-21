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
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.util.setFancyDialog
import com.nbcamp.tripgo.view.calendar.uistate.CalendarScheduleUiState
import com.nbcamp.tripgo.view.calendar.uistate.RunDialogUiState.Companion.NOT_OPEN
import com.nbcamp.tripgo.view.main.MainViewModel
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingFragment
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
        ScheduleListAdapter { model ->
            runDialogForReviewWriting(model)
            calendarViewModel.setRemoveData()
        }
    }

    private var isLoggedIn = false
    private var currentUser: Any? = null
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
                isLoggedIn = state.isLoggedIn
                currentUser = state.user
                when (isLoggedIn) {
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
                    state.allSchedules?.isEmpty() == true
                ) {
                    calendarNoticeTextView.isVisible = true
                    calendarProgressBar.isVisible = state.isLoading
                    calendarNoticeTextView.text = state.message
                    return@observe
                }
                calendarNoticeTextView.isVisible = false
                calendarProgressBar.isVisible = state.isLoading
                // 뷰모델로 부터 관찰한 내 일정을 캘린더에 표시
                showScheduleInCalendarView(state.allSchedules)

                // 뷰모델로 부터 관찰한 내 일정을 리사이클러뷰에 표시 (단, 현재 달만)  TODO 정렬을 뷰모델에서 하기
                scheduleListAdapter.submitList(state.monthSchedules)
            }

            // start ~ end date 사이의 기간을 달력에 표시
            schedulesDateState.observe(viewLifecycleOwner) { dateList ->
                val mcv = calendarMainView.state().edit()
                dateList.forEach { date ->
                    val selectedDay = CalendarDay.from(
                        date.first,
                        date.second,
                        date.third
                    )
                    // 일정이 있는 날엔 달력에 따로 표시해 주기 위한 리스트
                    selectedDayList.add(selectedDay)
                }
                mcv.commit()
                calendarMainView.addDecorator(
                    SelectedDayDecorator(selectedDayList)
                )
            }

            // 달력을 넘겼을 때 관찰 되는 livedata
            changedMonthState.observe(viewLifecycleOwner) { changedList ->
                // 리사이클러 뷰 어댑터에 보내기
                scheduleListAdapter.submitList(changedList)
            }

            runDialogState.observe(viewLifecycleOwner) { state ->
                // 모델을 넘겨 줘야 리뷰 작성 할 때 정보를 같이 넘겨 줄 수 있음
                if (state.isValidRange) {
                    runDialogForReviewWriting(state?.data)
                    /*
                       runDialogState를 observing하기 때문에
                       리뷰작성에서 취소를 누르거나, 다른 화면으로 이동하면 다이얼로그가 다시 뜨는데
                       이를 방지하기 위해 한 번 다이얼로그를 띄웠으면 데이터를 없애준다. (null 처리)
                     */
                    calendarViewModel.setRemoveData()
                    return@observe
                }
                // 데이터를 없앴을 땐, 아무 동작을 하지 않도록 한다.
                if (state.message == NOT_OPEN) Unit
                else {
                    requireActivity().toast("일정이 없거나 이후의 일정은 리뷰를 적을 수 없습니다.")
                }
            }
        }
    }

    private fun showScheduleInCalendarView(data: List<CalendarEntity>?) {
        calendarViewModel.setSelectedDate(data)
    }

    private fun getSchedulesFromFireStore() {
        calendarViewModel.getSchedulesFromFireStoreDatabase()
    }

    private fun initViews() = with(binding) {
        calendarMainView.run {
            val month = Calendar.getInstance().get(Calendar.MONTH)
            removeDecorators()
            invalidateDecorators()
            addDecorators(
                SaturdayDecorator(month, 1),
                SundayDecorator(month, 1),
                OutDateMonthDecorator(requireActivity(), month + 1),
                TodayDecorator(requireActivity())
            )
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
                // 하단 리사이클러뷰의 리스트를 현재 달에 바꾸어줌
                calendarViewModel.changeScheduleListForThisMonth(date)
            }
            setOnDateLongClickListener { _, date ->
                if (isLoggedIn) {
                    calendarViewModel.runDialogForReviewWriting(date, selectedDayList)
                }
            }

            // 로그인 안 되었을 떄, 스낵바 띄우는 리스너
            setOnDateChangedListener { _, _, _ ->
                Snackbar.make(binding.root, "로그인 페이지로 이동", 5000).setAction("LOGIN") {
                    sharedViewModel.runLoginActivity()
                }.show()
            }
        }
        calendarScheduleRecyclerView.run {
            adapter = scheduleListAdapter
        }
    }

    private fun runDialogForReviewWriting(model: CalendarEntity?) {
        if (model?.isReviewed == false) {
            setFancyDialog(requireActivity()) {
                goToReviewFragment(model, currentUser)
            }.show()
            return
        }
        requireActivity().toast(getString(R.string.already_write_review))
    }

    private fun goToReviewFragment(model: CalendarEntity, currentUser: Any?) {
        val transactionReviewWriting = parentFragmentManager.beginTransaction()
        sharedViewModel.setBasicReviewModel(model, currentUser)
        transactionReviewWriting.replace(
            R.id.main_fragment_container,
            ReviewWritingFragment.newInstance()
        ).addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        calendarViewModel.getLoginStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CalendarFragment()

        const val TAG = "WEATHER_FRAGMENT"
    }
}
