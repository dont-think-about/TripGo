package com.nbcamp.tripgo.view.tour.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.databinding.TourRecyclerviewItemBinding

class TourAdapter(
    private val onClickItem: (FestivalItem) -> Unit
) : ListAdapter<FestivalItem, TourAdapter.TourViewHolder>(TourDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val binding =
            TourRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TourViewHolder(binding, onClickItem)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    private var userLat = 0.0 // 사용자 위도
    private var userLon = 0.0 // 사용자 경도

    fun setUserLocation(lat: Double, lon: Double) {
        userLat = lat
        userLon = lon
        notifyDataSetChanged()
    } // 위치 정보 변경 후 , RecyclerView 갱신

    private fun calculateDistanceTo(
        mapx: Double,
        mapy: Double,
        userLat: Double,
        userLon: Double
    ): Double {
        val R = 6371.0
        val dLat = Math.toRadians(mapy - userLat)
        val dLon = Math.toRadians(mapx - userLon)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(mapy)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    } // 사용자 위치와 주어진 좌표 간의 거리를 계산 하는 함수

    fun popularDistance(recyclerView: RecyclerView) {
        val sortedList = currentList.sortedBy { item ->
            calculateDistanceTo(item.mapx.toDouble(), item.mapy.toDouble(), userLat, userLon)
        }
        submitList(sortedList.toList()) {
            recyclerView.scrollToPosition(0)
        }
    } // 인기 있는 여행지  거리순 으로 정렬

    fun popularDate(recyclerView: RecyclerView) {
        val sortedList = currentList.sortedBy { it.eventstartdate }
        submitList(sortedList) {
            recyclerView.scrollToPosition(0)
        }
    } // 인기 있는 여행지 날짜순 으로 정렬

    class TourViewHolder(
        private val binding: TourRecyclerviewItemBinding,
        private val onClickItem: (FestivalItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FestivalItem) {
            itemView.setOnClickListener {
                onClickItem(item)
            }
            with(binding) {
                tourTitle.text = item.title
                tourAddress.text =
                    if (item.addr1.isNullOrEmpty()) itemView.context.getString(R.string.to_be_updated_later) else item.addr1
                tourContent.text =
                    "${formatDate(item.eventstartdate)} ~ ${formatDate(item.eventenddate)}"
                myImage.load(if (item.firstimage.isNullOrEmpty()) R.drawable.icon_no_image else item.firstimage)
            }
        }

        private fun formatDate(date: String): String {
            return if (date.length == 8) {
                "${date.substring(2, 4)}.${date.substring(4, 6)}.${date.substring(6, 8)}"
            } else {
                date // 원래 형식이 잘못된 경우, 원래 문자열을 반환
            }
        } // 날짜 xx.xx.xx로 변경 시켜 주는 함수
    }

    companion object {
        private val TourDiffCallback = object : DiffUtil.ItemCallback<FestivalItem>() {
            override fun areItemsTheSame(oldItem: FestivalItem, newItem: FestivalItem): Boolean {
                return oldItem.contentid == newItem.contentid // contentid를 고유 식별자로 사용하여 비교
            }

            override fun areContentsTheSame(oldItem: FestivalItem, newItem: FestivalItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
