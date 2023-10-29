package com.nbcamp.tripgo.view.calendar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.databinding.DialogCalendarBinding
import com.nbcamp.tripgo.databinding.FragmentCalendarBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.SwipeToEditCallback
import com.nbcamp.tripgo.util.calendar.CalendarFragmentTodayDecorator
import com.nbcamp.tripgo.util.calendar.OutDateMonthDecorator
import com.nbcamp.tripgo.util.calendar.SaturdayDecorator
import com.nbcamp.tripgo.util.calendar.SelectedDayDecorator
import com.nbcamp.tripgo.util.calendar.SundayDecorator
import com.nbcamp.tripgo.util.calendar.TodayDecorator
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.util.extension.StringExtension.toCalendarDay
import com.nbcamp.tripgo.util.setFancyDialog
import com.nbcamp.tripgo.view.calendar.modify.TourModifyDetailActivity
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
    private var calendarBinding: DialogCalendarBinding? = null
    private var forModifySchedule: List<CalendarEntity>? = null
    private val calendarViewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory(
            requireActivity()
        )
    }
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val scheduleListAdapter by lazy {
        ScheduleListAdapter(
            // 짧게 클릭 하면 리뷰 작성 or 수정
            onClickItem = { model ->
                runDialogForReviewWriting(model)
            },
            // 길게 클릭 하면 리뷰 삭제
            onLongClickItem = { model ->
                runDialogForScheduleDelete(model)
            }
        )
    }

    /**
     *  스와이프를 했을 때 내가 일정 저장한 상세 페이지로 들어 갈 수 있도록 하고
     *  그곳에서 일정을 수정 할 수 있도록 함
     */
    private val modifyScheduleSwipeHandler by lazy {
        object : SwipeToEditCallback(requireActivity()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val model = scheduleListAdapter.currentList[viewHolder.adapterPosition]
                val startDate = model.startDate?.toCalendarDay()
                val endDate = model.endDate?.toCalendarDay()
                binding.calendarScheduleRecyclerView.adapter = scheduleListAdapter
                val intent = Intent(requireActivity(), TourModifyDetailActivity::class.java).apply {
                    putExtra("contentId", model.contentId)
                    // 일정 수정을 위해 startDate, endDate가 필요
                    putExtra("startDate", startDate)
                    putExtra("endDate", endDate)
                    putExtra("model", model)

                    // 수정 시 일정이 있는 날엔 달력에 따로 표시해 주기 위한 리스트
                    putExtra("selectedDayList", selectedDayList)
                    // 수정 시 해당 일정을 뺴고 보여주기 위한 임시 캘린더 배열
                    putExtra("forModifySchedule",
                        forModifySchedule?.let { ArrayList(it) }
                    )
                }
                startActivity(intent)
            }
        }
    }

    private var isLoggedIn = false
    private var currentUser: Any? = null
    private val selectedDayList = arrayListOf<CalendarDay>()
    private var month = Calendar.getInstance().get(Calendar.MONTH)
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(layoutInflater)
        loadingDialog = LoadingDialog(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
    }

    private fun initViewModel() = with(calendarViewModel) {
        loadingDialog?.run {
            setVisible()
            setText(getString(R.string.getting_my_schedule))
        }
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
                        calendarTitleTextView.isGone = true
                        calendarNoticeTextView.text = getString(R.string.log_in_and_submit_review)
                    }
                }
            }

            // 파이어 스토어로부터 데이터가 넘어 왔을 때, 관찰 되는 livedata
            myScheduleState.observe(viewLifecycleOwner) { state ->
                if (state == null) return@observe
                updateCalendarUi(state)
            }

            // start ~ end date 사이의 기간을 달력에 표시
            schedulesDateState.observe(viewLifecycleOwner) { dateList ->
                // 일정이 있는 날엔 달력에 따로 표시해 주기 위한 리스트
                selectedDayList.clear()
                selectedDayList.addAll(dateList)
                calendarMainView.run {
                    removeDecorators()
                    invalidateDecorators()
                    addDecorators(
                        SelectedDayDecorator(selectedDayList),
                        SaturdayDecorator(month, 1),
                        SundayDecorator(month, 1),
                        OutDateMonthDecorator(requireActivity(), month + 1),
                        CalendarFragmentTodayDecorator(requireActivity())
                    )
                }
                loadingDialog?.setInvisible()
            }

            // 달력을 넘겼을 때 관찰 되는 livedata
            changedMonthState.observe(viewLifecycleOwner) { changedList ->
                // 리사이클러 뷰 어댑터에 보내기
                scheduleListAdapter.submitList(changedList)
            }

            runDialogState.observe(viewLifecycleOwner) { state ->
                // 모델을 넘겨 줘야 리뷰 작성 할 때 정보를 같이 넘겨 줄 수 있음
                if (state?.isValidRange == true) {
                    runDialogForReviewWriting(state.data)
                    return@observe
                }
                // 데이터를 없앴을 땐, 아무 동작을 하지 않도록 한다.
                if (state?.message == NOT_OPEN) Unit
                else {
                    requireActivity().toast(getString(R.string.not_writing_review))
                }
            }

            deleteScheduleUiState.observe(viewLifecycleOwner) { state ->
                if (state == null) return@observe
                calendarMainView.setCurrentDate(CalendarDay.today(), true)
                updateCalendarUi(state)
            }

            calendarClickModifyEvent.observe(viewLifecycleOwner) {
                when (it) {
                    true -> requireActivity().toast(getString(R.string.cant_select_duplicate_schedule))
                    false -> requireActivity().toast(getString(R.string.not_register_schedule_before_today))
                }
                calendarBinding?.addScheduleCalendarView?.clearSelection()
            }

            // sharedViewModel을 통해 CalendarFragment - ScheduleModifyFragment 사이 데이터 전달
            sharedViewModel.buttonClickModifyState.observe(viewLifecycleOwner) { state ->
                updateCalendarUi(state)
            }
        }
    }

    private fun updateCalendarUi(state: CalendarScheduleUiState) = with(binding) {
        if (state == CalendarScheduleUiState.error(state.message)
            || state.allSchedules?.isEmpty() == true
        ) {
            calendarNoticeTextView.isVisible = true
            calendarProgressBar.isVisible = state.isLoading
            calendarNoticeTextView.text = state.message
            return
        }
        calendarNoticeTextView.isVisible = false
        calendarProgressBar.isVisible = state.isLoading == true
        // 뷰모델로 부터 관찰한 내 일정을 캘린더에 표시
        showScheduleInCalendarView(state.allSchedules?.toMutableList())

        // 수정 시 해당 일정을 뺴고 보여주기 위한 임시 캘린더 배열
        forModifySchedule = state.allSchedules
        // 뷰모델로 부터 관찰한 내 일정을 리사이클러뷰에 표시 (단, 현재 달만)
        scheduleListAdapter.submitList(state.monthSchedules?.toMutableList())
    }

    private fun showScheduleInCalendarView(data: List<CalendarEntity>?) {
        calendarViewModel.setSelectedDate(data)
    }

    private fun getSchedulesFromFireStore() {
        calendarViewModel.getSchedulesFromFireStoreDatabase()
    }

    private fun initViews() = with(binding) {
        val callSwipeHelper = ItemTouchHelper(modifyScheduleSwipeHandler)
        nestedScrollView.isNestedScrollingEnabled = false
        calendarMainView.run {
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
                Snackbar.make(binding.root, getString(R.string.move_to_log_in), 5000)
                    .setAction("LOGIN") {
                        sharedViewModel.runLoginActivity()
                    }.show()
            }
        }
        calendarScheduleRecyclerView.run {
            adapter = scheduleListAdapter
            addItemDecoration(
                DividerItemDecoration(
                    requireActivity(),
                    DividerItemDecoration.VERTICAL
                )
            )
            callSwipeHelper.attachToRecyclerView(this)
        }
    }

    private fun runDialogForReviewWriting(model: CalendarEntity?) {
        if (model?.isReviewed == false) {
            setFancyDialog(
                context = requireActivity(),
                title = getString(R.string.review_writing),
                message = getString(R.string.want_review),
                positiveText = getString(R.string.yes),
                negativeText = getString(R.string.no),
                icon = R.drawable.icon_alert_review
            ) {
                goToReviewFragment(model, currentUser, WritingType.NEW)
            }.show()
            return
        }
        setFancyDialog(
            context = requireActivity(),
            title = getString(R.string.modfy_review),
            message = getString(R.string.want_modify_review),
            positiveText = getString(R.string.yes),
            negativeText = getString(R.string.no),
            icon = R.drawable.icon_alert_review
        ) {
            goToReviewFragment(model!!, currentUser, WritingType.MODIFY)
        }.show()
    }


    // 일정 삭제 다이얼로그
    private fun runDialogForScheduleDelete(model: CalendarEntity) {
        if (model.isReviewed == true) {
            requireActivity().toast(getString(R.string.cannot_delete_review_writted))
            return
        }
        setFancyDialog(
            context = requireActivity(),
            title = getString(R.string.delete_schedule),
            message = getString(R.string.confirm_delete_schedule),
            positiveText = getString(R.string.yes),
            negativeText = getString(R.string.no),
            icon = R.drawable.icon_alert_review
        ) {
            calendarViewModel.deleteMySchedule(model)
        }.show()
    }

    private fun goToReviewFragment(
        model: CalendarEntity,
        currentUser: Any?,
        writingType: WritingType
    ) {
        val transactionReviewWriting = parentFragmentManager.beginTransaction()
        // review writing fragment로 데이터 전달
        sharedViewModel.setBasicReviewModel(model, currentUser, writingType)
        transactionReviewWriting.replace(
            R.id.main_fragment_container,
            ReviewWritingFragment.newInstance()
        ).addToBackStack(null)
            .commit()
    }


    override fun onResume() {
        super.onResume()
        calendarViewModel.getLoginStatus()
        binding.calendarMainView.setCurrentDate(CalendarDay.today(), true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        loadingDialog = null
        calendarBinding = null
    }

    companion object {
        fun newInstance() = CalendarFragment()

        const val TAG = "WEATHER_FRAGMENT"
    }
}
