package com.nbcamp.tripgo.view.tour.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.databinding.TourRecyclerviewItemBinding

class TourSearchAdapter(
    private val onClickItem: (KeywordItem) -> Unit // 아이템 클릭시 실행할 콜백 함수
) : ListAdapter<KeywordItem, TourSearchAdapter.TourViewHolder>(TourViewHolder.TourDiffCallback) {

    private var userLat = 0.0 // 사용자 위도
    private var userLon = 0.0 // 사용자 경도

    fun calculateDistance(item: KeywordItem): Double {
        return calculateDistanceTo(
            item.mapx.toDouble(),
            item.mapy.toDouble(),
            userLat,
            userLon
        )
    }

    fun setUserLocation(lat: Double, lon: Double) {
        userLat = lat
        userLon = lon
        notifyDataSetChanged()
    } // 위치 정보 변경 후, RecyclerView 갱신

    private fun calculateDistanceTo(
        mapx: Double,
        mapy: Double,
        userLat: Double,
        userLon: Double
    ): Double {
        val R = 6371.0
        val dLat = Math.toRadians(mapy - userLat)
        val dLon = Math.toRadians(mapx - userLon)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(mapy)) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    } // 사용자 위치와 주어진 좌표 간의 거리를 계산하는 함수

    fun tourDistance(recyclerView: RecyclerView) {
        val sortedList = currentList.sortedBy { item ->
            calculateDistanceTo(item.mapx.toDouble(), item.mapy.toDouble(), userLat, userLon)
        }
        submitList(sortedList.toList()) {
            recyclerView.scrollToPosition(0)
        }
    } // Tour 아이템 거리순으로 정렬

    fun tourDate(recyclerView: RecyclerView) {
        val sortedByDate = currentList.sortedBy { it.createdtime }
        submitList(sortedByDate) {
            recyclerView.scrollToPosition(0)
        }
    } // Tour 아이템 날짜순으로 정렬

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val binding =
            TourRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TourViewHolder(binding, onClickItem, ::calculateDistanceTo)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, userLat, userLon)
    }

    class TourViewHolder(
        private val binding: TourRecyclerviewItemBinding,
        private val onClickItem: (KeywordItem) -> Unit,
        private val calculateDistance: (Double, Double, Double, Double) -> Double
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: KeywordItem, userLat: Double, userLon: Double) {
            itemView.setOnClickListener { onClickItem(item) }
            with(binding) {
                tourTitle.text = item.title
                tourAddress.text =
                    if (item.addr1.isNullOrEmpty()) itemView.context.getString(R.string.to_be_updated_later) else item.addr1
                tourContent.text = binding.root.context.getString(
                    R.string.distance_from_my_location,
                    calculateDistance(
                        item.mapx.toDouble(),
                        item.mapy.toDouble(),
                        userLat,
                        userLon
                    ).toInt()
                )
                myImage.load(if (item.firstimage.isNullOrEmpty()) R.drawable.icon_no_image else item.firstimage)
            }
        }

        companion object {
            val TourDiffCallback = object : DiffUtil.ItemCallback<KeywordItem>() {
                override fun areItemsTheSame(oldItem: KeywordItem, newItem: KeywordItem): Boolean {
                    return oldItem.contentid == newItem.contentid
                }

                override fun areContentsTheSame(
                    oldItem: KeywordItem,
                    newItem: KeywordItem
                ): Boolean {
                    return oldItem == newItem
                }
            }
        }
    }
}
