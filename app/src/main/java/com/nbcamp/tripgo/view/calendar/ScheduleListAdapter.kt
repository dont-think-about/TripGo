package com.nbcamp.tripgo.view.calendar

import android.content.Context
import android.graphics.Color
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.databinding.ItemCalendarScheduleCardBinding
import java.util.Locale

class ScheduleListAdapter(
    private val scheduleContext: Context,
    private val onClickItem: (CalendarEntity) -> Unit,
    private val onLongClickItem: (CalendarEntity) -> Unit,
) : ListAdapter<CalendarEntity, ScheduleListAdapter.ScheduleViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        return ScheduleViewHolder(
            ItemCalendarScheduleCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            scheduleContext,
            onClickItem,
            onLongClickItem
        )
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScheduleViewHolder(
        private val binding: ItemCalendarScheduleCardBinding,
        private val scheduleContext: Context,
        private val onClickItem: (CalendarEntity) -> Unit,
        private val onLongClickItem: (CalendarEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val today = Calendar.getInstance(Locale.KOREA)
        private val year = today.get(Calendar.YEAR)
        private val month = today.get(Calendar.MONTH) + 1
        private val day = today.get(Calendar.DATE)
        fun bind(model: CalendarEntity) = with(binding) {
            val todayString =
                "$year${if (month < 10) "0$month" else "$month"}${if (day < 10) "0$day" else "$day"}"
            val isValid = model.endDate?.toInt()!! <= todayString.toInt()
            if (isValid) {
                when (model.isReviewed) {
                    true -> {
                        itemCheckTextView.run {
                            text = scheduleContext.getString(R.string.already_written_review)
                            setTextColor(Color.RED)
                        }
                    }
                    false -> {
                        itemCheckTextView.run {
                            text = scheduleContext.getString(R.string.possible_write_review)
                            setTextColor(Color.GREEN)
                        }
                    }
                    else -> Unit
                }

                itemView.setOnClickListener {
                    onClickItem(model)
                }
            }
            itemView.setOnLongClickListener {
                onLongClickItem(model)
                true
            }
            itemCheckTextView.isVisible = isValid
            defaultSetting(model)
        }

        private fun defaultSetting(model: CalendarEntity) = with(binding) {
            itemTitleTextView.text = model.title
            "${model.startDate} ~ ${model.endDate}".also { itemDateTextView.text = it }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CalendarEntity>() {
            override fun areItemsTheSame(
                oldItem: CalendarEntity,
                newItem: CalendarEntity
            ): Boolean = oldItem.contentId == newItem.contentId

            override fun areContentsTheSame(
                oldItem: CalendarEntity,
                newItem: CalendarEntity
            ): Boolean = oldItem == newItem
        }
    }
}
