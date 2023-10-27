package com.nbcamp.tripgo.view.calendar

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.databinding.DialogCalendarBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.calendar.CalendarFragmentTodayDecorator
import com.nbcamp.tripgo.util.calendar.CantSetDayDecorator
import com.nbcamp.tripgo.util.calendar.OutDateMonthDecorator
import com.nbcamp.tripgo.util.calendar.SaturdayDecorator
import com.nbcamp.tripgo.util.calendar.SundayDecorator
import com.nbcamp.tripgo.util.calendar.TodayDecorator
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.Calendar

class ScheduleModifyFragment : DialogFragment() {
    private var _binding: DialogCalendarBinding? = null
    private val binding: DialogCalendarBinding
        get() = _binding!!
    private var loadingDialog: LoadingDialog? = null
    private var entity: CalendarEntity? = null
    private var selectedDayList: ArrayList<CalendarDay>? = null
    private var forModifySchedule: ArrayList<CalendarEntity>? = null
    private val month = Calendar.getInstance().get(Calendar.MONTH)
    private val calendarViewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory(
            requireActivity()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCalendarBinding.inflate(layoutInflater)
        loadingDialog = LoadingDialog(requireActivity())
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        entity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("model", CalendarEntity::class.java)
        } else {
            arguments?.getParcelable("model")
        }
        selectedDayList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelableArrayList("selectedDayList", CalendarDay::class.java)
        } else {
            arguments?.getParcelable("selectedDayList")
        }
        forModifySchedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelableArrayList("forModifySchedule", CalendarEntity::class.java)
        } else {
            arguments?.getParcelable("forModifySchedule")
        }
        initViews()
        initViewModel()
    }


    private fun initViews() = with(binding) {
        negativeTextView.setOnClickListener {
            val fragment = parentFragmentManager.findFragmentByTag(TAG)
            if (fragment != null) {
                val dialogFragment = fragment as DialogFragment
                dialogFragment.dismiss()
            }
        }
        entity?.let {
            setCalendarOption(it)
        }
        runModifySchedule()

    }

    private fun initViewModel() = with(calendarViewModel) {
        with(binding) {
            // start ~ end date 사이의 기간을 달력에 표시
            schedulesDateState.observe(viewLifecycleOwner) { dateList ->
                // 일정이 있는 날엔 달력에 따로 표시해 주기 위한 리스트
                selectedDayList?.clear()
                selectedDayList?.addAll(dateList)

                addScheduleCalendarView.run {
                    removeDecorators()
                    invalidateDecorators()
                    addDecorators(
                        SaturdayDecorator(month, 1),
                        SundayDecorator(month, 1),
                        OutDateMonthDecorator(requireActivity(), month + 1),
                        CalendarFragmentTodayDecorator(requireActivity()),
                        selectedDayList?.let {
                            CantSetDayDecorator(requireActivity(), it)
                        }
                    )
                }

            }

            calendarClickModifyEvent.observe(requireActivity()) {
                when (it) {
                    true -> requireActivity().toast(getString(R.string.cant_select_duplicate_schedule))
                    false -> requireActivity().toast(getString(R.string.not_register_schedule_before_today))
                }
                binding.addScheduleCalendarView.clearSelection()
            }
        }
    }

    private fun setCalendarOption(model: CalendarEntity) = with(binding) {
        if (selectedDayList == null) {
            return
        }
        // 다이얼로그에 보여줄 스케줄은 현재 선택한 스케줄을 제외하고 선택할 수 있도록 함
        val filteredSchedule =
            forModifySchedule?.filter { it.endDate != model.endDate }
        calendarViewModel.setSelectedDate(filteredSchedule)
        addScheduleCalendarView.run {
            val month = Calendar.getInstance().get(Calendar.MONTH)
            removeDecorators()
            invalidateDecorators()
            addDecorators(
                SaturdayDecorator(month, 1),
                SundayDecorator(month, 1),
                OutDateMonthDecorator(requireActivity(), month + 1),
                TodayDecorator(requireActivity()),
                CantSetDayDecorator(requireActivity(), selectedDayList!!)
            )
            setOnMonthChangedListener { _, date ->
                removeDecorators()
                invalidateDecorators()
                addDecorators(
                    SaturdayDecorator(date.month, 0),
                    SundayDecorator(date.month, 0),
                    TodayDecorator(requireActivity()),
                    OutDateMonthDecorator(requireActivity(), date.month),
                    CantSetDayDecorator(requireActivity(), selectedDayList!!)
                )
            }
            setOnRangeSelectedListener { _, dates ->
                calendarViewModel.selectScheduleRange(dates, selectedDayList!!)
            }
            setOnDateChangedListener { _, date, _ ->
                // 사용자는 하루만 선택을 할 수도 있으므로 단일 처리도 해야함
                val dates = listOf(date, date)
                calendarViewModel.selectScheduleRange(dates, selectedDayList!!)
            }
        }
    }

    private fun runModifySchedule() {
        binding.calendarProgressBar.isVisible = false

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        loadingDialog = null
    }

    companion object {
        const val TAG = "SCHEDULE_DIALOG_FRAGMENT"
        fun newInstance(): DialogFragment = ScheduleModifyFragment()
    }
}
